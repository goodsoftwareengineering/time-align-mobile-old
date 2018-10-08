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

(defn top-bar [{:keys [top-bar-height dimensions]}]
  [view {:style {:height top-bar-height
                 :width (:width @dimensions)
                 :background-color "green"
                 :justify-content "center"
                 :align-items "center"}}
   [text {:style {:justify-content "center"
                  :align-items "center"}} (str (js/Date.))]])

(defn root [params]
  (let [dimensions     (r/atom {:width nil :height nil})
        top-bar-height 25]
    (r/create-class
     {:reagent-render
      (fn [params]
        (let [square-width  100
              start         0
              stop          (* 6 60 60 1000) ;; 3 hours in millis
              square-height (-> (- stop start)
                                (/ day-ms) ;; percent of a day - makes it easy to convert to pixels
                                (* (:height @dimensions)))]

          ;; parent view
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
            [view {:id    "this-is-my-node"
                   :key   "this-is-the-key"
                   :style {:top              (-> start
                                                 (/ day-ms)
                                                 (* (:height @dimensions))
                                                 (max 0)
                                                 (min (:height @dimensions)))
                           :left             (-> @dimensions
                                                 (:width)
                                                 (/ 2)
                                                 (- (/ square-width 2)))
                           :width            square-width
                           :height           square-height
                           :border-radius    10
                           :background-color "yellow"}}
             [text (str "starting at " (-> start
                                           (/ 1000)
                                           (/ 60)
                                           (/ 60)
                                           (#(gstring/format "%.2f" %))))]
             [text (str "" (-> square-height
                               (/ (:height @dimensions))
                               (* day-ms)
                               (/ 1000)
                               (/ 60)
                               (/ 60)
                               (#(gstring/format "%.2f" %)))
                        " hours")]]]]))})))
