(ns time-align-mobile.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::greeting]))

;; initial state of app-db
(def app-db {:tasks [{:id (uuid)
                      :color ""
                      :complete false
                      :label ""
                      :description ""
                      :data {}
                      :periods [{:id
                                 :start (date-time)
                                 :stop (date-time)
                                 :description ""
                                 :planned false
                                 :data {}}]}]
             :templates [{:id (uuid)
                          :relative-start (time)
                          :relative-stop (time)
                          :duration (integer)
                          :description ""
                          :planned true
                          :data {}}]
             :view {:each-page {:range
                                :filter
                                :structured-data}}
             :navigation {:current-page :keyword}})
