(ns time-align-mobile.components.filter-picker
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  picker
                                                  picker-item
                                                  flat-list
                                                  touchable-highlight]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn filter-picker []
  (let [filters (subscribe [:get-filters])
        selected-filter (subscribe [:get-active-filter])]
    [view {:style {:flex-direction "row"
                   :justify-content "center"
                   :align-items "center"}}
     [text {:style {:color "grey"}} ":filter"]
     [picker {:selected-value  (:id @selected-filter)
              :style           {:width 250}
              :on-value-change #(dispatch [:update-active-filter %])}
      (map (fn [filter] [picker-item {:label (:label filter)
                                      :key (:id filter)
                                      :value (:id filter)}])
           @filters)]]))
