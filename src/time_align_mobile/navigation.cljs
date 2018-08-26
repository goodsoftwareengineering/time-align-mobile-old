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
            [time-align-mobile.screens.template-list :as template-list-screen]
            [time-align-mobile.screens.filter-form :as filter-form-screen]
            [time-align-mobile.screens.filter-list :as filter-list-screen]))

(def bucket {:id              :bucket
             :label           "bucket"
             :screen          bucket-form-screen/root
             :in-drawer       true
             :position-drawer 999
             :icon            {:family "Entypo"
                               :name "credit"}})

(def buckets {:id              :buckets
              :label           "buckets"
              :screen          bucket-list-screen/root
              :in-drawer       true
              :position-drawer 5
              :icon            {:family "FontAwesome"
                                :name   "list"}})

(def period {:id              :period
             :label           "period"
             :screen          period-form-screen/root
             :in-drawer       true
             :position-drawer 998
             :icon            {:family "Entypo"
                               :name "credit"}})

(def periods {:id                  :periods
              :label           "periods"
              :screen          period-list-screen/root
              :in-drawer       true
              :position-drawer 2
              :icon            {:family "Entypo"
                                :name   "time-slot"}})

(def template {:id              :template
               :label           "template"
               :screen          template-form-screen/root
               :in-drawer       true
               :position-drawer 997
               :icon            {:family "Entypo"
                                 :name   "credit"}})

(def templates {:id              :templates
                :label           "templates"
                :screen          template-list-screen/root
                :in-drawer       true
                :position-drawer 6
                :icon            {:family "FontAwesome"
                                  :name   "wpforms"}})

(def _filter {:id              :filter
             :label           "filter"
             :screen          filter-form-screen/root
             :in-drawer       true
             :position-drawer 997
             :icon            {:family "Entypo"
                               :name   "credit"}})

(def _filters {:id             :filters
              :label           "filters"
              :screen          filter-list-screen/root
              :in-drawer       true
              :position-drawer 997
              :icon            {:family "Entypo"
                                :name   "credit"}})

(def day {:id              :day
          :label           "day"
          :screen          day-screen/root
          :in-drawer       true
          :position-drawer 1
          :icon            {:family "FontAwesome"
                            :name   "columns"}})

(def calendar {:id              :calendar
               :label           "calendar"
               :screen          calendar-screen/root
               :in-drawer       true
               :position-drawer 0
               :icon            {:family "Entypo"
                                 :name   "calendar"}})

(def report {:id              :report
             :label           "report"
             :screen          report-screen/root
             :in-drawer       true
             :position-drawer 4
             :icon            {:family "Entypo"
                               :name   "bar-graph"}})

(def queue {:id              :queue
            :label           "queue"
            :screen          queue-screen/root
            :in-drawer       true
            :position-drawer 3
            :icon            {:family "MaterialIcons"
                              :name   "queue"}})

;; https://expo.github.io/vector-icons/
(def screens-map [bucket
                  buckets
                  period
                  periods
                  template
                  templates
                  _filter
                  _filters
                  day
                  calendar
                  report
                  queue])
