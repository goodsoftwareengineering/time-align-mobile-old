(ns time-align-mobile.components.structured-data
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  text-input
                                                  touchable-highlight
                                                  switch
                                                  alert]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn structured-data [{:keys [data update]}]
  ;; TODO spec this and all component entry points
  ;; TODO use r/atom to hold the raw string on change
  ;; Use a save button to validate and pass on a clj map and alert otherwise

  (let [original-raw-string (.stringify js/JSON (clj->js data) nil 2)
        raw-string-holder (r/atom original-raw-string)
        comp-update (fn [string] (try
                                   (let [new-data (js->clj
                                                   (.parse js/JSON string)
                                                   :keywordize-keys true)]
                                     (update new-data))
                                   (catch js/Error e
                                     ;; TODO alert here
                                     (println "would alert here"))))]
    [view {}
     [text-input {:style {:width 300
                          :padding-bottom 10}
                  :multiline true
                  :default-value @raw-string-holder
                  :editable true
                  :on-change-text #(reset! raw-string-holder %)}]
     [touchable-highlight {:on-press #(comp-update @raw-string-holder)}
      [text "save"]]
     [touchable-highlight {:on-press #(reset! raw-string-holder original-raw-string)}
      [text "reset"]]]
    )
  )

