(ns time-align-mobile.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [oops.core :refer [ocall]]
              [time-align-mobile.handlers]
              [time-align-mobile.subs]
              [time-align-mobile.navigation :as nav]
              [cljs.reader :refer [read-string]]
              [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                                 oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
              [time-align-mobile.js-imports :refer [ReactNative
                                                    expo
                                                    AtExpo
                                                    ei
                                                    en
                                                    fa
                                                    app-state
                                                    ic
                                                    mi
                                                    text
                                                    view
                                                    image
                                                    touchable-highlight
                                                    gesture-handler
                                                    drawer-layout
                                                    secure-store-get!]] ))

(defn drawer-list []
  [view {:style {:flex 1 :justify-content "center" :align-items "flex-start"}}
   (->> nav/screens-map
        (filter #(:in-drawer %))
        (sort-by #(:position-drawer %))
        (map (fn [{:keys [icon label id]}]
               (let [{:keys [family name]} icon
                     params                {:name  name
                                            :style {:margin-right 25
                                                    :width 32}
                                            :size  32}
                     label-element         [text  label]
                     icon-element          (case family
                                             "EvilIcons"     [ei params]
                                             "FontAwesome"   [fa params]
                                             "IonIcons"      [ic params]
                                             "Entypo"        [en params]
                                             "MaterialIcons" [mi params])]

                 [touchable-highlight {:key      (str "icon-" name)
                                       :on-press (fn [_]
                                                   ;; TODO remove bucket id params when done testing

                                                   (dispatch
                                                    [:navigate-to
                                                     {:current-screen id
                                                      :params
                                                      (cond
                                                        (= id :bucket)
                                                        {:bucket-id (uuid "a7396f81-38d4-4d4f-ab19-a7cef18c4ea2")}

                                                        (= id :period)
                                                        {:period-id (uuid "a8404f81-38d4-4d4f-ab19-a7cef18c4531")}

                                                        (= id :template)
                                                        {:template-id (uuid "c52e4f81-38d4-4d4f-ab19-a7cef18c8882")}

                                                        (= id :filter)
                                                        {:filter-id (uuid "bbc34081-38d4-4d4f-ab19-a7cef18c1212")}
                                                        :else            nil)}]))}
                  [view {:flex-direction  "row"
                         :justify-content "flex-start"
                         :align-items     "center"
                         :padding-left    20
                         :width           200}
                   icon-element
                   label-element]]))))])

(defn app-root []
  (fn []
    (let [navigation (subscribe [:get-navigation])]
      (fn []
        [view {:style {:flex             1
                       :background-color "#ffffff"}}
         [drawer-layout
          {:drawer-width            200
           :drawer-position         "left"
           :drawer-type             "front"
           :drawer-background-color "#ddd"
           :render-navigation-view  (fn [] (r/as-element (drawer-list)))}

          (if-let [screen-comp (some #(if (= (:id %) (:current-screen @navigation))
                                        (:screen %))
                                     nav/screens-map)]
            [screen-comp (:params @navigation)]
            [view [text "That screen doesn't exist"]])]]))))

(defn init []
  (dispatch-sync [:initialize-db])
  (secure-store-get!
   "app-db"
   (fn [value]
     (when (some? value)
       (let [app-db (read-string value)]
         (dispatch [:load-db app-db])))))
  ;; Start ticking
  (js/setInterval #(dispatch [:tick (js/Date.)]) 5000)
  (ocall expo "registerRootComponent" (r/reactify-component app-root)))
