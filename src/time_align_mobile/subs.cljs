(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn get-navigation [db _]
  (get-in db [:navigation]))

(reg-sub :get-navigation get-navigation)

(defn get-task-form-structured-data-current-path [db _]
  (get-in db [:view :task-form :structured-data-current-path]))

(reg-sub :get-task-form-structured-data-current-path
         get-task-form-structured-data-current-path)

(defn get-task-in-form [db _]
  (let [task-id (get-in db [:view :task-form :id])
        task    (some #(if (= task-id (:id %)) %) (:tasks db))]
    (if (some? task)
      task
      ;; TODO default task should load from some default settings
      {:id          nil
       :label       "Add new label"
       :created     (new js/Date)
       :last-edited (new js/Date)
       :data        {}
       :color       "#2222aa"
       :periods     nil})))

(reg-sub :get-task-in-form get-task-in-form)


