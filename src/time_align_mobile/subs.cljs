(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn get-navigation [db _]
  (get-in db [:navigation]))

(reg-sub :get-navigation get-navigation)



