(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  touchable-highlight
                                                  animated-view
                                                  animated-xy
                                                  pan-responder]]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [reagent.core :as r]))

(defn root [params]
  (let [dimensions (r/atom {:width nil :height nil})
        y-pos      (r/atom 0)
        pan        (r/atom nil)]
    (r/create-class
     {:component-will-mount
      (fn []
        (println "Going to mount...")
        (let [pr (ocall pan-responder "create"
                        (clj->js {:onStartShouldSetPanResponder #(do (println "onStartShouldSetPanResponder called") true)
                                  :onMoveShouldSetPanResponder  #(do (println "onMoveShouldSetPanResponder called") true)

                                  :onPanResponderGrant     #(println "onPanResponderGrant called..")
                                  :onPanResponderMove      #(do
                                                              ;; (println (str "onPanResponderMove called.. " (js->clj %2)))
                                                              (println (str "onPanResponderMove called.. " (get (js->clj %2) "moveY")))
                                                              ;; (swap! y-pos (fn [old] (+ old (get (js->clj %2) "moveY"))))
                                                              (reset! y-pos (get (js->clj %2) "moveY"))
                                                              )
                                  :onPanResponderRelease   #(println "onPanResponderRelease called..")
                                  :onPanResponderTerminate #(println "onPanResponderTerminate called..")}))]
          (reset! pan pr)))

      :reagent-render
      (fn [params]
        (let [square-height 100
              square-width  100]
          [view {:style     {:flex 1 :justify-content "center" :align-items "center"}
                 :on-layout (fn [event]
                              (let [layout (-> event
                                               (oget "nativeEvent" "layout")
                                               (js->clj :keywordize-keys true))]
                                (if (nil? (:height dimensions))
                                  (reset! dimensions {:width  (:width layout)
                                                      :height (:height layout)}))))}

           [view {:style {:height           (:height @dimensions)
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
