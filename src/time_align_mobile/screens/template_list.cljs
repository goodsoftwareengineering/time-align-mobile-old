(ns time-align-mobile.screens.template-list
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  flat-list
                                                  touchable-highlight]]
            [time-align-mobile.components.filter-picker :refer [filter-picker
                                                                filter-sort]]
            [time-align-mobile.components.list-items :as list-items]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn root [params]
  (let [templates     (subscribe [:get-templates])
        active-filter (subscribe [:get-active-filter])]
    [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
     [text "Templates"]
     [filter-picker :template]
     [flat-list {:data (filter-sort @templates @active-filter)
                 :render-item
                 (fn [i]
                   (let [item         (:item (js->clj i :keywordize-keys true))]
                     (r/as-element (list-items/template
                                    (merge
                                     item
                                     {:on-press
                                      #(dispatch
                                        [:navigate-to
                                         {:current-screen :bucket
                                          :params         {:bucket-id (:id item)}}])})))))}]]))
