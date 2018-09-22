(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view text]]
            [reagent.core :as r]))

(def dimensions (r/atom {:width nil :height nil}))

(defn root [params]
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
                  :background-color "green"}}]])
