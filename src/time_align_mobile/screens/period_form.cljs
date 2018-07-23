(ns time-align-mobile.screens.period-form
  (:require [time-align-mobile.js-imports :refer [view text]]
            [re-frame.core :refer [subscribe dispatch]]
            [time-align-mobile.js-imports :refer [view
                                                  keyboard-aware-scroll-view
                                                  text
                                                  text-input
                                                  color-picker
                                                  date-time-picker
                                                  modal
                                                  platform
                                                  touchable-highlight
                                                  format-date]]
            [time-align-mobile.components.form-buttons :as form-buttons]
            [time-align-mobile.styles :refer [field-label-changeable-style
                                              field-label-style]]))

(defn id-comp [period-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":id"]
   [text (str (:id @period-form))]])

(defn parent-id-comp [period-form]
  [view {:style {:flex-direction "row"}}
   [text {:style (merge field-label-style
                        {:width 90})} ":bucket-id"]
   [text (str (:bucket-id @period-form))]])

(defn parent-label-comp [period-form]
  [view {:style {:flex-direction "row"}}
   [text {:style (merge field-label-style
                        {:width 90})} ":bucket-label"]
   [view {:style {:background-color (str (:bucket-color @period-form))
                  :width 25}}]
   [text (str (:bucket-label @period-form))]])

(defn created-comp [period-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":created"]
   [text (format-date (:created @period-form))]])

(defn last-edited-comp [period-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":last-edited"]
   [text (format-date (:last-edited @period-form))]])

(defn label-comp [period-form changes]
  [view {:style {:flex-direction "row"
                 :align-items    "center"}}
   [text {:style (field-label-changeable-style changes :label)} ":label"]
   [text-input {:default-value  (:label @period-form)
                :style          {:height 40
                                 :width  200}
                :spell-check    true
                :on-change-text (fn [text]
                                  (dispatch [:update-period-form
                                             {:label text}]))}]])

(defn root [params]
  (let [period-form (subscribe [:get-period-form])
        update-structured-data (fn [new-data]
                                 (dispatch
                                  [:update-period-form {:data new-data}]))
        changes                (subscribe [:get-period-form-changes])]
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

      [parent-id-comp period-form]

      [parent-label-comp period-form]

      [id-comp period-form]

      [created-comp period-form]

      [last-edited-comp period-form]

      [label-comp period-form changes]

      [form-buttons/root
       #(dispatch [:save-period-form (new js/Date)])
       #(dispatch [:load-period-form (:id @period-form)])]]]))
