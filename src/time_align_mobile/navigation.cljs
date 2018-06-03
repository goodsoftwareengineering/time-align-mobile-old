(ns time-align-mobile.navigation
  (:require [time-align-mobile.screens.calendar :as calendar-screen]
            [time-align-mobile.screens.day :as day-screen]
            [time-align-mobile.screens.period-form :as period-form-screen]
            [time-align-mobile.screens.period-list :as period-list-screen]
            [time-align-mobile.screens.queue :as queue-screen]
            [time-align-mobile.screens.report :as report-screen]
            [time-align-mobile.screens.task-form :as task-form-screen]
            [time-align-mobile.screens.task-list :as task-list-screen]
            [time-align-mobile.screens.template-form :as template-form-screen]
            [time-align-mobile.screens.template-list :as template-list-screen]))

(def screens-map {:calendar  calendar-screen/root
                  :day       day-screen/root
                  :period    period-form-screen/root
                  :periods   period-list-screen/root
                  :queue     queue-screen/root
                  :report    report-screen/root
                  :task      task-form-screen/root
                  :tasks     task-list-screen/root
                  :templates template-list-screen/root
                  :template  template-form-screen/root})
