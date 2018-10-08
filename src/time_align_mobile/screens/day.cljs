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
            [reagent.core :as r]))

(def day-ms
  ;; 24 hours in millis
  (* 24 60 60 1000))

(def padding 10)

(defn top-bar [{:keys [top-bar-height dimensions]}]
  [view {:style {:height top-bar-height
                 :width (:width @dimensions)
                 :background-color "green"
                 :justify-content "center"
                 :align-items "center"}}
   [text {:style {:justify-content "center"
                  :align-items "center"}} (str (js/Date.))]])

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

(defn period [{:keys [period dimensions]}]
  (let [{:keys [id start stop actual]} period]
    [view {:key   id
           :style {:top              (-> start
                                         (date->y-pos (:height dimensions))
                                         (max 0)
                                         (min (:height dimensions)))
                   :left             (-> dimensions
                                         (:width)
                                         (/ 2)
                                         (#(if actual
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
                   :border-radius    10
                   :background-color "yellow"}}
     [text (str "starting at " (-> start
                                   (get-ms)
                                   (/ 1000)
                                   (/ 60)
                                   (/ 60)
                                   (#(gstring/format "%.2f" %))))]
     [text (str "" (-> stop
                       (.valueOf)
                       (- (.valueOf start))
                       (/ 1000)
                       (/ 60)
                       (/ 60)
                       (#(gstring/format "%.2f" %)))
                " hours")]]))

(defn root [params]
  (let [dimensions     (r/atom {:width nil :height nil})
        top-bar-height 25]
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
                        :background-color "blue"}}
          (doall (->> [{:start  (js/Date. 2018 11 12 5 35)
                        :stop   (js/Date. 2018 11 12 10 0)
                        :id     12345
                        :actual true}
                       {:start  (js/Date. 2018 11 12 5 35)
                        :stop   (js/Date. 2018 11 12 10 0)
                        :id     45678
                        :actual false}]
                      (map #(period {:period     %
                                     :dimensions @dimensions}))))]])})))
