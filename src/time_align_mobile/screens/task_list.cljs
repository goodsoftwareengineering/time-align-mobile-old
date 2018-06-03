(ns time-align-mobile.screens.task-list
  (:require [time-align-mobile.js-imports :refer [view text]] ))

(defn root []
  (fn [ ] [view [text "calendar"]]))
