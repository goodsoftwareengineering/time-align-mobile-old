(ns time-align-mobile.db
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [spec-tools.core :as st]
            [clojure.string :as string]
            [clojure.test.check.generators :as gen]
            [time-align-mobile.navigation :as nav]))

;; end of old stuff

;; moment stuff
(def moment-tz (.-tz (js/require "moment-timezone")))
(def hour-ms
  (->> 1
       (* 60)
       (* 60)
       (* 1000)))
(defn get-default-timezone []
  (.guess moment-tz))
(defn set-hour-for-date [date hour zone]
  (-> (moment-tz date zone)
      (.hour hour)
      (.startOf "hours")
      js/Date.))
(defn start-of-today [date zone]
  (set-hour-for-date date 0 zone))
(defn end-of-today [date zone]
  (set-hour-for-date date 20 zone)) ;;Set to 20 to avoid straddling the date line
(defn make-date
  ([] (.toDate (moment-tz (js/Date.) "UTC")))
  ( [year month day]
   (make-date year month day 0))
  ( [year month day hour]
   (make-date year month day hour 0))
  ( [year month day hour minute]
   (make-date year month day hour minute 0))
  ( [year month day hour minute second]
   (make-date year month day hour minute second 0))
  ( [year month day hour minute second millisecond]
   (-> (js/Date. (.UTC js/Date year (- 1 month) day hour minute second millisecond))
       (moment-tz "UTC"))))
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
  (if (and
       (contains? period :start)
       (contains? period :stop)
       (some? (:start period))
       (some? (:stop period)))
    (> (.valueOf (:stop period))
       (.valueOf (:start period)))
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
(def period-spec
  (st/create-spec {:spec (s/and
                          (ds/spec {:spec {:id          uuid?
                                           :label       string?
                                           :created     ::moment
                                           :last-edited ::moment
                                           :data        map?
                                           :planned     boolean?
                                           :start       (ds/maybe ::moment)
                                           :stop        (ds/maybe ::moment)}
                                    :name ::period})
                          start-before-stop)
                   :gen  #(gen/fmap generate-period
                                    (s/gen ::moment))}))

;; task
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
(def task-spec
  (st/create-spec {:spec
                   (ds/spec {:spec {:id          uuid?
                                    :label       string?
                                    :created     ::moment
                                    :last-edited ::moment
                                    :data        map?
                                    :color       ::color
                                    :periods     (ds/maybe [period-spec])}
                             :name ::task})}))

;; template
(def template-spec
  (st/create-spec {:spec (s/and
                          (ds/spec {:spec {:id          uuid?
                                           :task-id     uuid?
                                           :label       string?
                                           :created     ::moment
                                           :last-edited ::moment
                                           :data        map?
                                           :planned     boolean?
                                           :start       (ds/maybe {:hour   integer?
                                                                   :minute integer?})
                                           :stop        (ds/maybe {:hour   integer?
                                                                   :minute integer?})
                                           :duration    (ds/maybe integer?)}
                                    :name ::template})
                          start-before-stop)}))

(def screen-id-set (set (->> nav/screens-map
                          (map (fn [{:keys [id]}] id)))))

(s/def ::screen screen-id-set)

(def app-db-spec
  (ds/spec {:spec {:view {:task-form {:id (ds/maybe uuid?) ;; TODO relying on this for updates could open up a race condition?
                                      :structured-data-current-path [keyword?]
                                      :new-map-item {:key (ds/maybe keyword?)
                                                     :type (ds/maybe (s/spec
                                                                      #{:map
                                                                        :boolean
                                                                        :string
                                                                        :number
                                                                        :coll}))}
                                      :new-coll-item {:type (ds/maybe (s/spec
                                                                       #{:map
                                                                         :boolean
                                                                         :string
                                                                         :number
                                                                         :coll}))}}}

                   :navigation {:current-screen ::screen
                                :params (ds/maybe map?)}

                   :tasks     [task-spec]
                   :templates (ds/maybe [template-spec])
                   :config    {:auto-log-time-align boolean?}}
            :name ::app-db}))

(def app-db
  {:view       {:task-form {:id (uuid "4b9b07da-5222-408c-aba4-777f0a1203af")
                            :structured-data-current-path []
                            :new-map-item {:key nil
                                           :type nil}
                            :new-coll-item {:type nil}}}
   :navigation {:current-screen :day
                :params         nil}
   :tasks      [{:id          (uuid "a7396f81-38d4-4d4f-ab19-a7cef18c4ea2")
                 :label       "Using Time Align"
                 :created     (new js/Date 2018 4 28 15 57)
                 :last-edited (new js/Date 2018 4 28 15 57)
                 :data        {:category :default}
                 :color       "#2222aa"
                 :periods     nil}

                {:id          (uuid "4b9b07da-5222-408c-aba4-777f0a1203af")
                 :label       "Using Time Align"
                 :created     (new js/Date 2018 4 28 15 57)
                 :last-edited (new js/Date 2018 4 28 15 57)
                 :data        {:string                                 "default"
                               :boolean                                true
                               :number                                 1.2
                               :another-number                         555
                               :keyword-as-value                       :keyword-value
                               :map                                    {:string-in-map "key-val"
                                                                        :vec-in-map    [1 2 3 4 5]
                                                                        :map-in-map    {:list-in-map-in-map '("a" "b" "c")}}
                               :vector                                 [1 2 3 "string"]
                               :vector-with-keys                       [:a :b "c"]
                               :map-to-test-keyboard-aware-auto-scroll {:a "a"
                                                                        :b "b"
                                                                        :c "c"
                                                                        :d "d"
                                                                        :e "e"
                                                                        :f "f"
                                                                        :g 6
                                                                        :h "7"
                                                                        :i "i"
                                                                        :j {:letter "j"
                                                                            :index  9}
                                                                        :k "k"
                                                                        :l ["m" "n" "o" "p"]}}
                 :color       "#2222aa"
                 :periods     nil}
                ]
   :templates  nil
   :config     {:auto-log-time-align true}})

;; TODO use https://facebook.github.io/react-native/docs/appstate.html to log all time in app
;; old initial state of app-db
