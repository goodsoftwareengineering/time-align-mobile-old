(ns time-align-mobile.handlers
  (:require
    [re-frame.core :refer [reg-event-db ->interceptor reg-event-fx]]
    [zprint.core :refer [zprint]]
    [cljs.reader :refer [read-string]]
    [clojure.spec.alpha :as s]
    [time-align-mobile.db :as db :refer [app-db app-db-spec]]
    [time-align-mobile.js-imports :refer [alert]]
    [com.rpl.specter :as sp :refer-macros [select setval transform]]))

;; -- Interceptors ----------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/develop/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (->interceptor
        :id :validate-spec
        :after (fn [context]
                 (let [db (-> context :effects :db)]
                   (check-and-throw app-db-spec db)
                   context)))
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
           {:dispatch [:load-period-form (:period-id params)]})))

(defn load-bucket-form [db [_ bucket-id]]
  ;; TODO is there a more idiomatic way than first of the select?
  ;; Without that the app silently failed with no spec errors thrown
  (let [bucket (first (select [:buckets sp/ALL #(= (:id %) bucket-id)] db))
        bucket-form (merge bucket {:data (with-out-str (zprint (:data bucket) {:map {:force-nl? true}}))
                               ;; (.stringify js/JSON
                               ;;                   (clj->js (:data bucket))
                               ;;                   nil 2)
                               })]
    (assoc-in db [:view :bucket-form] bucket-form)))

(defn update-bucket-form [db [_ bucket-form]]
  (transform [:view :bucket-form] #(merge % bucket-form) db))

(defn save-bucket-form [{:keys [db]} [_ date-time]]
  (let [bucket-form (get-in db [:view :bucket-form])]
    (try
      ;; TODO need to take into account keywords with spaces
      ;; it seems to work for going in and out of clj but I have a feeling
      ;; it won't when doing filtering
      (let [new-data (read-string (:data bucket-form))
            ;; (js->clj
            ;;           (.parse js/JSON (:data bucket-form))
            ;;           :keywordize-keys true)
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
  ;; TODO is there a more idiomatic way than first of the select?
  ;; Without that the app silently failed with no spec errors thrown
  (let [period (first (select [:buckets sp/ALL :periods sp/ALL #(= (:id %) period-id)] db))
        period-form (merge period {:data (with-out-str (zprint (:data period) {:map {:force-nl? true}}))
                               ;; (.stringify js/JSON
                               ;;                   (clj->js (:data period))
                               ;;                   nil 2)
                               })]
    (assoc-in db [:view :period-form] period-form)))

(defn update-period-form [db [_ period-form]]
  (transform [:view :period-form] #(merge % period-form) db))

(defn save-period-form [{:keys [db]} [_ date-time]]
  (let [period-form (get-in db [:view :period-form])]
    (try
      ;; TODO need to take into account keywords with spaces
      ;; it seems to work for going in and out of clj but I have a feeling
      ;; it won't when doing filtering
      (let [new-data (read-string (:data period-form))
            ;; (js->clj
            ;;           (.parse js/JSON (:data period-form))
            ;;           :keywordize-keys true)
            new-period (merge period-form {:data new-data
                                       :last-edited date-time})
            new-db (setval [:buckets sp/ALL :periods sp/ALL #(= (:id %) (:id new-period))]
                           new-period
                           db)]
        {:db new-db
         ;; load period form so that the data string gets re-formatted prettier
         :dispatch [:load-period-form (:id new-period)]})
      (catch js/Error e
        {:db db
         :alert (str "Failed data json validation " e)}))))


(reg-event-db :initialize-db [validate-spec] (fn [_ _] app-db))
(reg-event-fx :navigate-to [validate-spec] navigate-to)
(reg-event-db :load-bucket-form [validate-spec] load-bucket-form)
(reg-event-db :update-bucket-form [validate-spec] update-bucket-form)
(reg-event-fx :save-bucket-form [alert-message validate-spec] save-bucket-form)
(reg-event-db :load-period-form [validate-spec] load-period-form)
(reg-event-db :update-period-form [validate-spec] update-period-form)
(reg-event-fx :save-period-form [alert-message validate-spec] save-period-form)


