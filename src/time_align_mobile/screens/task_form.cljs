(ns time-align-mobile.screens.task-form
  (:require [time-align-mobile.js-imports :refer [view
                                                  keyboard-aware-scroll-view
                                                  text
                                                  text-input
                                                  color-picker
                                                  modal
                                                  platform
                                                  touchable-highlight]]
            [time-align-mobile.components.structured-data :refer [structured-data]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(def modal-visible (r/atom false))

(defn root [params]
  (let [task-form              (subscribe [:get-task-form])
        update-structured-data (fn [new-data] (dispatch [:update-task-form {:data new-data}]))
        changes                (subscribe [:get-task-form-changes])]

    [keyboard-aware-scroll-view
     ;; check link for why these options https://stackoverflow.com/questions/45466026/keyboard-aware-scroll-view-android-issue?rq=1
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
       [text (str (:id @task-form))]]


      ;; :created     ::moment ;; can't edit display date in their time zone
      ;; :last-edited ::moment ;; can't edit display date in their time zone

      [modal {:animation-type "slide"
              :transparent false
              :visible @modal-visible}
       [view {:style {:flex 1}}
        [color-picker {:on-color-selected (fn [color]
                                            (dispatch [:update-task-form {:color color}])
                                            (reset! modal-visible false))
                       :old-color (:color @task-form)
                       :style {:flex 1}}]]]
      [view {:style {:flex-direction "row"
                     :align-items    "center"}}
       [text {:style {:color         (if (contains? @changes :label)
                                       "blue"
                                       "grey")
                      :padding-right 5}} ":label"]
       [text-input {:default-value  (:label @task-form)
                    :style          {:height 40
                                     :width  200}
                    :spell-check    true
                    :on-change-text (fn [text] (dispatch [:update-task-form {:label text}]))}]]

      [view {:style {:flex-direction "row"
                     :align-items    "center"}}
       [text {:style {:color         (if (contains? @changes :color)
                                       "blue"
                                       "grey")
                      :padding-right 5}} ":color"]
       [touchable-highlight {:on-press #(reset! modal-visible true)}
        [view {:style {:height 25
                       :width 100
                       :background-color (:color @task-form)}}]]]

      ;; put this https://github.com/instea/react-native-color-picker
      ;; in this https://facebook.github.io/react-native/docs/modal.html

      ;; :periods     (ds/maybe [period-spec])}

      [view {:style {:flex           1
                     :flex-direction "row"
                     :align-items    "flex-start"}}
       [text {:style {:color         (if (contains? @changes :data)
                                       "blue"
                                       "grey")
                      :padding-right 5}} ":data"]
       [structured-data {:data   (:data @task-form)
                         :update update-structured-data}]]


      [view {:style {:flex            1
                     :flex-direction  "row"
                     :align-items     "center"
                     :justify-content "center"}}
       [touchable-highlight {:on-press #(dispatch [:save-task-form])
                             :style    {:padding      5
                                        :margin-right 10}}
        [text "save"]]
       [touchable-highlight {:on-press #(dispatch [:load-task-form (:id @task-form)])}
        [text "cancel"]]]]]))
