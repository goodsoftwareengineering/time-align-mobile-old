(ns time-align-mobile.screens.calendar
  (:require [time-align-mobile.js-imports :refer [view text]] ))

(defn root [params]
  [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
   [text "calendar"]])
