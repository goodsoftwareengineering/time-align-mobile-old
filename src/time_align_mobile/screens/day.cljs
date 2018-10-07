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

(defn set-pan-responder [{:keys [pan-responder y-pos top-bar-height pan dimensions]}]
  (println "Will mount...")
  (let [pr (ocall pan-responder "create"
                  (clj->js {:onStartShouldSetPanResponder #(do (println "onStartShouldSetPanResponder called") true)
                            :onMoveShouldSetPanResponder  #(do (println "onMoveShouldSetPanResponder called") true)

                            :onPanResponderGrant     #(println "onPanResponderGrant called..")
                            :onPanResponderMove      #(do
                                                        (let [new-y (- (get (js->clj %2) "moveY")
                                                                       top-bar-height)
                                                              y-ms (-> new-y
                                                                         (/ (:height @dimensions))
                                                                         (* day-ms))
                                                              y-hour (-> y-ms
                                                                         (/ 1000)
                                                                         (/ 60)
                                                                         (/ 60))
                                                              target (oget %1 "target")
                                                              instance (get-instance-from-node target)
                                                              key (oget instance "key")]
                                                          (println key)
                                                          ;; (println y-hour)
                                                          (reset! y-pos new-y)))
                            :onPanResponderRelease   #(println "onPanResponderRelease called..")
                            :onPanResponderTerminate #(println "onPanResponderTerminate called..")}))]
          (reset! pan pr)))

(defn root [params]
  (let [dimensions     (r/atom {:width nil :height nil})
        top-bar-height 25
        y-pos          (r/atom 0)
        pan            (r/atom nil)]
    (r/create-class
     {:component-will-mount
      #(set-pan-responder {:pan-responder  pan-responder
                           :y-pos          y-pos
                           :top-bar-height top-bar-height
                           :pan            pan
                           :dimensions     dimensions})

      :reagent-render
      (fn [params]
        (let [
              square-width  100
              start         0
              stop          (* 3 60 60 1000) ;; 3 hours in millis
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
            [view (merge (js->clj (oget @pan "panHandlers"))
                         {:id "this-is-my-node"
                          :key "this-is-the-key"
                          :style {:top              (->> @y-pos
                                                         (max 0)
                                                         (min (- (:height @dimensions) square-height)))
                                  :left             (-> @dimensions
                                                        (:width)
                                                        (/ 2)
                                                        (- (/ square-width 2)))
                                  :width            square-width
                                  :height           square-height
                                  :border-radius    10
                                  :background-color "yellow"}})]]]))})))
