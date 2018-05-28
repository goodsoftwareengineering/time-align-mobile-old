(ns time-align-mobile.db
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [clojure.string :as string]
            [clojure.test.check.generators :as gen]))

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
                                           :description string?
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
;; (defn generate-task [{:keys [color moment]}]
;;   (let [color (gen/generate (s/gen ::color))
;;         moment (gen/generate (s/gen ::moment))
;;         ])
;;   {:id (random-uuid)
;;    :description ""
;;    :created moment
;;    :last-edited moment
;;    :data {}
;;    :color color
;;    :periods })
(def task-spec
  (st/create-spec {:spec
                   (ds/spec {:spec {:id          uuid?
                                    :description string?
                                    :created     ::moment
                                    :last-edited ::moment
                                    :data        map?
                                    :color       ::color
                                    :periods     [period-spec]}
                             :name ::task-spec})}))

;; (def app-db-data-spec
;;   {(ds/req :view) [{:page-id uuid?
;;                     :forms   {:regular-form map?}
;;                     :range   {:start ::moment
;;                               :stop  ::moment}
;;                     :filters [(st/create-spec {:spec (s/or :some-filters simple-keyword?
;;                                                            :no-filters nil?)
;;                                                :gen  #(gen/return nil)})]}]

;;    (ds/req :navigation) {(ds/req :current-page) (s/spec #{:period
;;                                                           :task
;;                                                           :queue
;;                                                           :templates
;;                                                           :list-tasks
;;                                                           :list-periods
;;                                                           :home
;;                                                           :report
;;                                                           :calendar})}

;;    ;; (ds/maybe :tasks) [{(ds/req :id)       uuid?
;;    ;;                     (ds/req :color)    ::color
;;    ;;                     (ds/req :complete) boolean?
;;    ;;                     (ds/req :label)    string?

;;    ;;                     (ds/maybe :description) string?

;;    ;;                     (ds/opt :user-data) {(ds/req :created)  ::moment
;;    ;;                                          (ds/req :modified) ::moment}

;;    ;;                     (ds/opt :periods) [{(ds/req :id)      uuid?
;;    ;;                                         (ds/req :planned) boolean?

;;    ;;                                         (ds/maybe :start)       ::moment
;;    ;;                                         (ds/maybe :stop)        ::moment
;;    ;;                                         (ds/maybe :description) string?

;;    ;;                                         (ds/opt :data) {(ds/req :created)
;;    ;;                                                         ::moment
;;    ;;                                                         (ds/req :modified)
;;    ;;                                                         ::moment}}]}]

;;    ;; (ds/maybe :templates) [{(ds/req :id)      uuid?
;;    ;;                         (ds/req :planned) boolean?

;;    ;;                         (ds/maybe :description) string?

;;    ;;                         (ds/opt :relative-start) ::moment
;;    ;;                         (ds/opt :relative-stop)  ::moment
;;    ;;                         (ds/opt :duration)       integer?

;;    ;;                         (ds/opt :data) {(ds/req :created)  ::moment
;;    ;;                                         (ds/req :modified) ::moment}}]
;;    })
;; initial state of app-db
(def app-db {:greeting "Hello Clojurescript in Expo!"})
