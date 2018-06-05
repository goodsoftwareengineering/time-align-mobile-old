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
                                                    gesture-handler
                                                    drawer-layout]] ))

(defn alert [title]
  (.alert Alert title))

(defn drawer-list []
  [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
   (->> nav/screens-map
        (filter #(:in-drawer %))
        (sort-by #(:position-drawer %))
        (map (fn [{:keys [icon]}]
               (let [{:keys [family name]} icon
                     params {:name name
                             :key (str "icon-" name)
                             :size 32}]
                 (case family
                   "EvilIcons"     [ei params]
                   "FontAwesome"   [fa params]
                   "IonIcons"      [ic params]
                   "Entypo"        [en params]
                   "MaterialIcons" [mi params])))))])

(defn app-root []
  (let [current-screen (subscribe [:get-current-screen])
        drawer-state (subscribe [:get-drawer-state])]
    (fn []
      [view {:style {:flex 1}}
       [drawer-layout
        {:drawer-width 200
         :drawer-position "left"
         :drawer-type "front"
         :drawer-background-color "#ddd"
         :render-navigation-view (fn [] (r/as-element (drawer-list)))}

        [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
         [text "screen"]]]
       ])))

(defn init []
  (dispatch-sync [:initialize-db])
  (ocall expo "registerRootComponent" (r/reactify-component app-root)))
