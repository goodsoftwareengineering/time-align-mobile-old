(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn get-navigation [db _]
  (get-in db [:navigation]))

(reg-sub :get-navigation get-navigation)

(defn get-task-form-structured-data-current-path [db _]
  (get-in db [:view :task-form :structured-data-current-path]))

(reg-sub :get-task-form-structured-data-current-path
         get-task-form-structured-data-current-path)



