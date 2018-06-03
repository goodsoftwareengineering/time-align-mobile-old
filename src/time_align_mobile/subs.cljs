(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-greeting
 (fn [db _]
   (:greeting db)))

(defn get-current-screen [db _]
  (get-in db [:navigation :current-screen]))

(reg-sub
 :current-screen
 (get-current-screen))
