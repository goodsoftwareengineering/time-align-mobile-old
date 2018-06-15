(ns time-align-mobile.screens.task-form
  (:require [time-align-mobile.js-imports :refer [view text text-input]] ))

(defn root [{:keys [task]}]
  (let [task {:id          (random-uuid)
              :label       "Using Time Align"
              :created     (new js/Date 2018 4 28 15 57)
              :last-edited (new js/Date 2018 4 28 15 57)
              :data        {:category :default}
              :color       "#2222aa"
              :periods     nil}]

    [view {:style {:flex 1 :justify-content "center" :align-items "flex-start"
                   :padding-top 50
                   :padding-left 10}}
     [view {:style {:flex 1 :flex-direction "row"}}
      [text {:style {:color "grey"
                     :padding-right 5}} ":id"]
      [text (str (:id task))]]

     [view {:style {:flex 1 :flex-direction "row"}}
      [text {:style {:color "grey"
                     :padding-right 5}} ":label"]
      [text-input {:value (:label task)
                   :editable true}]]
     ;; :label       string? ;; regular spell checking string
     ;; :created     ::moment ;; can't edit display date in their time zone
     ;; :last-edited ::moment ;; can't edit display date in their time zone
     ;; :data        map?
     ;; :color       ::color
     ;; :periods     (ds/maybe [period-spec])}
     ]


    )
  )
