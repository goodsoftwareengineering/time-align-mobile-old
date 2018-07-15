(ns time-align-mobile.handlers
  (:require
    [re-frame.core :refer [reg-event-db ->interceptor reg-event-fx]]
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
                                           "JSON validation Failed"
                                           (last (.split (str alert-message)
                                                         ":"))))
              (setval [:effects :alert] sp/NONE context)))))

;; -- Handlers --------------------------------------------------------------

(defn navigate-to [{:keys [db]} [_ {:keys [current-screen params]}]]
  (merge {:db (assoc-in db [:navigation] {:current-screen current-screen
                                          :params         params})}
         (when (= current-screen :task)
           {:dispatch [:load-task-form (:task-id params)]})))

(defn load-task-form [db [_ task-id]]
  ;; TODO is there a more idiomatic way than first of the select?
  ;; Without that the app silently failed with no spec errors thrown
  (let [task (first (select [:tasks sp/ALL #(= (:id %) task-id)] db))
        task-form (merge task {:data (.stringify js/JSON
                                                 (clj->js (:data task))
                                                 nil 2)})]
    (assoc-in db [:view :task-form] task-form)))

(defn update-task-form [db [_ task-form]]
  (transform [:view :task-form] #(merge % task-form) db))

(defn save-task-form [{:keys [db]} _]
  (let [task-form (get-in db [:view :task-form])]
    (try
      ;; TODO need to take into account keywords with spaces
      ;; it seems to work for going in and out of clj but I have a feeling
      ;; it won't when doing filtering
      (let [new-data (js->clj
                      (.parse js/JSON (:data task-form))
                      :keywordize-keys true)
            new-task (merge task-form {:data new-data})
            new-db (setval [:tasks sp/ALL #(= (:id %) (:id new-task))]
                           new-task
                           db)]
        {:db new-db
         ;; load task form so that the data string gets re-formatted prettier
         :dispatch [:load-task-form (:id new-task)]})
      (catch js/Error e
        {:db db
         :alert (str "Failed data json validation " e)}))))

(reg-event-db :initialize-db [validate-spec] (fn [_ _] app-db))
(reg-event-fx :navigate-to [validate-spec] navigate-to)
(reg-event-db :load-task-form [validate-spec] load-task-form)
(reg-event-db :update-task-form [validate-spec] update-task-form)
(reg-event-fx :save-task-form [alert-message validate-spec] save-task-form)

