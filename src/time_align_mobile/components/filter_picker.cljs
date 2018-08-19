(ns time-align-mobile.components.filter-picker
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  picker
                                                  picker-item
                                                  flat-list
                                                  touchable-highlight]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn filter-items [items active-filter]
  (if (some? active-filter)
    (->> items
         (filter
          (fn [item]
            (->> (:predicates active-filter)
                 (every? (fn [{:keys [negate path value]}]
                         (if negate
                           (not= (get-in item path) value)
                           (= (get-in item path) value))))))))
    items))

(defn filter-picker []
  (let [filters (subscribe [:get-filters])
        selected-filter (subscribe [:get-active-filter])]
    [view {:style {:flex-direction "row"
                   :justify-content "center"
                   :align-items "center"}}
     [text {:style {:color "grey"}} ":filter"]
     [picker {:selected-value  (if-let [id (:id @selected-filter)]
                                 id
                                 "none")
              :style           {:width 250}
              :on-value-change #(dispatch [:update-active-filter (if (= % "none")
                                                                   nil
                                                                   %)])}
      (map (fn [filter] [picker-item {:label (:label filter)
                                      :key (:id filter)
                                      :value (:id filter)}])
           (cons {:label "none" :id "none"} @filters))]]))
