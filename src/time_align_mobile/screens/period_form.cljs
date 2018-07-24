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
                                                  switch
                                                  platform
                                                  touchable-highlight
                                                  format-date]]
            [time-align-mobile.components.form-buttons :as form-buttons]
            [time-align-mobile.components.structured-data :refer [structured-data]]
            [reagent.core :as r :refer [atom]]
            [time-align-mobile.styles :refer [field-label-changeable-style
                                              field-label-style]]))

(def start-modal-visible (r/atom false))

(def stop-modal-visible (r/atom false))

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

(defn planned-comp [period-form changes]
  [view {:style {:flex-direction "row"
                 :align-items    "center"}}
   [text {:style (field-label-changeable-style changes :planned)} ":planned"]
   [switch {:value (:planned @period-form)
            :on-value-change #(dispatch [:update-period-form {:planned %}])}]])

(defn start-comp [period-form changes]
  [view {:style {:flex-direction "row"}}
   [text {:style (field-label-changeable-style changes :start)} ":start"]
   [touchable-highlight {:on-press #(reset! start-modal-visible true)}
    [text (format-date (:start @period-form))]]
   [date-time-picker {:is-visible @start-modal-visible
     :date (:start @period-form)
     :mode "datetime"
     :on-confirm (fn [d]
                   (dispatch [:update-period-form {:start d}])
                   (reset! start-modal-visible false))
     :on-cancel #(reset! start-modal-visible false)}]] )

(defn stop-comp [period-form changes]
  [view {:style {:flex-direction "row"}}
   [text {:style (field-label-changeable-style changes :stop)} ":stop"]
   [touchable-highlight {:on-press #(reset! stop-modal-visible true)}
    [text (format-date (:stop @period-form))]]
   [date-time-picker {:is-visible @stop-modal-visible
                      :date (:stop @period-form)
                      :mode "datetime"
                      :on-confirm (fn [d]
                                    (dispatch [:update-period-form {:stop d}])
                                    (reset! stop-modal-visible false))
                      :on-cancel #(reset! stop-modal-visible false)}]] )

(defn data-comp [period-form changes update-structured-data]
  [view {:style {:flex           1
                 :flex-direction "row"
                 :align-items    "flex-start"}}
   [text {:style (field-label-changeable-style changes :data)} ":data"]
   [structured-data {:data   (:data @period-form)
                     :update update-structured-data}]])

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

      [planned-comp period-form changes]

      [start-comp period-form changes]

      [stop-comp period-form changes]

      [data-comp period-form changes update-structured-data]

      [form-buttons/root
       #(dispatch [:save-period-form (new js/Date)])
       #(dispatch [:load-period-form (:id @period-form)])]]]))
