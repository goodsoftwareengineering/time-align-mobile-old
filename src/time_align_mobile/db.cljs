(ns time-align-mobile.db
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [spec-tools.core :as st]
            [clojure.string :as string]
            [clojure.test.check.generators :as gen]
            [time-align-mobile.navigation :as nav]
            [time-align-mobile.js-imports :refer [make-date
                                                  get-default-timezone
                                                  start-of-today
                                                  end-of-today]]))

(def hour-ms
  (->> 1
       (* 60)
       (* 60)
       (* 1000)))
(def time-range
  (range (.valueOf (start-of-today (make-date) (get-default-timezone)))
         (.valueOf (end-of-today (make-date) (get-default-timezone)))
         hour-ms))
(def time-set
  (set (->> time-range
            (map #(new js/Date %)))))
(s/def ::moment (s/with-gen inst? #(s/gen time-set)))

;; period
(defn start-before-stop [period]
  (if

      ;; Check that it has time stamps
      (and
       (contains? period :start)
       (contains? period :stop)
       (some? (:start period))
       (some? (:stop period)))

    ;; If it has time stamps they need to be valid
    (> (.valueOf (:stop period))
       (.valueOf (:start period)))

    ;; Passes if it has no time stamps
    true))
(defn generate-period [moment]
  (let [desc-chance   (> 0.5 (rand))
        queue-chance  (> 0.5 (rand))
        actual-chance (> 0.5 (rand))
        start         (.valueOf moment)
        stop          (->> start
                           (+ (rand-int (* 2 hour-ms))))
        stamps        (if queue-chance
                        {:start nil
                         :stop  nil}
                        {:start (new js/Date start)
                         :stop  (new js/Date stop)})
        type          (if-not (empty? stamps)
                        (if actual-chance
                          {:planned false}
                          {:planned true})
                        {:planned :true})]

                                      (merge stamps type
                                             {:id (random-uuid)}
                                             {:data        {}
                                              :description ""
                                              :created     moment
                                              :last-edited moment})))
(def period-data-spec {:id          uuid?
                       :created     ::moment
                       :last-edited ::moment
                       :label       string?
                       :planned     boolean?
                       :start       (ds/maybe ::moment)
                       :stop        (ds/maybe ::moment)
                       :data        map?})
(def period-spec
  (st/create-spec {:spec (s/and
                          (ds/spec {:spec period-data-spec
                                    :name ::period})
                          start-before-stop)
                   :gen  #(gen/fmap generate-period
                                    (s/gen ::moment))}))

;; template
(defn start-before-stop-template [template]
  (let [start-hour   (:hour (:start template))
        start-minute (:minute (:start template))
        stop-hour    (:hour (:stop template))
        stop-minute  (:minute (:stop template))]
    (if
        ;; Check that it has time stamps
        (and
         (contains? template :start)
         (contains? template :stop)
         (some? (:start template))
         (some? (:stop template)))

      ;; If it has time stamps they need to be valid
      ;; Compare hours then minutes
      (or (> stop-hour start-hour)
          (if (= stop-hour start-hour)
            (> stop-minute start-minute)))

      ;; Passes if it has no time stamps
      true)))

(def template-data-spec {:id          uuid?
                         :label       string?
                         :created     ::moment
                         :last-edited ::moment
                         :data        map?
                         :planned     boolean?
                         :start       (ds/maybe {:hour   integer?
                                                 :minute integer?})
                         :stop        (ds/maybe {:hour   integer?
                                                 :minute integer?})
                         :duration    (ds/maybe integer?)})
(def template-spec
  (st/create-spec {:spec (s/and
                          (ds/spec {:spec template-data-spec
                                    :name ::template})
                          start-before-stop-template)}))

;; bucket
(s/def ::hex-digit (s/with-gen
                     (s/and string? #(contains? (set "0123456789abcdef") %))
                     #(s/gen (set "0123456789abcdef"))))
(s/def ::hex-str (s/with-gen
                   (s/and string? (fn [s] (every? #(s/valid? ::hex-digit %) (seq s))))
                   #(gen/fmap string/join (gen/vector (s/gen ::hex-digit) 6))))
(s/def ::color (s/with-gen
                 (s/and #(= "#" (first %))
                        #(s/valid? ::hex-str (string/join (rest %)))
                        #(= 7 (count %)))
                 #(gen/fmap
                   (fn [hex-str] (string/join (cons "#" hex-str)))
                   (s/gen ::hex-str))))
(def bucket-data-spec {:id          uuid?
                       :label       string?
                       :created     ::moment
                       :last-edited ::moment
                       :data        map?
                       :color       ::color
                       :templates   (ds/maybe [template-spec])
                       :periods     (ds/maybe [period-spec])})
(def bucket-spec
  (st/create-spec {:spec
                   (ds/spec {:spec bucket-data-spec
                             :name ::bucket})}))

(def screen-id-set (set (->> nav/screens-map
                          (map (fn [{:keys [id]}] id)))))
(s/def ::screen screen-id-set)

;; filter
(def filter-data-spec
  {:id          uuid?
   :label       string?
   :created     ::moment
   :last-edited ::moment
   :predicates  [{:path [keyword?]
                  :value string?
                  :negate boolean?}]})

;; app-db
(def app-db-spec
  (ds/spec {:spec {:forms         {:bucket-form
                                   (ds/maybe (merge bucket-data-spec
                                                    {:data string?}))
                                   :period-form
                                   (ds/maybe (merge period-data-spec
                                                    {:data         string?
                                                     :bucket-id    uuid?
                                                     :bucket-label string?
                                                     :bucket-color ::color}))
                                   :template-form
                                   (ds/maybe (merge template-data-spec
                                                    {:data         string?
                                                     :bucket-id    uuid?
                                                     :bucket-label string?
                                                     :bucket-color ::color}))
                                   :filter-form
                                   (ds/maybe (merge filter-data-spec
                                                    {:predicates string?}))}
                   :active-filter (ds/maybe uuid?)
                   :filters       [filter-data-spec]
                   :navigation    {:current-screen ::screen
                                   :params         (ds/maybe map?)}

                   :buckets [bucket-spec]
                   :config  {:auto-log-time-align boolean?}}
            :name ::app-db}))
(def app-db
  {:forms         {:bucket-form   nil
                   :period-form   nil
                   :template-form nil
                   :filter-form   nil}
   :active-filter nil
   :filters       [{:id          (uuid "bbc34081-38d4-4d4f-ab19-a7cef18c1212")
                    :label       "basic"
                    :created     (new js/Date 2018 4 28 15 57)
                    :last-edited (new js/Date 2018 4 28 15 57)
                    :predicates  [{:path [:bucket-label] :value "This one has periods" :negate false}]}
                   {:id          (uuid "defaaa81-38d4-4d4f-ab19-a7cef18c1300")
                    :label       "other"
                    :created     (new js/Date 2018 4 28 15 57)
                    :last-edited (new js/Date 2018 4 28 15 57)
                    :predicates  [{:path [:data :category] :value "other" :negate false}
                                  {:path [:bucket-label] :value "This one has periods" :negate false}]}]
   :navigation    {:current-screen :day
                   :params         nil}
   :buckets       [{:id          (uuid "a7396f81-38d4-4d4f-ab19-a7cef18c4ea2")
                    :label       "This one has periods"
                    :created     (new js/Date 2018 4 28 15 57)
                    :last-edited (new js/Date 2018 4 28 15 57)
                    :data        {:category :default}
                    :color       "#2222aa"
                    :templates   nil
                    :periods     [{:id          (uuid "a8404f81-38d4-4d4f-ab19-a7cef18c4531")
                                   :created     (new js/Date 2018 4 28 15 57)
                                   :last-edited (new js/Date 2018 4 28 15 57)
                                   :label       ""
                                   :planned     true
                                   :start       (new js/Date 2018 6 28 14 55)
                                   :stop        (new js/Date 2018 6 28 15 35)
                                   :data        {}}
                                  {:id          (uuid "e3314f81-38d4-4d4f-ab19-a7cef17c4182")
                                   :created     (new js/Date 2018 4 28 15 58)
                                   :last-edited (new js/Date 2018 4 28 15 58)
                                   :label       ""
                                   :planned     false
                                   :start       (new js/Date 2018 6 28 16 55)
                                   :stop        (new js/Date 2018 6 28 17 35)
                                   :data        {:mood :neutral}}]}

                   {:id          (uuid "8c3907da-5222-408c-aba4-777f0a1204de")
                    :label       "This one has a template"
                    :created     (new js/Date 2018 4 28 15 57)
                    :last-edited (new js/Date 2018 4 28 15 57)
                    :data        {:string           "default"
                                  :boolean          true
                                  :number           1.2
                                  :another-number   555
                                  :keyword-as-value :keyword-value
                                  :map              {:string-in-map "key-val"
                                                     :vec-in-map    [1 2 3 4 5]
                                                     :map-in-map    {:list-in-map-in-map '("a" "b" "c")}}
                                  :vector           [1 2 3 "string"]
                                  :vector-with-keys [:a :b "c"]}
                    :color       "#2222aa"
                    :templates   [{:id          (uuid "c52e4f81-38d4-4d4f-ab19-a7cef18c8882")
                                   :created     (new js/Date 2018 4 28 15 57)
                                   :last-edited (new js/Date 2018 4 28 15 57)
                                   :label       ""
                                   :planned     true
                                   :start       {:hour 9 :minute 32}
                                   :stop        {:hour 10 :minute 4}
                                   :duration    nil
                                   :data        {:category "basic"}}
                                  {:id          (uuid "da3e4f81-38d4-4d4f-ab19-a7cef18c6641")
                                   :created     (new js/Date 2018 4 28 15 57)
                                   :last-edited (new js/Date 2018 4 28 15 57)
                                   :label       ""
                                   :planned     true
                                   :start       {:hour 4 :minute 32}
                                   :stop        {:hour 10 :minute 4}
                                   :duration    nil
                                   :data        {}}
                                  {:id          (uuid "ef111f81-38d4-4d4f-ab19-a7cef18caa31")
                                   :created     (new js/Date 2018 4 28 15 57)
                                   :last-edited (new js/Date 2018 4 28 15 57)
                                   :label       ""
                                   :planned     true
                                   :start       {:hour 5 :minute 32}
                                   :stop        {:hour 10 :minute 4}
                                   :duration    nil
                                   :data        {}}]
                    :periods     nil}
                   {:id          (uuid "4b9b07da-5222-408c-aba4-777f0a1203af")
                    :label       "Using Time Align"
                    :created     (new js/Date 2018 4 28 15 57)
                    :last-edited (new js/Date 2018 4 28 15 57)
                    :data        {:string           "default"
                                  :boolean          true
                                  :number           1.2
                                  :another-number   555
                                  :keyword-as-value :keyword-value
                                  :map              {:string-in-map "key-val"
                                                     :vec-in-map    [1 2 3 4 5]
                                                     :map-in-map    {:list-in-map-in-map '("a" "b" "c")}}
                                  :vector           [1 2 3 "string"]
                                  :vector-with-keys [:a :b "c"]}
                    :color       "#2222aa"
                    :templates   nil
                    :periods     nil}
                   ]
   :templates nil
   :config    {:auto-log-time-align true}})

;; TODO use https://facebook.github.io/react-native/docs/appstate.html to log all time in app
;; old initial state of app-db
