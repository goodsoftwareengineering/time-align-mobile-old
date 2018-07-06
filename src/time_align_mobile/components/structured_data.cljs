(ns time-align-mobile.components.structured-data
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  text-input
                                                  touchable-highlight
                                                  switch
                                                  alert]]
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
   {:key            (str (reduce str (into current-path [v])) "string-input")
    :default-value  v
    :style          {:height 40
                     :width  200}
    :spell-check    true
    :on-change-text #(update {:path  (into  current-path [k])
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

(defn breadcrumb-keys-buttons [current-path navigate]
  (map-indexed
   (fn [i k]
     [touchable-highlight
      {:on-press #(navigate (into [] (take (+ 1 i) current-path)))
       :key (str (into current-path [i k]) "-breadcrumb")}
      [text {:style {:color         "grey"
                     :padding-right 5}} (str k)]])
   current-path))

(defn map-element [{:keys [current-path
                           data
                           subset
                           update
                           navigate
                           remove
                           new-map-item-key
                           new-map-item-type
                           update-new-map-item-key
                           update-new-map-item-type
                           add-new-map-item]}]
  [view
   (breadcrumb-keys-buttons current-path navigate)
   (walk (fn [[k v]]
           (let [value-element (value-element-picker {:v            v
                                                      :k            k
                                                      :data         data
                                                      :current-path current-path
                                                      :update       update})]

             [view {:style {:flex-direction "row" :align-items "center"}
                    :key   (str (reduce str (into current-path [k v]))
                                "map-key-value")}
              [touchable-highlight {:on-long-press
                                    (fn [_]
                                      (alert
                                       "Do you want to delete this?"
                                       (str "key: " k " value: " v)
                                       (clj->js
                                        [{:text    "Cancel"
                                          :onPress #(println "canceled delete") ;; TODO find a camel to kebab thing to wrap this or do it in js_imports
                                          :style   "cancel"}
                                         {:text    "Delete"
                                          :onPress #(remove
                                                     {:path (into
                                                             current-path
                                                             [k])})}])))}
               
               [text {:style {:color         "grey"
                              :padding-right 5}}
                k]]
              value-element]))

         (fn [elements] (into [view {:style {:flex 1}}] elements))

         (into [] subset))

   [view {:style {:flex-direction "row" :align-items "center"
                  :margin-top 60}}
    [text-input {:style {:color "purple" :margin-right 25}
                 :default-value new-map-item-key
                 :on-change-text update-new-map-item-key}]
    [view {:style {}}
     [touchable-highlight {:on-press #(update-new-map-item-type :string)}
      [text "string"]]
     [touchable-highlight {:on-press #(update-new-map-item-type :map)}
      [text "map"]]
     [touchable-highlight {:on-press #(update-new-map-item-type :coll)}
      [text "coll"]]
     [touchable-highlight {:on-press #(update-new-map-item-type :number)}
      [text "number"]]
     [touchable-highlight {:on-press #(update-new-map-item-type :boolean)}
      [text "boolean"]]
     [touchable-highlight {:on-press #(update-new-map-item-type :keyword)}
      [text "keyword"]]]
    [touchable-highlight {:on-press add-new-map-item
                          :style {:padding-left 30}} [text "add new item"]]]])

(defn collection-element [{:keys [current-path
                                  data subset
                                  update navigate
                                  remove]}]
  [view
   (breadcrumb-keys-buttons current-path navigate)
   (map-indexed
    (fn [i v] [view {:style {:flex-direction "row" :align-items "center"}
                     :key (str (reduce str (into current-path [v]))
                               "collection-value-input")}
               [touchable-highlight {:on-long-press
                                     (fn [_]
                                       (alert
                                        "Do you want to delete this?"
                                        (str "value: " v)
                                        (clj->js
                                         [{:text "Cancel"
                                           :onPress #(println "canceled delete")
                                           :style "cancel"}
                                          {:text "Delete"
                                           :onPress #(remove
                                                      {:path (into
                                                              current-path
                                                              [i])})}])))}
                [text {:style {:color         "grey"
                               :padding-right 5}}
                 (str "[ " i " ]")]]
               (value-element-picker {:v            v
                                      :k            i
                                      :data         data
                                      :current-path current-path
                                      :update       update})])
    subset)])

(defn structured-data [{:keys [current-path
                               data
                               update
                               new-map-item-key
                               new-map-item-type
                               update-new-map-item-key
                               update-new-map-item-type
                               add-new-map-item
                               navigate
                               remove]}]
  ;; TODO spec this and all component entry points to get rid of what is below
  (let [current-path (if (and (some? current-path) (some? (first current-path)))
                       current-path
                       [])
        subset       (get-in data current-path data)
        element-arg  {:current-path current-path
                      :data         data
                      :subset       subset
                      :update       update
                      :navigate     navigate
                      :remove       remove
                      :new-map-item-key new-map-item-key
                      :new-map-item-type new-map-item-type
                      :update-new-map-item-key update-new-map-item-key
                      :update-new-map-item-type update-new-map-item-type
                      :add-new-map-item add-new-map-item}]
    (cond
      (map? subset)  (map-element element-arg)
      (coll? subset) (collection-element element-arg)
      :else          [view [text "subset isn't a map or collection"]])))

