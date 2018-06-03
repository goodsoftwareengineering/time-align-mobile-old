(ns time-align-mobile.screens.calendar
  (:require [time-align-mobile.js-imports :refer [view text]] ))

(defn root []
  (fn [ ] [view [text "calendar"]]))
