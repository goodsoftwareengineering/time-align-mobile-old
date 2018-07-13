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
  [{:keys [path value]}]
  (dispatch [:update-task-form-structured-data {:path path :value value}]))

(defn navigate-sd [{:keys [new-path]}]
  (dispatch [:update-task-form-structured-data-current-path new-path]))

(defn remove-sd [{:keys [path]}]
  (dispatch [:remove-task-form-structured-data-item path]))

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

(defn update-new-coll-item-key-sd
  [x]
  (println
   "updating new coll item key")
  (println x))

(defn update-new-coll-item-type-sd
  [x]
  (println
   "updating new coll item type")
  (println x))

(defn add-new-coll-item-sd [_]
  (println "adding new coll item"))

(defn root [{:keys [task]}]
  (let [task         (subscribe [:get-task-in-form])
        current-path (subscribe [:get-task-form-structured-data-current-path])]

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
       [text (str (:id @task))]]

      [view {:style {:flex-direction "row"
                     :align-items    "center"}}
       [text {:style {:color         "grey"
                      :padding-right 5}} ":label"]
       [text-input {:default-value  (:label @task)
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
       [touchable-highlight {:on-press #(navigate-sd {:new-path []})}
        [text {:style {:color         "grey"
                       :padding-right 5}} ":data"]]
       (structured-data {:current-path              @current-path
                         :data                      (:data @task)
                         :update                    update-sd
                         :navigate                  navigate-sd
                         :remove                    remove-sd
                         :new-map-item-key          :new-item-key
                         :new-map-item-type         :string
                         :update-new-map-item-key   update-new-map-item-key-sd
                         :update-new-map-item-type  update-new-map-item-type-sd
                         :add-new-map-item          add-new-map-item-sd
                         :update-new-coll-item-type update-new-coll-item-type-sd
                         :add-new-coll-item         add-new-coll-item-sd})]

      ;; :created     ::moment ;; can't edit display date in their time zone
      ;; :last-edited ::moment ;; can't edit display date in their time zone

      ]]))
