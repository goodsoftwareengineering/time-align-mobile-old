(ns time-align-mobile.screens.task-form
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  text-input
                                                  touchable-highlight
                                                  switch]]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.walk :refer [walk]]))

(defn map-button [current-path k v]
  [touchable-highlight
   {:key      (str "structured-data-field-" k "-" v)
    :on-press (fn [_]
                (println (let [new-path (into current-path [k])]
                           {:new-path new-path})))}
   [view
    [text "button here to go to the nested map"]]])

(defn coll-button [current-path k v]
  [touchable-highlight
   {:key      (str "structured-data-field-" k "-" v)
    :on-press (fn [_]
                (println (let [new-path (into current-path [k])]
                           {:new-path new-path})))}
   [view
    [text "button here to go to the collection"]]])

(defn number-input [data current-path k v]
  [text-input
   {:default-value  (str v)
    :style          {:height 40
                     :width  200}
    :spell-check    true
    :on-change-text (fn [new-v]
                      (println (let [new-path (into current-path [k] )
                                     new-data (assoc-in data new-path (js/parseFloat new-v))]
                                 {:new-path new-path
                                  :new-data new-data})))}])

(defn string-input [data current-path k v]
  [text-input
   {:default-value  v
    :style          {:height 40
                     :width  200}
    :spell-check    true
    :on-change-text (fn [new-v]
                      (println (let [new-path (into current-path [k])
                                     new-data (assoc-in data new-path new-v)]
                                 {:new-path new-path
                                  :new-data new-data})))}])

(defn boolean-input [data current-path k v]
  [switch {:on-value-change (fn [new-v] (println new-v))
           :value v}])

(defn key-input [data current-path k v]
  [text-input
   {:default-value  (str v)
    :style          {:height 40
                     :width  200}
    :spell-check    true
    :on-change-text (fn [new-v] ;; TODO this errors on all the inputs
                      (println (let [new-path (into  current-path [k])
                                     new-data (assoc-in data new-path
                                                        (keyword new-v))]
                                 {:new-path new-path
                                  :new-data new-data})))}])

(defn value-element-picker [v k data current-path]
  (cond
    (map? v)     (map-button current-path k v)
    (coll? v)    (coll-button current-path k v)
    (number? v)  (number-input data current-path k v)
    (string? v)  (string-input data current-path k v)
    (boolean? v) (boolean-input data current-path k v)
    (keyword? v) (key-input data current-path k v)
    :else        [text "not a supported element"]))

(defn breadcrumb-keys-buttons [current-path]
  (map-indexed
   (fn [i k]
     [touchable-highlight
      {:on-press (fn [_]
                   (println (str "pressed "
                                 (take (+ 1 i) current-path)
                                 " in collection current path")))}
      [text {:style {:color         "grey"
                     :padding-right 5}} (str k)]])
   current-path))

(defn map-element [current-path data subset]
  [view
   (breadcrumb-keys-buttons current-path)
   (walk (fn [[k v]]
           (let [value-element (value-element-picker v k data current-path)]

             [view {:style {:flex 1 :flex-direction "row" :align-items "center"}}
              [text {:style {:color         "grey"
                             :padding-right 5}}
               k]
              value-element]))

         (fn [elements] (into [view {:style {:flex 1}}] elements))

         (into [] subset))])

(defn collection-element [current-path data subset]
  [view

   (breadcrumb-keys-buttons current-path)

   (map
    (fn [v] (value-element-picker v (last current-path) data current-path))
    subset)])

(defn structured-data [current-path data]
  ;; TODO breadcrumbs

  (let [subset (get-in data current-path data)]

    (cond
      (map? subset) (map-element current-path data subset)

      (coll? subset) (collection-element current-path data subset)

      :else [view [text "subset isn't a map or collection"]])))

(defn root [{:keys [task]}]
  (let [task {:id          (random-uuid)
              :label       "Using Time Align"
              :created     (new js/Date 2018 4 28 15 57)
              :last-edited (new js/Date 2018 4 28 15 57)
              :data        {:string           "default"
                            :boolean          true
                            :number           1.2
                            :another-number   555
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
      (structured-data [:vector-with-keys] (:data task))]

     ;; :created     ::moment ;; can't edit display date in their time zone
     ;; :last-edited ::moment ;; can't edit display date in their time zone
     ]))
