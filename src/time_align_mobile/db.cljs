(ns time-align-mobile.db
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [clojure.string :as string]
            [clojure.test.check.generators :as gen]
            [time-align-mobile.navigation :as nav]))

;; generated old stuff
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::greeting]))
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
                                    :name ::period-spec})
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
                             :name ::task-spec})}))

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
                                    :name ::period-spec})
                          start-before-stop)}))

(s/def ::screens (set (keys nav/screens-map)))

(def app-db-spec
  (ds/spec {:spec {:view (ds/maybe [{:page    ::pages
                                     :form    (ds/maybe map?)
                                     :range   (ds/maybe {:start ::moment
                                                         :stop  ::moment})
                                     :filters (ds/maybe [simple-keyword?])}])

                   :navigation {:current-screen ::screens}

                   :tasks     [task-spec]
                   :templates (ds/maybe [template-spec])
                   :config    {:auto-log-time-align boolean?}}
            :name ::app-db-spec}))

(def app-db-test
  {:view       nil
   :navigation {:current-screen :day}
   :tasks      [{:id          (random-uuid)
                 :description "Using Time Align"
                 :created     (new js/Date 2018 4 28 15 57)
                 :last-edited (new js/Date 2018 4 28 15 57)
                 :data        {:category :default}
                 :color       "#2222aa"
                 :periods     nil}]
   :templates nil
   :config {:auto-log-time-align true}})

;; TODO use https://facebook.github.io/react-native/docs/appstate.html to log all time in app
;; old initial state of app-db
(def app-db {:greeting "Hello Clojurescript in Expo!"})
