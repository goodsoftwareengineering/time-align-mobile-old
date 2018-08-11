(ns time-align-mobile.handlers
  (:require
    [re-frame.core :refer [reg-event-db ->interceptor reg-event-fx]]
    [zprint.core :refer [zprint]]
    [cljs.reader :refer [read-string]]
    [clojure.spec.alpha :as s]
    [time-align-mobile.db :as db :refer [app-db app-db-spec]]
    [time-align-mobile.js-imports :refer [alert]]
    [com.rpl.specter :as sp :refer-macros [select select-one setval transform]]))

;; -- Interceptors ----------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/develop/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain spec db)]
      (println (zprint explain-data {:map {:force-nl? true}}))
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

;; -- Handlers --------------------------------------------------------------

(defn navigate-to [{:keys [db]} [_ {:keys [current-screen params]}]]
  (merge {:db (assoc-in db [:navigation] {:current-screen current-screen
                                          :params         params})}
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
                                                  {:map {:force-nl? true}}))})]
    (assoc-in db [:forms :filter-form] filter-form)))

(defn update-filter-form [db [_ filter-form]]
  (transform [:forms :filter-form] #(merge % filter-form) db))

(defn save-filter-form [{:keys [db]} [_ date-time]]
  (let [filter-form (get-in db [:forms :filter-form])]
    (try
      (let [new-predicates {:predicates (read-string (:predicates filter-form))}
            new-filter        (-> filter-form
                                  (merge {:last-edited date-time}
                                         new-predicates))
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


(reg-event-db :initialize-db [validate-spec] (fn [_ _] app-db))
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



