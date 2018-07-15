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
  [view {}
   [text-input {:style {:width 300
                        :padding-bottom 10}
                :multiline true
                :value 
                :editable true
                :on-change-text update}]])
;; TODO going to need this soon
;; (try
;;   (let [new-data (js->clj
;;                   (.parse js/JSON new-val)
;;                   :keywordize-keys true)]
;;     (update new-data))
;;   (catch js/Error e
;;     ;; TODO alert here
;;     (println "would alert here")))
;; task-form (merge task {:data (.stringify js/JSON
;;                                          (clj->js (:data task))
;;                                          nil 2)})
