(ns time-align-mobile.screens.filter-list
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  flat-list
                                                  touchable-highlight]]
            [time-align-mobile.components.filter-picker :refer [filter-picker
                                                                filter-sort]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))


(defn root [params]
  (let [filters (subscribe [:get-filters])
        active-filter (subscribe [:get-active-filter])]
    [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
     [text "Periods"]
     [filter-picker :filter]
     [flat-list {:data (filter-sort @filters @active-filter)
                 :render-item
                 (fn [i]
                   (let [item (:item (js->clj i :keywordize-keys true))
                         {:keys [id label]} item]
                     (r/as-element [touchable-highlight
                                    {:key id
                                     :on-press #(dispatch
                                                 [:navigate-to
                                                  {:current-screen :filter
                                                   :params {:filter-id id}}])}

                                    [view {:style {:flex-direction "row"}}
                                     [view {:style {:flex-direction "column"}}
                                      [text (if (> (count label) 0)
                                              label
                                              "No label")]
                                      [text {:style {:color "grey"}}
                                       (str "id: " id)]]]])))}]]))
