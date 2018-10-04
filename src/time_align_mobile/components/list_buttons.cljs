(ns time-align-mobile.components.list-buttons
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  touchable-highlight]]))

(defn root [add-fn]
  [view {:style {:flex            1
                 :flex-direction  "row"
                 :align-items     "center"
                 :justify-content "center"}}
   [touchable-highlight {:on-press add-fn
                         :style    {:padding      5
                                    :margin-right 10}}
    [text "add"]]])
