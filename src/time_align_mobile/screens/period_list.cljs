(ns time-align-mobile.screens.period-list
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  flat-list
                                                  touchable-highlight]]
            [time-align-mobile.components.filter-picker :refer [filter-picker
                                                                filter-items]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn root [params]
  (let [periods (subscribe [:get-periods])
        active-filter (subscribe [:get-active-filter])]
    [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
     [filter-picker]
     [flat-list {:data (filter-items @periods @active-filter)
                 :render-item
                 (fn [i]
                   (let [item (:item (js->clj i :keywordize-keys true))
                         id (:id item)
                         label (:label item)
                         color (:color item)
                         bucket-label (:bucket-label item)]
                     (r/as-element [touchable-highlight
                                    {:key id
                                     :on-press #(dispatch
                                                 [:navigate-to
                                                  {:current-screen :period
                                                   :params {:period-id id}}])}

                                    [view {:style {:flex-direction "row"}}
                                     [view
                                      {:style {:width 50
                                               :height 50
                                               :margin-right 20
                                               :background-color color}}]
                                     [view {:style {:flex-direction "column"}}
                                      [text (if (> (count label) 0)
                                              label
                                              "No label")]
                                      [text {:style {:color "grey"}}
                                       (if (> (count bucket-label) 0)
                                         (str "bucket-label: " bucket-label)
                                         "No bucket label")]
                                      [text {:style {:color "grey"}}
                                       (str "id: " id)]]]])))}]]))
