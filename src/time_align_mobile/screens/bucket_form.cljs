(ns time-align-mobile.screens.bucket-form
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
            [time-align-mobile.styles :refer [field-label-changeable-style
                                              field-label-style]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(def color-modal-visible (r/atom false))

(defn field-label-changeable-style [changes field]
  {:color         (if (contains? @changes field)
                    "blue"
                    "grey")
   :width 45
   :padding-right 5})

(defn id-comp [bucket-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":id"]
   [text (str (:id @bucket-form))]])

(defn created-comp [bucket-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":created"]
   [text (format-date (:created @bucket-form))]])

(defn last-edited-comp [bucket-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":last-edited"]
   [text (format-date (:last-edited @bucket-form))]])

(defn periods-comp [bucket-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":periods"]
   [touchable-highlight
    {:on-press #(println "navigate to periods list with filter")}
    [text (str (count (:periods @bucket-form)))]]])

(defn label-comp [bucket-form changes]
  [view {:style {:flex-direction "row"
                 :align-items    "center"}}
   [text {:style (field-label-changeable-style changes :label)} ":label"]
   [text-input {:default-value  (:label @bucket-form)
                :style          {:height 40
                                 :width  200}
                :spell-check    true
                :on-change-text (fn [text]
                                  (dispatch [:update-bucket-form
                                             {:label text}]))}]])

(defn color-comp [bucket-form changes]
  [view {:style {:flex-direction "row"
                 :align-items    "center"}}
   [text {:style (field-label-changeable-style changes :color)} ":color"]
   [touchable-highlight {:on-press #(reset! color-modal-visible true)}
    [view {:style {:height 25
                   :width 100
                   :background-color (:color @bucket-form)}}]]])

(defn data-comp [bucket-form changes update-structured-data]
  [view {:style {:flex           1
                 :flex-direction "row"
                 :align-items    "flex-start"}}
   [text {:style (field-label-changeable-style changes :data)} ":data"]
   [structured-data {:data   (:data @bucket-form)
                     :update update-structured-data}]])

(defn root [params]
  (let [bucket-form              (subscribe [:get-bucket-form])
        update-structured-data (fn [new-data]
                                 (dispatch
                                  [:update-bucket-form {:data new-data}]))
        changes                (subscribe [:get-bucket-form-changes])]

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

      [id-comp bucket-form]

      [created-comp bucket-form]

      [last-edited-comp bucket-form]

      [periods-comp bucket-form]

      [label-comp bucket-form changes]

      [modal {:animation-type "slide"
              :transparent false
              :visible @color-modal-visible}
       [view {:style {:flex 1}}
        [color-picker {:on-color-selected (fn [color]
                                            (dispatch [:update-bucket-form {:color color}])
                                            (reset! color-modal-visible false))
                       :old-color (:color @bucket-form)
                       :style {:flex 1}}]]]

      [color-comp bucket-form changes]

      [data-comp bucket-form changes update-structured-data]

      [view {:style {:flex            1
                     :flex-direction  "row"
                     :align-items     "center"
                     :justify-content "center"}}
       [touchable-highlight {:on-press #(dispatch [:save-bucket-form (new js/Date)])
                             :style    {:padding      5
                                        :margin-right 10}}
        [text "save"]]
       [touchable-highlight {:on-press #(dispatch [:load-bucket-form (:id @bucket-form)])}
        [text "cancel"]]]

      ]]))
