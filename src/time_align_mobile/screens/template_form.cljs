(ns time-align-mobile.screens.template-form
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
                                                  picker
                                                  picker-item
                                                  touchable-highlight
                                                  format-time
                                                  format-date]]
            [time-align-mobile.components.form-buttons :as form-buttons]
            [time-align-mobile.components.structured-data :refer [structured-data]]
            [reagent.core :as r :refer [atom]]
            [time-align-mobile.styles :refer [field-label-changeable-style
                                              field-label-style]]))

(def start-modal-visible (r/atom false))

(def stop-modal-visible (r/atom false))

(defn id-comp [template-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":id"]
   [text (str (:id @template-form))]])

(defn parent-id-comp [template-form changes]
  [view {:style {:flex-direction "row"}}
   [text {:style (field-label-changeable-style changes :bucket-id)}
    ":bucket-id"]
   [text (str (:bucket-id @template-form))]])

(defn parent-picker-comp [template-form changes buckets]
  [view {:style {:flex-direction "row"
                 :align-items "center"}}
   [text {:style (field-label-changeable-style changes :bucket-label)}
    ":bucket-label"]
   [picker {:selected-value  (:bucket-id @template-form)
            :style           {:width 250}
            :on-value-change #(dispatch [:update-template-form {:bucket-id %}])}
    (map (fn [bucket] [picker-item {:label (:label bucket)
                                    :key (:id bucket)
                                    :value (:id bucket)}])
         @buckets)]])

(defn created-comp [template-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":created"]
   [text (format-date (:created @template-form))]])

(defn last-edited-comp [template-form]
  [view {:style {:flex-direction "row"}}
   [text {:style field-label-style} ":last-edited"]
   [text (format-date (:last-edited @template-form))]])

(defn label-comp [template-form changes]
  [view {:style {:flex-direction "row"
                 :align-items    "center"}}
   [text {:style (field-label-changeable-style changes :label)} ":label"]
   [text-input {:default-value  (:label @template-form)
                :style          {:height 40
                                 :width  200}
                :spell-check    true
                :on-change-text (fn [text]
                                  (dispatch [:update-template-form
                                             {:label text}]))}]])

(defn planned-comp [template-form changes]
  [view {:style {:flex-direction "row"
                 :align-items    "center"}}
   [text {:style (field-label-changeable-style changes :planned)} ":planned"]
   [switch {:value (:planned @template-form)
            :on-value-change #(dispatch [:update-template-form {:planned %}])}]])

(defn start-comp [template-form changes]
  (let [[start-hour start-minute] (:start @template-form)
        std                       (new js/Date)
        start-time                (new js/Date
                                       (.getFullYear std)
                                       (.getMonth std)
                                       (.getDate std)
                                       start-hour
                                       start-minute)]
    [view {:style {:flex-direction "row"}}
     [text {:style (field-label-changeable-style changes :start)} ":start"]
     [touchable-highlight {:on-press #(reset! start-modal-visible true)}
      [text (format-time start-time)]]
     [date-time-picker {:is-visible @start-modal-visible
                        :date       start-time
                        :mode       "time"
                        :on-confirm (fn [d]
                                      (dispatch [:update-template-form {:start {:hour   (.getHour d)
                                                                                :minute (.getMinute d)}}])
                                      (reset! start-modal-visible false))
                        :on-cancel  #(reset! start-modal-visible false)}]]))

(defn stop-comp [template-form changes]
  [view {:style {:flex-direction "row"}}
   [text {:style (field-label-changeable-style changes :stop)} ":stop"]
   [touchable-highlight {:on-press #(reset! stop-modal-visible true)}
    [text (format-date (:stop @template-form))]]
   [date-time-picker {:is-visible @stop-modal-visible
                      :date (:stop @template-form)
                      :mode "datetime"
                      :on-confirm (fn [d]
                                    (dispatch [:update-template-form {:stop d}])
                                    (reset! stop-modal-visible false))
                      :on-cancel #(reset! stop-modal-visible false)}]] )

(defn data-comp [template-form changes update-structured-data]
  [view {:style {:flex           1
                 :flex-direction "row"
                 :align-items    "flex-start"}}
   [text {:style (field-label-changeable-style changes :data)} ":data"]
   [structured-data {:data   (:data @template-form)
                     :update update-structured-data}]])

(defn root [params]
  (let [template-form            (subscribe [:get-template-form])
        update-structured-data (fn [new-data]
                                 (dispatch
                                  [:update-template-form {:data new-data}]))
        changes                (subscribe [:get-template-form-changes])
        buckets                (subscribe [:get-buckets])]
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

      [parent-id-comp template-form changes]

      [parent-picker-comp template-form changes buckets]

      [id-comp template-form]

      [created-comp template-form]

      [last-edited-comp template-form]

      [label-comp template-form changes]

      [planned-comp template-form changes]

      [start-comp template-form changes]

      ;; [stop-comp template-form changes]

      [data-comp template-form changes update-structured-data]

      [form-buttons/root
       #(dispatch [:save-template-form (new js/Date)])
       #(dispatch [:load-template-form (:id @template-form)])]]]))
