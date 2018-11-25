(ns time-align-mobile.handlers
  (:require
    [re-frame.core :refer [reg-event-db ->interceptor reg-event-fx]]
    [zprint.core :refer [zprint]]
    [cljs.reader :refer [read-string]]
    [clojure.spec.alpha :as s]
    [time-align-mobile.db :as db :refer [app-db app-db-spec period-data-spec]]
    [time-align-mobile.js-imports :refer [alert]]
    [time-align-mobile.helpers :refer [same-day?]]
    [com.rpl.specter :as sp :refer-macros [select select-one setval transform]]))

;; -- Interceptors ----------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/develop/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db]
  (when-not (s/valid? spec db)
    (let [explaination (s/explain-data spec db)]
      (println (zprint explaination {:map {:force-nl? true}}))
      ;; (throw (ex-info (str "Spec check failed: " explain-data) explain-data))
      (alert "Failed spec validation" "Check the command line output.")
      true)))

(def validate-spec
  (if goog.DEBUG
    (->interceptor
        :id :validate-spec
        :after (fn [context]
                 (let [db (-> context :effects :db)
                       old-db (-> context :coeffects :db)]
                   (if (some? (check-and-throw app-db-spec db))
                     (assoc-in context [:effects :db] old-db)
                     context))))
    ->interceptor))

(def alert-message
  (->interceptor
   :id :alert
   :after (fn [context]
            (let [alert-message (-> context :effects :alert)]
              ;; message is ussually in the following format
              ;; "Failed data json validation SyntaxError: JSON Parse error: Expected '}'"
              ;; The user only cares about the Expected bit and the alert has limited space
              (when (some? alert-message) (alert
                                           "Validation failed"
                                           (str alert-message)
                                           ;; (last (.split (str alert-message)
                                           ;;               ":"))
                                           ))
              (setval [:effects :alert] sp/NONE context)))))

;; -- Helpers ---------------------------------------------------------------
(defn _clean-period [period]
  (select-keys period (keys period-data-spec)))

;; -- Handlers --------------------------------------------------------------

(defn initialize-db [_ _] app-db)

(defn navigate-to [{:keys [db]} [_ {:keys [current-screen params]}]]
  (merge {:db (-> db
                  (assoc-in [:navigation] {:current-screen current-screen
                                           :params         params})
                  ;; prevents using incompatible filters
                  (assoc-in [:active-filter] nil))}

         (when (= current-screen :bucket)
           {:dispatch [:load-bucket-form (:bucket-id params)]})
         (when (= current-screen :period)
           {:dispatch [:load-period-form (:period-id params)]})
         (when (= current-screen :template)
           {:dispatch [:load-template-form (:template-id params)]})
         (when (= current-screen :filter)
           {:dispatch [:load-filter-form (:filter-id params)]})))

