(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view
                                                  scroll-view
                                                  text
                                                  format-date
                                                  touchable-highlight
                                                  status-bar
                                                  get-instance-from-node
                                                  animated-view
                                                  mi
                                                  animated-xy
                                                  pan-responder]]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [goog.string :as gstring]
            [zprint.core :refer [zprint]]
            [goog.string.format]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]))

;; constants
(def day-ms
  ;; 24 hours in millis
  (* 24 60 60 1000))

(def padding 10)

;; helper functions
(defn get-ms
  "takes a js/date and returns milliseconds since 00:00 that day. Essentially relative ms for the day."
  [date]
  (let [h  (.getHours date)
        m  (.getMinutes date)
        s  (.getSeconds date)
        ms (.getMilliseconds date)]
    (+
     (-> h
         (* 60)
         (* 60)
         (* 1000))
     (-> m
         (* 60)
         (* 1000))
     (-> s (* 1000))
     ms)))

(defn date->y-pos [date-time total-height]
  (-> date-time
      (get-ms)
      (/ day-ms)
      (* total-height)))

(defn duration->height [duration-ms total-height]
  (-> duration-ms
      (/ day-ms)
      (* total-height)))

(defn same-day? [date-a date-b]
  (and (= (.getFullYear date-a)
          (.getFullYear date-b))
       (= (.getMonth date-a)
          (.getMonth date-b))
       (= (.getDate date-a)
          (.getDate date-b))))

;; components
(defn top-bar [{:keys [top-bar-height dimensions]}]
  [view {:style {:height top-bar-height
                 :width (:width @dimensions)
                 :background-color "#b9b9b9"
                 :justify-content "center"
                 :align-items "center"}}
   [text {:style {:justify-content "center"
                  :align-items "center"}} (str (js/Date.))]])

(defn period [{:keys [period dimensions displayed-day]}]
  (let [{:keys [id start stop planned color label bucket-label]} period
        adjusted-stop (if (same-day? displayed-day stop)
                        stop
                        ;; use the end of the day otherwise
                        (js/Date. (.getFullYear displayed-day)
                                  (.getMonth displayed-day)
                                  (.getDate displayed-day)
                                  23
                                  59))
        adjusted-start (if (same-day? displayed-day start)
                         start
                         (js/Date. (.getFullYear displayed-day)
                                   (.getMonth displayed-day)
                                   (.getDate displayed-day)
                                   0
                                   0))]
    [view {:key   id
           :style {:position         "absolute"
                   :top              (-> adjusted-start
                                         (date->y-pos (:height dimensions))
                                         (max 0)
                                         (min (:height dimensions)))
                   :left             (-> dimensions
                                         (:width)
                                         (/ 2)
                                         (#(if planned
                                             padding
                                             (+ % padding) )))
                   :width            (-> dimensions
                                         (:width)
                                         (/ 2)
                                         (- (* 2 padding)))
                   :height           (-> adjusted-stop
                                         (.valueOf)
                                         (- (.valueOf adjusted-start))
                                         (duration->height (:height dimensions)))
                   :border-radius    0
                   :background-color color
                   :opacity          0.5}}
     [touchable-highlight {:style {:width  "100%"
                                   :height "100%"
                                   :padding-left     10
                                   :padding-right    10
                                   :padding-top      0
                                   :padding-bottom   0}
                           :on-press #(dispatch [:select-period id])}
      [view
       [text bucket-label]
       [text label]]]]))

(defn selection-menu-info [dimensions selected-period]
  (let [heading-style    {:background-color "#bfbfbf"}
        info-style       {}
        heading-sub-comp (fn [heading] [text {:style heading-style} heading])
        info-sub-comp    (fn [info] [text {:style info-style} info])]

    [scroll-view {:style {:background-color "white"
                          :width            "100%"
                          :padding-top      10
                          :padding-left     4
                          :max-height       "50%"}}
     [heading-sub-comp "label"]
     [info-sub-comp (:label selected-period)]

     [heading-sub-comp "bucket"]
     [info-sub-comp (:bucket-label selected-period)]

     [heading-sub-comp "start"]
     [info-sub-comp
      (format-date (:start selected-period))]

     [heading-sub-comp "stop"]
     [info-sub-comp
      (format-date (:stop selected-period))]

     [heading-sub-comp "data"]
     [info-sub-comp
      (with-out-str
        (zprint (:data selected-period)
                {:map {:force-nl? true}}))]]))

