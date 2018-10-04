(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  touchable-highlight
                                                  animated-view
                                                  animated-xy
                                                  pan-responder]]
            [reagent.core :as r]))

(defn root [params]
  (let [dimensions (r/atom {:width nil :height nil})
        pan        (r/atom (new animated-xy))]
    (r/create-class
     {:component-will-mount
      (fn [] (println "I will mount"))

      :reagent-render
      (fn [params]
        [view {:style     {:flex 1 :justify-content "center" :align-items "center"}
               :on-layout (fn [event]
                            (let [layout (-> event
                                             (.-nativeEvent)
                                             (.-layout)
                                             (js->clj :keywordize-keys true))]
                              (if (nil? (:height dimensions))
                                (reset! dimensions {:width  (:width layout)
                                                    :height (:height layout)}))))}

         [view {:style {:height           (:height @dimensions)
                        :width            (:width @dimensions)
                        :background-color "blue"}}
          [touchable-highlight
           {:on-press (fn [e]
                        (println e)
                        ;; (let [synthetic-event (js->clj e :keywordize-keys true)]
                        ;;   (println synthetic-event))
                        )
            :style    {:top              (-> @dimensions
                                              (:height)
                                              (/ 2)
                                              (- 25))
                       :left             (-> @dimensions
                                             (:width)
                                             (/ 2)
                                             (- 25))
                       :width            50
                       :height           50}}
           [view {:style {:width "100%"
                          :height "100%"
                          :border-radius    10
                          :background-color "green"}}]]]])})))
