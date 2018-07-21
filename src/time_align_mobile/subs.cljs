(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]
            [zprint.core :refer [zprint]]
            [com.rpl.specter :as sp :refer-macros [select setval transform]]))

(defn get-navigation [db _]
  (get-in db [:navigation]))

(defn get-task-form [db _]
  (let [task-form (get-in db [:view :task-form])]
    (if (some? (:id task-form))
      task-form
      {:id          "nothing"
       :created     (new js/Date 2018 4 28 15 57)
       :last-edited (new js/Date 2018 4 28 15 57)
       :label       "here yet"
       :color       "#323232"
       :data        {:please "wait"}})))

(defn get-task-form-changes [db _]
  (let [task-form (get-in db [:view :task-form])]
    (if (some? (:id task-form))
      (let [task (first
                  (select [:tasks sp/ALL #(= (:id %) (:id task-form))]
                          db))
            ;; data needs to be coerced to compare to form
            new-data (with-out-str (zprint (:data task) {:map {:force-nl? true}}))
            ;; (.stringify js/JSON
            ;;                      (clj->js (:data task))
            ;;                      nil 2)
            altered-task (merge task {:data new-data})
            different-keys (->> (clojure.data/diff task-form altered-task)
                                (first))]
        different-keys)
      ;; return an empty map if there is no loaded task in the form
      {})))

(reg-sub :get-navigation get-navigation)
(reg-sub :get-task-form get-task-form)
(reg-sub :get-task-form-changes get-task-form-changes)


