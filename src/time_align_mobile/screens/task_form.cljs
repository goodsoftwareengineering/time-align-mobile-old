(ns time-align-mobile.screens.task-form
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  text-input
                                                  touchable-highlight
                                                  switch]]
            [clojure.walk :refer [walk]]))

(defn structured-data [current-path data]
  ;; breadcrumbs

  (walk (fn [[k v]]
          (let [value-element
                (cond
                  (map? v) [touchable-highlight
                            {:key      (str "structured-data-field-" k "-" v)
                             :on-press (fn [_]
                                         (println (let [new-path (into [k] current-path)]
                                                    {:new-path new-path})))}
                            [view
                             [text "button here to go to the nested map"]]]

                  (coll? v) [touchable-highlight
                             {:key      (str "structured-data-field-" k "-" v)
                              :on-press (fn [_]
                                          (println (let [new-path (into [k] current-path)]
                                                     {:new-path new-path})))}
                             [view
                              [text "button here to go to the collection"]]]

                  (number? v) [text-input
                               {:default-value  (str v)
                                :style          {:height 40
                                                 :width  200}
                                :spell-check    true
                                :on-change-text (fn [new-v]
                                                  (println (let [new-path (into [k] current-path)
                                                                 new-data (assoc-in data new-path (js/parseFloat new-v))]
                                                             {:new-path new-path
                                                              :new-data new-data})))}]

                  (string? v) [text-input
                               {:default-value  v
                                :style          {:height 40
                                                 :width  200}
                                :spell-check    true
                                :on-change-text (fn [new-v]
                                                  (println (let [new-path (into [k] current-path)
                                                                 new-data (assoc-in data new-path new-v)]
                                                             {:new-path new-path
                                                              :new-data new-data})))}]

                  (boolean? v) [switch {:on-value-change (fn [new-v] (println new-v))
                                        :value v}]

                  :else [text "not a supported element"])]

            [view {:style {:flex 1 :flex-direction "row" :align-items "center"}}
             [text {:style {:color         "grey"
                            :padding-right 5}}
              k]
             value-element]))

        (fn [elements] (into [view {:style {:flex 1}}] elements))

        (into [] (get-in data current-path data))))

(defn root [{:keys [task]}]
  (let [task {:id          (random-uuid)
              :label       "Using Time Align"
              :created     (new js/Date 2018 4 28 15 57)
              :last-edited (new js/Date 2018 4 28 15 57)
              :data        {:string         "default"
                            :boolean        true
                            :number         1.2
                            :another-number 555
                            :map-of-stuff   {:key-key :key-val}
                            :vector         [1 2 3 "string"]
                            :list           '(1 2 3 4)}
              :color       "#2222aa"
              :periods     nil}]

    [view {:style {:flex         1
                   :flex-direction "column"
                   :justify-content "flex-start"
                   :align-items "flex-start"
                   :padding-top  50
                   :padding-left 10}}
     [view {:style {:flex-direction "row"}}
      [text {:style {:color         "grey"
                     :padding-right 5}} ":id"]
      [text (str (:id task))]]

     [view {:style {:flex-direction "row"
                    :align-items    "center"}}
      [text {:style {:color         "grey"
                     :padding-right 5}} ":label"]
      [text-input {:placeholder    (:label task)
                   :style          {:height 40
                                    :width  200}
                   :spell-check    true
                   :on-change-text (fn [text] (println text))}]]

     ;; :color       ::color
     ;; :periods     (ds/maybe [period-spec])}

     ;; :data        map?
     ;; https://clojuredocs.org/clojure.walk/walk
     [view {:style {:flex 1
                    :flex-direction "row"
                    :align-items    "flex-start"}}
      [text {:style {:color         "grey"
                     :padding-right 5}} ":data"]
      (structured-data [] (:data task))]

     ;; :created     ::moment ;; can't edit display date in their time zone
     ;; :last-edited ::moment ;; can't edit display date in their time zone
     ]))
