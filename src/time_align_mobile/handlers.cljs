(ns time-align-mobile.handlers
  (:require
    [re-frame.core :refer [reg-event-db ->interceptor reg-event-fx]]
    [clojure.spec.alpha :as s]
    [time-align-mobile.db :as db :refer [app-db app-db-spec]]
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

;; -- Handlers --------------------------------------------------------------

(reg-event-db :initialize-db [validate-spec] (fn [_ _] app-db))

(defn navigate-to [{:keys [db]} [_ {:keys [current-screen params]}]]
  (merge {:db (assoc-in db [:navigation] {:current-screen current-screen
                                          :params         params})}
         (when (= current-screen :task)
           {:dispatch [:load-task-form (:task-id params)]})))
(reg-event-fx :navigate-to [validate-spec] navigate-to)

(defn load-task-form [db [_ task-id]]
  ;; TODO is there a more idiomatic way than first of the select?
  ;; Without that the app silently failed with no spec errors thrown
  (let [task (first (select [:tasks sp/ALL #(= (:id %) task-id)] db))]
    (assoc-in db [:view :task-form] task)))
(reg-event-db :load-task-form [validate-spec] load-task-form)

(defn update-task-form [db [_ task-form]]
  (transform [:view :task-form] #(merge % task-form) db))
(reg-event-db :update-task-form [validate-spec] update-task-form)

(defn save-task-form [db _]
  (let [task-form (get-in db [:view :task-form])]
    (if (some? (:id task-form))
      ;; TODO error message on else
      (transform [:tasks sp/ALL #(= (:id %) (:id task-form))]
                 (fn [task] (merge task task-form))
                 db))))
(reg-event-db :save-task-form [validate-spec] save-task-form)
