(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  touchable-highlight
                                                  status-bar
                                                  get-instance-from-node
                                                  animated-view
                                                  animated-xy
                                                  pan-responder]]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [goog.string :as gstring]
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
     [text bucket-label]
     [text label]]))

(defn root [params]
  (let [dimensions     (r/atom {:width nil :height nil})
        top-bar-height 25
        periods       (subscribe [:get-periods])
        displayed-day (js/Date.)]

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
                                     :dimensions @dimensions}))))]])})))
