(ns time-align-mobile.components.structured-data
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  text-input
                                                  touchable-highlight
                                                  switch]]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.walk :refer [walk]]))

(defn map-button [current-path k v]
  [touchable-highlight
   {:key  (str (reduce str (into current-path [v])) "map-button")    
    :on-press (fn [_]
                (println (let [new-path (into current-path [k])]
                           {:new-path new-path})))}
   [view
    [text "button here to go to the nested map"]]])

(defn coll-button [current-path k v]
  [touchable-highlight
   {:key      (str (reduce str (into current-path [v])) "coll-button")
    :on-press (fn [_]
                (println (let [new-path (into current-path [k])]
                           {:new-path new-path})))}
   [view
    [text "button here to go to the collection"]]])

(defn number-input [{:keys [v k data current-path update]}]
  [text-input
   {:key (str (reduce str (into current-path [v])) "number-input")
    :default-value  (str v)
    :style          {:height 40
                     :width  200}
    :spell-check    true
    :on-change-text #(update {:path (into  current-path [k])
                             :value (js/parseFloat %)})}])

(defn string-input [{:keys [v k data current-path update]}]
  [text-input
   {:key (str (reduce str (into current-path [v])) "string-input")
    :default-value  v
    :style          {:height 40
                     :width  200}
    :spell-check    true
    :on-change-text #(update {:path (into  current-path [k])
                             :value %})}])

(defn boolean-input [{:keys [v k data current-path update]}]
  [switch {:key (str (reduce str (into current-path [v])) "boolean-input")
           :on-value-change #(update {:path (into  current-path [k])
                                      :value %})
           :value v}])

(defn key-input [{:keys [v k data current-path update]}]
  [text-input
   {:key (str (reduce str (into current-path [v])) "key-input")
    :default-value  (subs (str v) 1)
    :style          {:height 40
                     :color "purple"
                     :width  200}
    :spell-check    true
    :on-change-text #(update {:path (into  current-path [k])
                              :value %})}])

(defn value-element-picker [{:keys [v k data current-path update]}]
  (let [input-args {:data data
                    :current-path current-path
                    :k k
                    :v v
                    :update update}]
    (cond
      (map? v)     (map-button current-path k v)
      (coll? v)    (coll-button current-path k v)
      (number? v)  (number-input input-args)
      (string? v)  (string-input input-args)
      (boolean? v) (boolean-input input-args)
      (keyword? v) (key-input input-args)
      :else        [text {:key (str (reduce str (into current-path [v]))
                                    "-fallback-input")}
                    "not a supported element"])))

(defn breadcrumb-keys-buttons [current-path]
  (map-indexed
   (fn [i k]
     [touchable-highlight
      {:on-press (fn [_]
                   (println {:new-path (into [] (take (+ 1 i) current-path))}))}
      [text {:style {:color         "grey"
                     :padding-right 5}} (str k)]])
   current-path))

(defn map-element [{:keys [current-path data subset update]}]
  [view
   (breadcrumb-keys-buttons current-path)
   (walk (fn [[k v]]
           (let [value-element (value-element-picker {:v v
                                                      :k k
                                                      :data data
                                                      :current-path current-path
                                                      :update update})]

             [view {
                    :style {:flex 1 :flex-direction "row" :align-items "center"}}
              [text {:style {:color         "grey"
                             :padding-right 5}}
               k]
              value-element]))

         (fn [elements] (into [view {:style {:flex 1}}] elements))

         (into [] subset))])

(defn collection-element [{:keys [current-path data subset update]}]
  [view
   (breadcrumb-keys-buttons current-path)
   (map-indexed
    (fn [i v] (value-element-picker {:v v
                                     :k i
                                     :data data
                                     :current-path current-path
                                     :update update}))
    subset)])

(defn structured-data [{:keys [current-path data update]}]
  ;; TODO spec this and all component entry points
  (let [current-path (if (and (some? current-path) (some? (first current-path)))
                       current-path
                       [])
        subset (get-in data current-path data)
        element-arg {:current-path current-path
                     :data data
                     :subset subset
                     :update update}]
    (cond
      (map? subset) (map-element element-arg)
      (coll? subset) (collection-element element-arg)
      :else [view [text "subset isn't a map or collection"]])))

