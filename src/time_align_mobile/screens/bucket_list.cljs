(ns time-align-mobile.screens.bucket-list
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  flat-list
                                                  touchable-highlight]]
            [time-align-mobile.components.filter-picker :refer [filter-picker
                                                                filter-sort]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn root [params]
  (let [buckets (subscribe [:get-buckets])
        active-filter (subscribe [:get-active-filter])]
    [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
     [text "Buckets"]
     [filter-picker :bucket]
     [flat-list {:data (filter-sort @buckets @active-filter)
                 :render-item
                 (fn [i]
                   (let [item (:item (js->clj i :keywordize-keys true))
                         id (:id item)
                         label (:label item)
                         periods (:periods item)
                         templates (:templates item)
                         color (:color item)]
                     (r/as-element [touchable-highlight
                                    {:key id
                                     :on-press #(dispatch
                                                 [:navigate-to
                                                  {:current-screen :bucket
                                                   :params {:bucket-id id}}])}

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
                                       "periods: " (count periods)]
                                      [text {:style {:color "grey"}}
                                       "templates: " (count templates)]
                                      [text {:style {:color "grey"}}
                                       (str "id: " id)]]]])))}]]))
