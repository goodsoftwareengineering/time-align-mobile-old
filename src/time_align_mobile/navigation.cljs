(ns time-align-mobile.navigation
  (:require [time-align-mobile.screens.calendar :as calendar-screen]
            [time-align-mobile.screens.day :as day-screen]
            [time-align-mobile.screens.period-form :as period-form-screen]
            [time-align-mobile.screens.period-list :as period-list-screen]
            [time-align-mobile.screens.queue :as queue-screen]
            [time-align-mobile.screens.report :as report-screen]
            [time-align-mobile.screens.bucket-form :as bucket-form-screen]
            [time-align-mobile.screens.bucket-list :as bucket-list-screen]
            [time-align-mobile.screens.template-form :as template-form-screen]
            [time-align-mobile.screens.template-list :as template-list-screen]))

;; https://expo.github.io/vector-icons/
(def screens-map [{:id              :calendar
                   :label           "calendar"
                   :screen          calendar-screen/root
                   :in-drawer       true
                   :position-drawer 0
                   :icon            {:family "Entypo"
                                     :name   "calendar"}}

                  {:id              :day
                   :label           "day"
                   :screen          day-screen/root
                   :in-drawer       true
                   :position-drawer 1
                   :icon            {:family "FontAwesome"
                                     :name   "columns"}}

                  {:id              :period
                   :label           "period"
                   :screen          period-form-screen/root
                   :in-drawer       false
                   :position-drawer nil
                   :icon            nil}

                  {:id              :periods
                   :label           "periods"
                   :screen          period-list-screen/root
                   :in-drawer       true
                   :position-drawer 2
                   :icon            {:family "Entypo"
                                     :name   "time-slot"}}

                  {:id              :queue
                   :label           "queue"
                   :screen          queue-screen/root
                   :in-drawer       true
                   :position-drawer 3
                   :icon            {:family "MaterialIcons"
                                     :name   "queue"}}

                  {:id              :report
                   :label           "report"
                   :screen          report-screen/root
                   :in-drawer       true
                   :position-drawer 4
                   :icon            {:family "Entypo"
                                     :name   "bar-graph"}}

                  ;; TODO remove this when done testing bucket form
                  {:id              :bucket
                   :label           "bucket"
                   :screen          bucket-form-screen/root
                   :in-drawer       true
                   :position-drawer 999
                   :icon            {:family "Entypo"
                                     :name "credit"}}

                  {:id              :period
                   :label           "period"
                   :screen          period-form-screen/root
                   :in-drawer       true
                   :position-drawer 998
                   :icon            {:family "Entypo"
                                     :name "credit"}}


                  {:id              :buckets
                   :label           "buckets"
                   :screen          bucket-list-screen/root
                   :in-drawer       true
                   :position-drawer 5
                   :icon            {:family "FontAwesome"
                                     :name   "list"}}

                  {:id              :template
                   :label           "template"
                   :screen          template-form-screen/root
                   :in-drawer       false
                   :position-drawer nil
                   :icon            nil}

                  {:id              :templates
                   :label           "templates"
                   :screen          template-list-screen/root
                   :in-drawer       true
                   :position-drawer 6
                   :icon            {:family "FontAwesome"
                                     :name   "wpforms"}}])
