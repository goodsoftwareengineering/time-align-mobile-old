(ns time-align-mobile.screens.task-form
  (:require [time-align-mobile.js-imports :refer [view
                                                  keyboard-aware-scroll-view
                                                  text
                                                  text-input
                                                  platform
                                                  touchable-highlight]]
            [time-align-mobile.components.structured-data :refer [structured-data]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn root [{:keys [task]}]
  (let [task (subscribe [:get-task-in-form])
        task-id (:id @task)
        data (:data @task)]

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
       [touchable-highlight {:on-press #(dispatch [:update-task-form-structured-data {:task-id task-id
                                                                                      :new-data {:new "data"}}])}
        [text {:style {:color         "grey"
                       :padding-right 5}} ":data"]]
       [structured-data {:data   data
                         :update (fn [d]
                                   (dispatch [:update-task-form-structured-data {:task-id task-id
                                                                                 :new-data d}]))}]]


      ;; :created     ::moment ;; can't edit display date in their time zone
      ;; :last-edited ::moment ;; can't edit display date in their time zone

      ]]))
