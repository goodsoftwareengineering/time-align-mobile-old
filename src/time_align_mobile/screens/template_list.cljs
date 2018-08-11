(ns time-align-mobile.screens.template-list
  (:require [time-align-mobile.js-imports :refer [view
                                                  text
                                                  flat-list
                                                  touchable-highlight]]
            [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn root [params]
  (let [templates (subscribe [:get-templates])]
    [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
     [text "filter goes here"]
     [flat-list {:data @templates
                 :render-item
                 (fn [i]
                   (let [item (:item (js->clj i :keywordize-keys true))
                         id (:id item)
                         label (:label item)
                         color (:color item)
                         bucket-label (:bucket-label item)]
                     (r/as-element [touchable-highlight
                                    {:key id
                                     :on-press #(dispatch [:navigate-to
                                                           {:current-screen :template
                                                            :params {:template-id id}}])}
                                    [view {:style {:flex-direction "row"}}
                                     [view
                                      {:style {:width 50
                                               :height 50
                                               :margin-right 20
                                               :background-color color}}]
                                     [view {:style {:flex-direction "column"}}
                                      [text (if (> (count label) 0)
                                              label
                                              "No label")]
                                      [text {:style {:color "grey"}}
                                       (if (> (count bucket-label) 0)
                                         (str "bucket-label: " bucket-label)
                                         "No bucket label")]
                                      [text {:style {:color "grey"}}
                                       (str "id: " id)]]]])))}]
     ;; (->> @templates
     ;;      (map (fn [template]
                 ;; [touchable-highlight {:on-press #(println "navigate to form")}
                 ;;  [text (:label template)]])))
     ]))
