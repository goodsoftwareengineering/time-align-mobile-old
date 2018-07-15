(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn get-navigation [db _]
  (get-in db [:navigation]))
(reg-sub :get-navigation get-navigation)

(defn get-task-form [db _]
  (let [task-form (get-in db [:view :task-form])]
    (if (some? (:id task-form))
      task-form
      {:id "nothing"
       :label "here yet"
       :data {:please "wait"}})))
(reg-sub :get-task-form get-task-form)


