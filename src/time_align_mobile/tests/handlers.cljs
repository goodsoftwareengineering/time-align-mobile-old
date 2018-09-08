(ns time-align-mobile.tests.handlers
  (:require [cljs.test :as t :refer-macros [deftest is]]
            [time-align-mobile.handlers :as handlers]
            [time-align-mobile.db :as db :refer [app-db]]))

(deftest initialize-db
  (is (= app-db (handlers/initialize-db [] []))))

(deftest navigate-to-bucket-form
  (is (= {:db {:navigation {:current-screen :bucket
                            :params {:bucket-id 12345}}
               :active-filter nil}
          :dispatch [:load-bucket-form 12345]}
         (handlers/navigate-to {:db {}} [[] {:current-screen :bucket
                                             :params {:bucket-id 12345}}]))))

(deftest navigate-to-period-form
  (is (= {:db {:navigation {:current-screen :period
                            :params {:period-id 12345}}
               :active-filter nil}
          :dispatch [:load-period-form 12345]}
         (handlers/navigate-to {:db {}} [[] {:current-screen :period
                                             :params {:period-id 12345}}]))))

(deftest navigate-to-template-form
  (is (= {:db {:navigation {:current-screen :template
                            :params {:template-id 12345}}
               :active-filter nil}
          :dispatch [:load-template-form 12345]}
         (handlers/navigate-to {:db {}} [[] {:current-screen :template
                                             :params {:template-id 12345}}]))))

(deftest navigate-to-filter-form
  (is (= {:db {:navigation {:current-screen :filter
                            :params {:filter-id 12345}}
               :active-filter nil}
          :dispatch [:load-filter-form 12345]}
         (handlers/navigate-to {:db {}} [[] {:current-screen :filter
                                             :params {:filter-id 12345}}]))))

(deftest navigate-to-non-form
  (is (= {:db {:navigation {:current-screen :day
                            :params {}}
               :active-filter nil}}
         (handlers/navigate-to {:db {}} [[] {:current-screen :day
                                             :params {}}]))))

(deftest load-bucket-form
  (is (= {:buckets [{:id 12345
                     :data {}}]
          :forms {:bucket-form {:id 12345
                                :data "{}\n"}}}
         (handlers/load-bucket-form {:buckets [{:id 12345
                                                :data {}}]} [[] 12345]))))

(deftest load-period-form )
(deftest load-filter-form )
(deftest load-template-form )


(deftest add-new-bucket )
(deftest add-new-period )
(deftest add-new-filter )
(deftest add-new-template )
(deftest add-template-period )

(deftest update-bucket-form )
(deftest update-period-form )
(deftest update-filter-form )
(deftest update-template-form )
(deftest update-active-filter )

(deftest delete-bucket )
(deftest delete-period )
(deftest delete-filter )
(deftest delete-template )

(deftest save-bucket-form )
(deftest save-period-form )
(deftest save-filter-form )
(deftest save-template-form )
