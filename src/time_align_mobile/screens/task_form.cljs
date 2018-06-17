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

     [view {:style {:flex 1
                    :flex-direction "row"
                    :align-items "center"}}
      [text {:style {:color "grey"
                     :padding-right 5}} ":label"]
      [text-input {:placeholder (:label task)
                   :style {:height 40
                           :width 200}
                   :spell-check true
                   :on-change-text (fn [text] (println text))}]]

     ;; :color       ::color
     ;; :periods     (ds/maybe [period-spec])}

     ;; :data        map?
     ;; https://clojuredocs.org/clojure.walk/walk
     ;;c

     ;; :created     ::moment ;; can't edit display date in their time zone
     ;; :last-edited ::moment ;; can't edit display date in their time zone
     ]


    )
  )
