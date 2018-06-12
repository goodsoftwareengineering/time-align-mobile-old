(ns time-align-mobile.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [oops.core :refer [ocall]]
              [time-align-mobile.handlers]
              [time-align-mobile.subs]
              [time-align-mobile.navigation :as nav]
              [time-align-mobile.js-imports :refer [ReactNative
                                                    expo
                                                    AtExpo
                                                    ei
                                                    en
                                                    fa
                                                    ic
                                                    mi
                                                    text
                                                    view
                                                    image
                                                    Alert
                                                    touchable-highlight
                                                    gesture-handler
                                                    drawer-layout]] ))

(defn alert [title]
  (.alert Alert title))

(defn drawer-list []
  [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
   (->> nav/screens-map
        (filter #(:in-drawer %))
        (sort-by #(:position-drawer %))
        (map (fn [{:keys [icon label id]}]
               (let [{:keys [family name]} icon
                     params                {:name name
                                            :size 32}
                     label-element         [text label]
                     icon-element          (case family
                                             "EvilIcons"     [ei params]
                                             "FontAwesome"   [fa params]
                                             "IonIcons"      [ic params]
                                             "Entypo"        [en params]
                                             "MaterialIcons" [mi params])]

                 [touchable-highlight {:key  (str "icon-" name)
                                       :on-press (fn [_]
                                                   (println {:current-screen id
                                                             :params nil})
                                                   (dispatch [:navigate-to {:current-screen id
                                                                            :params nil}]))}
                  [view
                   icon-element
                   label-element]]))))])

(defn app-root []
  (let [navigation (subscribe [:get-navigation])]
    (fn []
      [view {:style {:flex 1}}
       [drawer-layout
        {:drawer-width 200
         :drawer-position "left"
         :drawer-type "front"
         :drawer-background-color "#ddd"
         :render-navigation-view (fn [] (r/as-element (drawer-list)))}

        (if-let [screen-comp (some #(if (= (:id %) (:current-screen @navigation))
                                      (:screen %))
                                   nav/screens-map)]
          (screen-comp (:params @navigation))
          [view [text "nope"]])]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (ocall expo "registerRootComponent" (r/reactify-component app-root)))