(defn load-bucket-form [db [_ bucket-id]]
  (let [bucket      (select-one [:buckets sp/ALL #(= (:id %) bucket-id)] db)
        bucket-form (merge bucket {:data (with-out-str (zprint (:data bucket) {:map {:force-nl? true}}))})]
    (assoc-in db [:forms :bucket-form] bucket-form)))

(defn update-bucket-form [db [_ bucket-form]]
  (transform [:forms :bucket-form] #(merge % bucket-form) db))

(defn save-bucket-form [{:keys [db]} [_ date-time]]
  (let [bucket-form (get-in db [:forms :bucket-form])]
    (try
       (let [new-data (read-string (:data bucket-form))
             new-bucket (merge bucket-form {:data new-data
                                       :last-edited date-time})
            new-db (setval [:buckets sp/ALL #(= (:id %) (:id new-bucket))]
                           new-bucket
                           db)]
        {:db new-db
         ;; load bucket form so that the data string gets re-formatted prettier
         :dispatch [:load-bucket-form (:id new-bucket)]})
      (catch js/Error e
        {:db db
         :alert (str "Failed data json validation " e)}))))

(defn load-period-form [db [_ period-id]]
  (let [[sub-bucket period] (select-one
                             [:buckets sp/ALL
                              (sp/collect-one (sp/submap [:id :color :label]))
                              :periods sp/ALL #(= (:id %) period-id)] db)
        sub-bucket-remap    {:bucket-id    (:id sub-bucket)
                             :bucket-color (:color sub-bucket)
                             :bucket-label (:label sub-bucket)}
        period-form         (merge period
                                   {:data (with-out-str
                                            (zprint (:data period)
                                                    {:map {:force-nl? true}}))}
                                   sub-bucket-remap)]
    (assoc-in db [:forms :period-form] period-form)))

(defn update-period-form [db [_ period-form]]
  (let [period-form (if (contains? period-form :bucket-id)
                      (merge period-form
                             {:bucket-label (:label
                                             (select-one
                                              [:buckets
                                               sp/ALL
                                               #(= (:id %) (:bucket-id period-form))]
                                              db))})
                      ;; ^ pulls out the label when selecting new parent
                      ;; because all that comes from the picker is id
                      period-form)]
    (transform [:forms :period-form] #(merge % period-form) db)))

(defn save-period-form [{:keys [db]} [_ date-time]]
  (let [period-form (get-in db [:forms :period-form])]
    (try
      (let [new-data          (read-string (:data period-form))
            keys-wanted       (->> period-form
                                   (keys)
                                   ;; TODO use spec to get only keys wanted
                                   (remove #(or (= :bucket-id %)
                                                (= :bucket-label %)
                                                (= :bucket-color %))))
            new-period        (-> period-form
                                  (merge {:data        new-data
                                          :last-edited date-time})
                                  (select-keys keys-wanted))
            [old-bucket
             old-period]      (select-one [:buckets sp/ALL
                                       (sp/collect-one (sp/submap [:id]))
                                       :periods sp/ALL
                                       #(= (:id %) (:id new-period))] db)
            removed-period-db (setval [:buckets sp/ALL
                                       #(= (:id %) (:id old-bucket))
                                       :periods sp/ALL
                                       #(= (:id %) (:id old-period))]
                                      sp/NONE db)
            new-db            (setval [:buckets sp/ALL
                                       ;; TODO should the bucket-id come from period form?
                                       #(= (:id %) (:bucket-id period-form))
                                       :periods
                                       sp/NIL->VECTOR
                                       sp/AFTER-ELEM]
                                      new-period removed-period-db)]

        {:db       new-db
         ;; load period form so that the data string gets re-formatted prettier
         :dispatch [:load-period-form (:id new-period)]})
      (catch js/Error e
        {:db    db
         :alert (str "Failed data json validation " e)}))))

(defn load-template-form [db [_ template-id]]
  (let [[sub-bucket template] (select-one
                             [:buckets sp/ALL
                              (sp/collect-one (sp/submap [:id :color :label]))
                              :templates sp/ALL #(= (:id %) template-id)] db)
        sub-bucket-remap    {:bucket-id    (:id sub-bucket)
                             :bucket-color (:color sub-bucket)
                             :bucket-label (:label sub-bucket)}
        template-form         (merge template
                                   {:data (with-out-str
                                            (zprint (:data template)
                                                    {:map {:force-nl? true}}))}
                                   sub-bucket-remap)]
    (assoc-in db [:forms :template-form] template-form)))

(defn update-template-form [db [_ template-form]]
  (let [template-form (if (contains? template-form :bucket-id)
                      (merge template-form
                             {:bucket-label (:label
                                             (select-one
                                              [:buckets
                                               sp/ALL
                                               #(= (:id %) (:bucket-id template-form))]
                                              db))})
                      ;; ^ pulls out the label when selecting new parent
                      ;; because all that comes from the picker is id
                      template-form)]
    (transform [:forms :template-form] #(merge % template-form) db)))

(defn save-template-form [{:keys [db]} [_ date-time]]
  (let [template-form (get-in db [:forms :template-form])]
    (try
      (let [new-data          (read-string (:data template-form))
            keys-wanted       (->> template-form
                                   (keys)
                                   (remove #(or (= :bucket-id %)
                                                (= :bucket-label %)
                                                (= :bucket-color %))))
            new-template        (-> template-form
                                  (merge {:data        new-data
                                          :last-edited date-time})
                                  (select-keys keys-wanted))
            [old-bucket
             old-template]      (select-one [:buckets sp/ALL
                                       (sp/collect-one (sp/submap [:id]))
                                       :templates sp/ALL
                                       #(= (:id %) (:id new-template))] db)
            removed-template-db (setval [:buckets sp/ALL
                                       #(= (:id %) (:id old-bucket))
                                       :templates sp/ALL
                                       #(= (:id %) (:id old-template))]
                                      sp/NONE db)
            new-db            (setval [:buckets sp/ALL
                                       #(= (:id %) (:bucket-id template-form))
                                       :templates
                                       sp/NIL->VECTOR
                                       sp/AFTER-ELEM]
                                      new-template removed-template-db)]

        {:db       new-db
         ;; load template form so that the data string gets re-formatted prettier
         :dispatch [:load-template-form (:id new-template)]})
      (catch js/Error e
        {:db    db
         :alert (str "Failed data read validation " e)}))))

(defn load-filter-form [db [_ filter-id]]
  (let [filter     (select-one
                    [:filters sp/ALL #(= (:id %) filter-id)] db)
        filter-form (merge filter
                           {:predicates (with-out-str
                                          (zprint (:predicates filter)
                                                  {:map {:force-nl? true}}))}
                           {:sort (with-out-str
                                          (zprint (:sort filter)
                                                  {:map {:force-nl? true}}))})]
    (assoc-in db [:forms :filter-form] filter-form)))

(defn update-filter-form [db [_ filter-form]]
  (transform [:forms :filter-form] #(merge % filter-form) db))

(defn save-filter-form [{:keys [db]} [_ date-time]]
  (let [filter-form (get-in db [:forms :filter-form])]
    (try
      (let [new-predicates {:predicates (read-string (:predicates filter-form))}
            new-sort {:sort (read-string (:sort filter-form))}
            new-filter        (-> filter-form
                                  (merge {:last-edited date-time}
                                         new-predicates
                                         new-sort))
            old-filter        (select-one [:filters sp/ALL
                                           #(= (:id %) (:id new-filter))] db)
            removed-filter-db (setval [:filters sp/ALL
                                       #(= (:id %) (:id old-filter))]
                                      sp/NONE db)
            new-db            (setval [:filters
                                       sp/NIL->VECTOR
                                       sp/AFTER-ELEM]
                                      new-filter removed-filter-db)]

        {:db       new-db
         ;; load filter form so that the data string gets re-formatted prettier
         :dispatch [:load-filter-form (:id new-filter)]})
      (catch js/Error e
        {:db    db
         :alert (str "Failed predicate read validation " e)}))))

(defn update-active-filter [db [_ id]]
  (assoc db :active-filter id))

(defn add-new-bucket [{:keys [db]} [_ {:keys [id now]}]]
  {:db (setval [:buckets
                sp/NIL->VECTOR
                sp/AFTER-ELEM]
               {:id          id
                :label       ""
                :created     now
                :last-edited now
                :data        {}
                :color       "#ff1122"
                :templates   nil
                :periods     nil}
               db)
   :dispatch [:navigate-to {:current-screen :bucket
                            :params {:bucket-id id}}]})

(defn add-new-period [{:keys [db]} [_ {:keys [bucket-id id now]}]]
  {:db (setval [:buckets sp/ALL
                #(= (:id %) bucket-id)
                :periods
                sp/NIL->VECTOR
                sp/AFTER-ELEM]
               {:id id
                :created now
                :last-edited now
                :label ""
                :data {}
                :planned true
                :start now
                :stop (new js/Date (+ (.valueOf now) (* 1000 60)))}
               db)
   :dispatch [:navigate-to {:current-screen :period
                            :params {:period-id id}}]})

(defn add-template-period [{:keys [db]} [_ {:keys [template id now]}]]
  ;; template needs bucket-id
  ;; TODO refactor so that this function takes in a template id (maybe bucket id)
  ;; and then queries the db for the template
  (let [new-data       (merge (:data template)
                              {:template-id (:id template)})
        start-relative (:start template)
        duration       (:duration template)
        start          (if (some? start-relative)
                         (new js/Date
                              (.getFullYear now)
                              (.getMonth now)
                              (.getDate now)
                              (:hour start-relative)
                              (:minute start-relative))
                         now)
        stop           (if (some? duration)
                         (new js/Date (+ (.valueOf start) duration))
                         (new js/Date (+ (.valueOf start) (* 1000 60))))
        period         (merge template
                              {:id    id
                               :data  new-data
                               :created now
                               :last-edited now
                               :start start
                               :stop  stop})
        period-clean   (_clean-period period)]

    {:db       (setval [:buckets sp/ALL
                        #(= (:id %) (:bucket-id template))
                        :periods
                        sp/NIL->VECTOR
                        sp/AFTER-ELEM]
                       period-clean
                       db)
     :dispatch [:navigate-to {:current-screen :period
                              :params         {:period-id id}}]}))

(defn add-new-template [{:keys [db]} [_ {:keys [bucket-id id now]}]]
  {:db       (setval [:buckets sp/ALL
                      #(= (:id %) bucket-id)
                      :templates
                      sp/NIL->VECTOR
                      sp/AFTER-ELEM]
                     {:id          id
                      :created     now
                      :last-edited now
                      :label       ""
                      :data        {}
                      :planned     true
                      :start       {:hour   (.getHours now)
                                    :minute (.getMinutes now)}
                      :stop        {:hour   (.getHours now)
                                    :minute (+ 5 (.getMinutes now))}
                      :duration    nil}
                     db)
   :dispatch [:navigate-to {:current-screen :template
                            :params         {:template-id id}}]})

(defn add-new-filter [{:keys [db]} [_ {:keys [id now]}]]
  {:db (setval [:filters
                sp/NIL->VECTOR
                sp/AFTER-ELEM]
               {:id          id
                :label       ""
                :created     now
                :last-edited now
                :compatible []
                :sort nil
                :predicates []}
               db)
   :dispatch [:navigate-to {:current-screen :filter
                            :params {:filter-id id}}]})

(defn delete-bucket [{:keys [db]} [_ id]]
  {:db (->> db
            (setval [:buckets sp/ALL #(= id (:id %))] sp/NONE)
            (setval [:forms :bucket-form] nil))
   ;; TODO pop stack when possible
   :dispatch [:navigate-to {:current-screen :buckets}]})

(defn delete-period [{:keys [db]} [_ id]]
  {:db (->> db
            (setval [:buckets sp/ALL :periods sp/ALL #(= id (:id %))] sp/NONE)
            (setval [:forms :period-form] nil)
            (setval [:selected-period] nil))
   ;; TODO pop stack when possible
   :dispatch [:navigate-to {:current-screen :periods}]})

(defn delete-template [{:keys [db]} [_ id]]
  {:db (->> db
            (setval [:buckets sp/ALL :templates sp/ALL #(= id (:id %))] sp/NONE)
            (setval [:forms :template-form] nil))
   ;; TODO pop stack when possible
   :dispatch [:navigate-to {:current-screen :templates}]})

(defn delete-filter [{:keys [db]} [_ id]]
  {:db (->> db
            (setval [:filters sp/ALL #(= id (:id %))] sp/NONE)
            (setval [:forms :filter-form] nil))
   ;; TODO pop stack when possible
   :dispatch [:navigate-to {:current-screen :filters}]})

(defn select-period [db [_ id]]
  (assoc-in db [:selected-period] id))

(defn update-period [db [_ {:keys [id update-map]}]]
  (transform [:buckets sp/ALL
              :periods sp/ALL
              #(= id (:id %))]
             #(merge % update-map)
             db))

(defn add-period [db [_ {:keys [period bucket-id]}]]
  (setval [:buckets sp/ALL
           #(= (:id %) bucket-id)
           :periods
           sp/NIL->VECTOR
           sp/AFTER-ELEM]
          (merge (_clean-period period)
                 {:id (random-uuid)})
          db))

(defn select-next-or-prev-period [db [_ direction]]
  (if-let [selected-period-id (get-in db [:selected-period])]
    (let [displayed-day (get-in db [:time-navigators :day])
          selected-period (select-one [:buckets sp/ALL :periods sp/ALL
                                       #(= selected-period-id (:id %))] db)
          sorted-periods (->> db
                              (select [:buckets sp/ALL :periods sp/ALL])
                              ;; Next period needs to be on this displayed day
                              (filter #(and (some? (:start %))
                                            (some? (:stop %))
                                            (or (same-day? (:start %) displayed-day)
                                                (same-day? (:stop %) displayed-day))))
                              ;; Next period needs to be visible on this track
                              (filter #(= (:planned selected-period) (:planned %)))
                              (sort-by #(.valueOf (:start %)))
                              (#(if (= direction :prev)
                                  (reverse %)
                                  %)))
          next-period    (->> sorted-periods
                              ;; Since they are sorted, drop them until you get to
                              ;; the current selected period.
                              ;; Then take the next one.
                              (drop-while #(not (= (:id %) selected-period-id)))
                              (second))]
      (if (some? next-period)
        (assoc-in db [:selected-period] (:id next-period))
        db))
    db))

(defn update-day-time-navigator [db [_ new-date]]
  (assoc-in db [:time-navigators :day] new-date))

(defn tick [db [_ date-time]]
  (let [period-in-play-id (get-in db [:period-in-play-id])]

    ;; TODO remove this
    (println (str "tick update: " (.toTimeString date-time)))

    ;; Update period in play if there is one
    (-> (if (some? period-in-play-id)
          (do (println "Updating period in play")
              (transform [:buckets sp/ALL
                          :periods sp/ALL
                          #(= (:id %) period-in-play-id)]

                         #(merge % {:stop date-time})

                         db))
          db)
        ;; update now regardless
        (assoc-in [:now] date-time))))

(defn play-from-period [db [_ {:keys [id time-started new-id]}]]
  (let [[bucket-just-id
         period-to-play-from] (select-one [:buckets sp/ALL
                                           (sp/collect-one (sp/submap [:id]))
                                           :periods sp/ALL
                                           #(= (:id %) id)] db)
        new-period            (merge period-to-play-from
                                     {:id      new-id
                                      :planned false
                                      :start   time-started
                                      :stop    (->> time-started
                                                    (.valueOf)
                                                    (+ (* 25 60 1000))
                                                    (js/Date.))})]
    (->> db
         ;; Add new period
         (setval [:buckets sp/ALL
                  #(= (:id %) (:id bucket-just-id))
                  :periods
                  sp/NIL->VECTOR
                  sp/AFTER-ELEM]
                 new-period )
         ;; Set it as playing
         (setval [:period-in-play-id] new-id)
         ;; Set it as selected
         (setval [:selected-period] new-id))))

(reg-event-db :initialize-db [validate-spec] initialize-db)
(reg-event-fx :navigate-to [validate-spec] navigate-to)
(reg-event-db :load-bucket-form [validate-spec] load-bucket-form)
(reg-event-db :update-bucket-form [validate-spec] update-bucket-form)
(reg-event-fx :save-bucket-form [alert-message validate-spec] save-bucket-form)
(reg-event-db :load-period-form [validate-spec] load-period-form)
(reg-event-db :update-period-form [validate-spec] update-period-form)
(reg-event-fx :save-period-form [alert-message validate-spec] save-period-form)
(reg-event-db :load-template-form [validate-spec] load-template-form)
(reg-event-db :update-template-form [validate-spec] update-template-form)
(reg-event-fx :save-template-form [alert-message validate-spec] save-template-form)
(reg-event-db :load-filter-form [validate-spec] load-filter-form)
(reg-event-db :update-filter-form [validate-spec] update-filter-form)
(reg-event-fx :save-filter-form [alert-message validate-spec] save-filter-form)
(reg-event-db :update-active-filter [validate-spec] update-active-filter)
(reg-event-fx :add-new-bucket [validate-spec] add-new-bucket)
(reg-event-fx :add-new-period [validate-spec] add-new-period)
(reg-event-fx :add-template-period [validate-spec] add-template-period)
(reg-event-fx :add-new-template [validate-spec] add-new-template)
(reg-event-fx :add-new-filter [validate-spec] add-new-filter)
(reg-event-fx :delete-bucket [validate-spec] delete-bucket)
(reg-event-fx :delete-period [validate-spec] delete-period)
(reg-event-fx :delete-template [validate-spec] delete-template)
(reg-event-fx :delete-filter [validate-spec] delete-filter)
(reg-event-db :select-period [validate-spec] select-period)
(reg-event-db :update-period [validate-spec] update-period)
(reg-event-db :add-period [validate-spec] add-period)
(reg-event-db :select-next-or-prev-period [validate-spec] select-next-or-prev-period)
(reg-event-db :update-day-time-navigator [validate-spec] update-day-time-navigator)
(reg-event-db :tick [validate-spec] tick)
(reg-event-db :play-from-period [validate-spec] play-from-period)
