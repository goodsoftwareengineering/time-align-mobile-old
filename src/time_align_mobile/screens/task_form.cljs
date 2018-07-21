(ns time-align-mobile.screens.task-form
  (:require [time-align-mobile.js-imports :refer [view
                                                  keyboard-aware-scroll-view
                                                  text
                                                  text-input
                                                  color-picker
                                                  date-time-picker
                                                  modal
                                                  platform
                                                  touchable-highlight
                                                  format-date]]
            [time-align-mobile.components.structured-data :refer [structured-data]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(def color-modal-visible (r/atom false))
(def created-modal-visible (r/atom false))

(def field-label-style {:color         "grey"
                        :padding-right 5
                        :width 75})

(defn field-label-changeable-style [changes field]
  {:color         (if (contains? @changes field)
                    "blue"
                    "grey")
   :width 45
   :padding-right 5})

(defn id-comp [task-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":id"]
   [text (str (:id @task-form))]])

(defn created-comp [task-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":created"]
   [text (format-date (:created @task-form))]])

(defn last-edited-comp [task-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":last-edited"]
   [text (format-date (:last-edited @task-form))]])

(defn periods-comp [task-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":periods"]
   [touchable-highlight
    {:on-press #(println "navigate to periods list with filter")}
    [text (str (count (:periods @task-form)))]]])

(defn label-comp [task-form changes]
  [view {:style {:flex-direction "row"
                 :align-items    "center"}}
   [text {:style (field-label-changeable-style changes :label)} ":label"]
   [text-input {:default-value  (:label @task-form)
                :style          {:height 40
                                 :width  200}
                :spell-check    true
                :on-change-text (fn [text]
                                  (dispatch [:update-task-form
                                             {:label text}]))}]])

(defn color-comp [task-form changes]
  [view {:style {:flex-direction "row"
                 :align-items    "center"}}
   [text {:style (field-label-changeable-style changes :color)} ":color"]
   [touchable-highlight {:on-press #(reset! color-modal-visible true)}
    [view {:style {:height 25
                   :width 100
                   :background-color (:color @task-form)}}]]])

(defn data-comp [task-form changes update-structured-data]
  [view {:style {:flex           1
                 :flex-direction "row"
                 :align-items    "flex-start"}}
   [text {:style (field-label-changeable-style changes :data)} ":data"]
   [structured-data {:data   (:data @task-form)
                     :update update-structured-data}]])

(defn root [params]
  (let [task-form              (subscribe [:get-task-form])
        update-structured-data (fn [new-data]
                                 (dispatch
                                  [:update-task-form {:data new-data}]))
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

      [id-comp task-form]

      [created-comp task-form]

      [last-edited-comp task-form]

      [periods-comp task-form]

      [label-comp task-form changes]

      [modal {:animation-type "slide"
              :transparent false
              :visible @color-modal-visible}
       [view {:style {:flex 1}}
        [color-picker {:on-color-selected (fn [color]
                                            (dispatch [:update-task-form {:color color}])
                                            (reset! color-modal-visible false))
                       :old-color (:color @task-form)
                       :style {:flex 1}}]]]

      [color-comp task-form changes]

      [data-comp task-form changes update-structured-data]

      [view {:style {:flex            1
                     :flex-direction  "row"
                     :align-items     "center"
                     :justify-content "center"}}
       [touchable-highlight {:on-press #(dispatch [:save-task-form (new js/Date)])
                             :style    {:padding      5
                                        :margin-right 10}}
        [text "save"]]
       [touchable-highlight {:on-press #(dispatch [:load-task-form (:id @task-form)])}
        [text "cancel"]]]

      ]]))
