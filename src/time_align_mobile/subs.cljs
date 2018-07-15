(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn get-navigation [db _]
  (get-in db [:navigation]))
(reg-sub :get-navigation get-navigation)

(defn get-task-form [db _]
  (get-in db [:view :task-form]))
(reg-sub :get-task-form get-task-form)