(defn selection-menu-button [label on-press]
  [touchable-highlight {:on-press on-press
                        :style    {:background-color "#00ffff"
                                   :border-radius    2
                                   :padding          4
                                   :margin           4
                                   :align-self       "flex-start"}}
   [view {:style {:flex-direction "row"
                  :align-items    "center"}}
    [mi {:name  "edit"
         :style {:margin-right 4}}]
    [text label]]])

(defn selection-menu-buttons [dimensions selected-period]
  [view {:style {:background-color "white"
                 :width            "100%"
                 :padding-top      10
                 :padding-left     4
                 :height           "50%"}}

   [selection-menu-button
    "edit"
    #(dispatch [:navigate-to {:current-screen :period
                              :params         {:period-id (:id selected-period)}}])]

   [selection-menu-button
    "cancel"
    #(dispatch [:select-period nil])]

   [selection-menu-button
    "up"
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (- (* 5 60 1000)) ;; five minutes
                                                        (js/Date.))
                                             :stop  (-> selected-period
                                                        (:stop)
                                                        (.valueOf)
                                                        (- (* 5 60 1000))
                                                        (js/Date.))}}])]])

(defn selection-menu-arrow [dimensions selected-period]
  [view {:style {:position            "absolute"
                              :top                 (-> (:start selected-period)
                                                       (date->y-pos (:height dimensions))
                                                       (max 0)
                                                       (min (:height dimensions))
                                                       (- 5))
                              :left                (if (:planned selected-period)
                                                     0
                                                     (-> dimensions
                                                         (:width)
                                                         (/ 2)
                                                         (- 7.5)))
                              :background-color    "transparent"
                              :border-style        "solid"
                              :border-left-width   10
                              :border-right-width  10
                              :border-bottom-width 15
                              :border-left-color   "transparent"
                              :border-right-color  "transparent"
                              :border-bottom-color "red"
                              :transform           (if (:planned selected-period)
                                                     [{:rotate "270deg"}]
                                                     [{:rotate "90deg"}])}}])

(defn selection-menu [dimensions selected-period]
  [view {:style {:position         "absolute"
                 :background-color "blue"
                 :top              0
                 :height           (:height dimensions)
                 :width            (-> dimensions
                                       (:width)
                                       (/ 2)
                                       (+ padding))
                 :left             (-> dimensions
                                       (:width)
                                       (/ 2)
                                       (#(if (:planned selected-period)
                                           (- % padding)
                                           0)))}}

   [view {:style {:height           "100%"
                  :width            (-> dimensions
                                        (:width)
                                        (/ 2)
                                        (+ padding))
                  :background-color "#dcdcdc"}}

                [selection-menu-info dimensions selected-period]

                ;; buttons
                [selection-menu-buttons dimensions selected-period]]

               [selection-menu-arrow dimensions selected-period]])

(defn root [params]
  (let [dimensions      (r/atom {:width nil :height nil})
        top-bar-height  25
        periods         (subscribe [:get-periods])
        displayed-day   (js/Date.)
        selected-period (subscribe [:get-selected-period])]

    (r/create-class
     {:reagent-render
      (fn [params]
        [view {:style     {:flex 1 :justify-content "center" :align-items "center"}
               :on-layout (fn [event]
                            (let [layout (-> event
                                             (oget "nativeEvent" "layout")
                                             (js->clj :keywordize-keys true))]
                              (if (nil? (:height dimensions))
                                (reset! dimensions {:width  (:width layout)
                                                    :height (- (:height layout) top-bar-height)}))))}
         ;; make our own status bar
         [status-bar {:hidden true}]
         [top-bar {:top-bar-height top-bar-height
                   :dimensions     dimensions}]

         ;; view that stretches to fill what is left of the screen
         [view {:style {:height           (:height @dimensions) ;; this is already adjusted to accoutn for top-bar
                        :width            (:width @dimensions)
                        :background-color "#dedede"}}

          ;; periods
          (doall (->> @periods
                      (filter (fn [{:keys [start stop]}]
                                (cond (and (some? start) (some? stop))
                                      (or (same-day? displayed-day start)
                                          (same-day? displayed-day stop)))))
                      (map #(period {:period     %
                                     :displayed-day displayed-day
                                     :dimensions @dimensions}))))


          ;; selection menu
          (when (some? @selected-period)
            [selection-menu @dimensions @selected-period])]])})))
