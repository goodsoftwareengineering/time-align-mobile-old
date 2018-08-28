(ns time-align-mobile.components.list-items
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  touchable-highlight]]))


(defn bucket [{:keys [id color label periods templates on-press]}]
  [touchable-highlight
   {:key      id
    :on-press on-press}

   [view {:style {:flex-direction "row"}}
    [view
     {:style {:width            50
              :height           50
              :margin-right     20
              :background-color color}}]
    [view {:style {:flex-direction "column"}}
     [text (if (> (count label) 0) label "No label")]
     [text {:style {:color "grey"}} "periods: " (count periods)]
     [text {:style {:color "grey"}} "templates: " (count templates)]
     [text {:style {:color "grey"}} (str "id: " id)]]]])

;; TODO move period, templates, and filters here
