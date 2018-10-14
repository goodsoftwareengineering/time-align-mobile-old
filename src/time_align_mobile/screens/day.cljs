(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view
                                                  scroll-view
                                                  text
                                                  format-date
                                                  touchable-highlight
                                                  status-bar
                                                  get-instance-from-node
                                                  animated-view
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

(defn period [{:keys [period dimensions]}]
  (let [{:keys [id start stop planned color label bucket-label]} period]
    [view {:key   id
           :style {:position         "absolute"
                   :top              (-> start
                                         (date->y-pos (:height dimensions))
                                         (max 0)
                                         (min (:height dimensions)))
                   :left             (-> dimensions
                                         (:width)
                                         (/ 2)
                                         (#(if planned
                                             (+ % padding)
                                             padding)))
                   :width            (-> dimensions
                                         (:width)
                                         (/ 2)
                                         (- (* 2 padding)))
                   :height           (-> stop
                                         (.valueOf)
                                         (- (.valueOf start))
                                         (duration->height (:height dimensions)))
                   :padding-left     10
                   :padding-right    10
                   :padding-top      0
                   :padding-bottom   0
                   :border-radius    0
                   :background-color color
                   :opacity          0.5}}
     [touchable-highlight {:style {:width  "100%"
                                   :height "100%"}
                           :on-press #(dispatch [:select-period id])}
      [view
       [text bucket-label]
       [text label]]]]))

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
                                     :dimensions @dimensions}))))


          ;; info & actions section
          (when (some? @selected-period)
            (let [heading-style    {:background-color "#bfbfbf"}
                  heading-sub-comp (fn [heading] [text {:style heading-style} heading])
                  info-style       {}
                  button           (fn [label action long-action] [touchable-highlight {:on-press      action
                                                                                        :on-long-press long-action
                                                                                        :style         {:background-color "#00ffff"
                                                                                                        :border-radius    2
                                                                                                        :padding          4
                                                                                                        :align-self       "flex-start"}}
                                                                   [text label]])]

              [scroll-view {:style {:position         "absolute"
                                    :background-color "#fefefe"
                                    :top              0
                                    :padding-top      10
                                    :padding-left     4
                                    :height           (:height @dimensions)
                                    :width            (-> @dimensions
                                                          (:width)
                                                          (/ 2))
                                    :left             (-> @dimensions
                                                          (:width)
                                                          (/ 2)
                                                          (#(if (not (:planned @selected-period))
                                                              %
                                                              0)))}}
               [view
                [heading-sub-comp "label"]
                [text {:style info-style} (:label @selected-period)]

                [heading-sub-comp "bucket"]
                [text {:style info-style} (:bucket-label @selected-period)]

                [heading-sub-comp "start"]
                [text {:style info-style}
                 (format-date (:start @selected-period))]

                [heading-sub-comp "stop"]
                [text {:style info-style}
                 (format-date (:stop @selected-period))]

                [heading-sub-comp "data"]
                [text {:style info-style}
                 (with-out-str
                   (zprint (:data @selected-period)
                           {:map {:force-nl? true}}))]]

               [view
                [button "edit" #(dispatch [:navigate-to {:current-screen :period
                                                         :params         {:period-id (:id @selected-period)}}])]]
               ]))]])})))
