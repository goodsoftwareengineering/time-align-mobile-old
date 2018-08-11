(ns time-align-mobile.screens.filter-form
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
                                                  flat-list
                                                  platform
                                                  picker
                                                  picker-item
                                                  touchable-highlight
                                                  format-time
                                                  format-date]]
            [time-align-mobile.components.form-buttons :as form-buttons]
            [time-align-mobile.components.structured-data :refer [structured-data]]
            [time-align-mobile.components.form-fields :refer [id-comp
                                                              created-comp
                                                              last-edited-comp
                                                              label-comp]]
            [reagent.core :as r :refer [atom]]
            [time-align-mobile.styles :refer [field-label-changeable-style
                                              field-label-style]]))

(defn predicates-comp [form changes update-structured-data]
  [view {:style {:flex           1
                 :flex-direction "row"
                 :align-items    "flex-start"}}
   [text {:style (field-label-changeable-style changes :predicates)}
    ":predicates"]
   [structured-data {:data   (:predicates @form)
                     :update update-structured-data}]])

(defn root [params]
  (let [filter-form            (subscribe [:get-filter-form])
        update-structured-data (fn [new-data]
                                 (dispatch
                                  [:update-filter-form {:predicates new-data}]))
        changes                (subscribe [:get-filter-form-changes])]
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

      [id-comp filter-form]

      [created-comp filter-form]

      [last-edited-comp filter-form]

      [label-comp filter-form changes :update-filter-form]

      [predicates-comp filter-form changes update-structured-data]

      [form-buttons/root
       #(dispatch [:save-filter-form (new js/Date)])
       #(dispatch [:load-filter-form (:id @filter-form)])]]]))
