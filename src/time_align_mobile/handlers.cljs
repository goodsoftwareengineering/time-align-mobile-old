(ns time-align-mobile.handlers
  (:require
    [re-frame.core :refer [reg-event-db ->interceptor]]
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

(reg-event-db :initialize-db [validate-spec]
              (fn [_ _]
                app-db))

(defn navigate-to [db [_ {:keys [current-screen params]}]]
  (assoc-in db [:navigation] {:current-screen current-screen
                              :params         params}))
(reg-event-db :navigate-to [validate-spec]
              navigate-to)

(defn update-task-form-structured-data [db [_ {:keys [task-id new-data]}]]
  (let [specter-path [:tasks
                      sp/ALL
                      #(= (:id %) task-id)
                      :data]]
    (setval specter-path new-data db)))
(reg-event-db :update-task-form-structured-data [validate-spec]
              update-task-form-structured-data)
