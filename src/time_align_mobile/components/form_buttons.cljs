(ns time-align-mobile.components.form-buttons
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  touchable-highlight]]))

(defn root [save-fn cancel-fn]
  [view {:style {:flex            1
                 :flex-direction  "row"
                 :align-items     "center"
                 :justify-content "center"}}
   [touchable-highlight {:on-press save-fn
                         :style    {:padding      5
                                    :margin-right 10}}
    [text "save"]]
   [touchable-highlight {:on-press cancel-fn}
    [text "cancel"]]])
