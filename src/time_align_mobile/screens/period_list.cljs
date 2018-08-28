(ns time-align-mobile.screens.period-list
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  modal
                                                  flat-list
                                                  touchable-highlight]]
            [time-align-mobile.components.filter-picker :refer [filter-picker
                                                                filter-sort]]
            [time-align-mobile.components.list-buttons :as list-buttons]
            [time-align-mobile.components.list-items :as list-items]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(def modal-visible (atom false))

(defn root [params]
  (let [periods (subscribe [:get-periods])
        buckets (subscribe [:get-buckets])
        active-filter (subscribe [:get-active-filter])]
    [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
     [text "Periods"]
     [filter-picker :period]
     [flat-list {:data (filter-sort @periods @active-filter)
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
                                               :border-radius 25
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
                                       (str "id: " id)]]]])))}]

     [modal {:animation-type "slide"
             :transparent false
             :visible @modal-visible}
      [view {:style {:flex 1
                     :padding 10}}
       [text "Select a bucket to add the period to"]
       [flat-list {:data @buckets
                    :render-item
                    (fn [i]
                      (let [item (:item (js->clj i :keywordize-keys true))]
                        (r/as-element
                         (list-items/bucket
                          (merge
                           item
                           {:on-press
                            (fn [_]
                              (reset! modal-visible false)
                              ;; passing dispatch the parent bucket id
                              ;; for the period about to be created
                              (dispatch [:add-new-period (:id item)]))})))))}]]]

     [list-buttons/root #(reset! modal-visible true)]]))
