(ns time-align-mobile.screens.task-form
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  text-input
                                                  touchable-highlight]]
            [time-align-mobile.components.structured-data :refer [structured-data]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn root [{:keys [task]}]
  (let [task {:id          (random-uuid)
              :label       "Using Time Align"
              :created     (new js/Date 2018 4 28 15 57)
              :last-edited (new js/Date 2018 4 28 15 57)
              :data        {:string           "default"
                            :boolean          true
                            :number           1.2
                            :another-number   555
                            :keyword-as-value :keyword-value
                            :map              {:string-in-map "key-val"
                                               :vec-in-map    [1 2 3 4 5]
                                               :map-in-map    {:list-in-map-in-map '("a" "b" "c")}}
                            :vector           [1 2 3 "string"]
                            :vector-with-keys [:a :b "c"]
                            :list             '(1 2 3 4)}
              :color       "#2222aa"
              :periods     nil}
        ;; current-path (subscribe [:get-task-form-structured-data-current-path])
        ]

    [view {:style {:flex            1
                   :flex-direction  "column"
                   :justify-content "flex-start"
                   :align-items     "flex-start"
                   :padding-top     50
                   :padding-left    10}}
     [view {:style {:flex-direction "row"}}
      [text {:style {:color         "grey"
                     :padding-right 5}} ":id"]
      [text (str (:id task))]]

     [view {:style {:flex-direction "row"
                    :align-items    "center"}}
      [text {:style {:color         "grey"
                     :padding-right 5}} ":label"]
      [text-input {:default-value  (:label task)
                   :style          {:height 40
                                    :width  200}
                   :spell-check    true
                   :on-change-text (fn [text] (println text))}]]

     ;; :color       ::color
     ;; :periods     (ds/maybe [period-spec])}

     ;; :data        map?
     ;; https://clojuredocs.org/clojure.walk/walk
     [view {:style {:flex           1
                    :flex-direction "row"
                    :align-items    "flex-start"}}
      [touchable-highlight {:on-press (fn [_] (println "set current-path nil"))}
       [text {:style {:color         "grey"
                      :padding-right 5}} ":data"]]
      (structured-data {:current-path [:map]
                        :data         (:data task)
                        :update       (fn [x]
                                        ;; TODO spec this as a function that needs an argument with a structure
                                        (println "updating ...")
                                        (println x))
                        :navigate     (fn [x]
                                        (println "navigating ...")
                                        (println x))
                        :remove       (fn [x]
                                        (println "removing ...")
                                        (println x))})]

     ;; :created     ::moment ;; can't edit display date in their time zone
     ;; :last-edited ::moment ;; can't edit display date in their time zone

     ]))
