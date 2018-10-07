(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  touchable-highlight
                                                  status-bar
                                                  animated-view
                                                  animated-xy
                                                  pan-responder]]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [reagent.core :as r]))

(defn top-bar [{:keys [top-bar-height dimensions]}]
  [view {:style {:height top-bar-height
                 :width (:width @dimensions)
                 :background-color "green"
                 :justify-content "center"
                 :align-items "center"}}
   [text {:style {:justify-content "center"
                  :align-items "center"}} (str (js/Date.))]])

(defn set-pan-responder [{:keys [pan-responder y-pos top-bar-height pan]}]
  (println "Will mount...")
  (let [pr (ocall pan-responder "create"
                  (clj->js {:onStartShouldSetPanResponder #(do (println "onStartShouldSetPanResponder called") true)
                            :onMoveShouldSetPanResponder  #(do (println "onMoveShouldSetPanResponder called") true)

                            :onPanResponderGrant     #(println "onPanResponderGrant called..")
                            :onPanResponderMove      #(do
                                                        ;; (println (str "onPanResponderMove called.. " (js->clj %2)))
                                                        (println (str "onPanResponderMove called.. " (get (js->clj %2) "moveY")))
                                                        ;; (swap! y-pos (fn [old] (+ old (get (js->clj %2) "moveY"))))
                                                        (reset! y-pos (- (get (js->clj %2) "moveY")
                                                                         top-bar-height)))
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
      #(set-pan-responder {:pan-responder pan-responder
                          :y-pos y-pos
                          :top-bar-height top-bar-height
                          :pan pan})

      :reagent-render
      (fn [params]
        (let [square-height 100
              square-width  100]

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
                     :dimensions dimensions}]

           ;; view that stretches to fill what is left of the screen
           [view {:style {:height           (:height @dimensions) ;; this is already adjusted to accoutn for top-bar
                          :width            (:width @dimensions)
                          :background-color "blue"}}
            [view (merge (js->clj (oget @pan "panHandlers"))
                         {:style {:top              (->> @y-pos
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
