(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
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
                        :background-color "blue"}}]])})))
