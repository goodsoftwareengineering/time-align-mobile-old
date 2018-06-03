(ns time-align-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn get-current-screen [db _]
  (get-in db [:navigation :current-screen]))

(reg-sub
 :get-current-screen
 get-current-screen)




