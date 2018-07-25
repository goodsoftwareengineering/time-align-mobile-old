(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]
            [zprint.core :refer [zprint]]
            [com.rpl.specter :as sp :refer-macros [select select-one setval transform]]))

(defn get-navigation [db _]
  (get-in db [:navigation]))

(defn get-bucket-form [db _]
  (let [bucket-form (get-in db [:view :bucket-form])]
    (if (some? (:id bucket-form))
      bucket-form
      {:id          "nothing"
       :created     (new js/Date 2018 4 28 15 57)
       :last-edited (new js/Date 2018 4 28 15 57)
       :label       "here yet"
       :color       "#323232"
       :data        {:please "wait"}})))

(defn get-bucket-form-changes [db _]
  (let [bucket-form (get-in db [:view :bucket-form])]
    (if (some? (:id bucket-form))
      (let [bucket (first
                  (select [:buckets sp/ALL #(= (:id %) (:id bucket-form))]
                          db))
            ;; data needs to be coerced to compare to form
            new-data (with-out-str (zprint (:data bucket) {:map {:force-nl? true}}))
            ;; (.stringify js/JSON
            ;;                      (clj->js (:data bucket))
            ;;                      nil 2)
            altered-bucket (merge bucket {:data new-data})
            different-keys (->> (clojure.data/diff bucket-form altered-bucket)
                                (first))]
        (if (nil? different-keys)
          {} ;; empty map if no changes
          different-keys))
      ;; return an empty map if there is no loaded bucket in the form
      {})))

(defn get-period-form [db _]
  (let [period-form (get-in db [:view :period-form])]
    (if (some? (:id period-form))
      period-form
      {:id           "nothing"
       :bucket-color "#2222aa"
       :bucket-label "nothing here yet"
       :bucket-id    "nope"
       :created      (new js/Date 2018 4 28 15 57)
       :last-edited  (new js/Date 2018 4 28 15 57)
       :label        "here yet"
       :planned      false
       :start        nil
       :stop         nil
       :data         {:please "wait"}})))

(defn get-period-form-changes [db _]
  (let [period-form (get-in db [:view :period-form])]
    (if (some? (:id period-form))
      (let [[sub-bucket period] (select-one [:buckets sp/ALL
                                (sp/collect-one (sp/submap [:id :color :label]))
                                :periods sp/ALL #(= (:id %) (:id period-form))]
                               db)
            ;; data needs to be coerced to compare to form
            new-data (with-out-str (zprint (:data period) {:map {:force-nl? true}}))
            altered-period (merge period {:data new-data
                                          :bucket-id (:id sub-bucket)
                                          :bucket-label (:label sub-bucket)})
            different-keys (->> (clojure.data/diff period-form altered-period)
                                (first))]
        (if (nil? different-keys)
          {} ;; empty map if no changes
          different-keys))
      ;; return an empty map if there is no loaded period in the form
      {})))

(defn get-buckets [db _]
  (:buckets db))

(reg-sub :get-navigation get-navigation)
(reg-sub :get-bucket-form get-bucket-form)
(reg-sub :get-bucket-form-changes get-bucket-form-changes)
(reg-sub :get-period-form get-period-form)
(reg-sub :get-period-form-changes get-period-form-changes)
(reg-sub :get-buckets get-buckets)


