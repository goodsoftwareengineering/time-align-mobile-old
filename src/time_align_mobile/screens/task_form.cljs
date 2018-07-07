(ns time-align-mobile.screens.task-form
  (:require [time-align-mobile.js-imports :refer [view
                                                  keyboard-aware-scroll-view
                                                  text
                                                  text-input
                                                  platform
                                                  touchable-highlight]]
            [time-align-mobile.components.structured-data :refer [structured-data]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn update-sd
  [x]
  ;; TODO spec this as a function that needs an argument with a structure
  (println "updating ...")
  (println x))

(defn navigate-sd [x]
  (println "navigating ...")
  (println x))

(defn remove-sd [x]
  (println "removing ...")
  (println x))

(defn update-new-map-item-key-sd
  [x]
  (println
   "updating new map item key")
  (println x))

(defn update-new-map-item-type-sd
  [x]
  (println
   "updating new map item type")
  (println x))

(defn add-new-map-item-sd [_]
  (println "adding new map item"))

(defn root [{:keys [task]}]
  (let [task {:id          (random-uuid)
              :label       "Using Time Align"
              :created     (new js/Date 2018 4 28 15 57)
              :last-edited (new js/Date 2018 4 28 15 57)
              :data        {:string                                 "default"
                            :boolean                                true
                            :number                                 1.2
                            :another-number                         555
                            :keyword-as-value                       :keyword-value
                            :map                                    {:string-in-map "key-val"
                                                                     :vec-in-map    [1 2 3 4 5]
                                                                     :map-in-map    {:list-in-map-in-map '("a" "b" "c")}}
                            :vector                                 [1 2 3 "string"]
                            :vector-with-keys                       [:a :b "c"]
                            :map-to-test-keyboard-aware-auto-scroll {:a "a"
                                                                     :b "b"
                                                                     :c "c"
                                                                     :d "d"
                                                                     :e "e"
                                                                     :f "f"
                                                                     :g 6
                                                                     :h "7"
                                                                     :i "i"
                                                                     :j {:letter "j"
                                                                         :index  9}
                                                                     :k "k"
                                                                     :l ["m" "n" "o" "p"]}
                            }
              :color       "#2222aa"
              :periods     nil}
        ;; current-path (subscribe [:get-task-form-structured-data-current-path])
        ]

    [keyboard-aware-scroll-view
     ;; check this for why these options https://stackoverflow.com/questions/45466026/keyboard-aware-scroll-view-android-issue?rq=1
     {:enable-on-android            true
      :enable-auto-automatic-scroll (= (.-OS platform) "ios")}
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
       (structured-data {:current-path             []
                         :data                     (:data task)
                         :update                   update-sd
                         :navigate                 navigate-sd
                         :remove                   remove-sd
                         :new-map-item-key         :new-item-key
                         :new-map-item-type        :string
                         :update-new-map-item-key  update-new-map-item-key-sd
                         :update-new-map-item-type update-new-map-item-type-sd
                         :add-new-map-item         add-new-map-item-sd})]

      ;; :created     ::moment ;; can't edit display date in their time zone
      ;; :last-edited ::moment ;; can't edit display date in their time zone

      ]]))
