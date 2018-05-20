(ns time-align-mobile.db
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [clojure.string :as string]
            [clojure.test.check.generators :as gen]))

;; spec of app-db
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::greeting]))

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

(def app-db-data-spec
  {(ds/req :view)       [{(ds/req :page-id) uuid?
                          ;; (ds/opt :form {(ds/opt :regular-form {(ds/opt)})})
                          (ds/opt :range)   {(ds/req :start) ::moment
                                             (ds/req :stop)  ::moment}
                          (ds/opt :filter)  {(ds/maybe :tags) [keyword?]}}]
   (ds/req :navigation) {(ds/req :current-page) keyword?}

   (ds/maybe :tasks) [{(ds/req :id)       uuid?
                       (ds/req :color)    ::color
                       (ds/req :complete) boolean?
                       (ds/req :label)    string?

                       (ds/maybe :description) string?

                       (ds/opt :user-data) {(ds/req :created)  ::moment
                                            (ds/req :modified) ::moment}

                       (ds/opt :periods) [{(ds/req :id)      uuid?
                                           (ds/req :planned) boolean?

                                           (ds/maybe :start)       ::moment
                                           (ds/maybe :stop)        ::moment
                                           (ds/maybe :description) string?

                                           (ds/opt :data) {(ds/req :created)
                                                           ::moment
                                                           (ds/req :modified)
                                                           ::moment}}]}]

   (ds/maybe :templates) [{(ds/req :id)      uuid?
                           (ds/req :planned) boolean?

                           (ds/maybe :description) string?

                           (ds/opt :relative-start) ::moment
                           (ds/opt :relative-stop)  ::moment
                           (ds/opt :duration)       integer?

                           (ds/opt :data) {(ds/req :created)  ::moment
                                           (ds/req :modified) ::moment}}]})

;; initial state of app-db
(def app-db {:greeting "Hello Clojurescript in Expo!"})
