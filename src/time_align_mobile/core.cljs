(ns time-align-mobile.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [oops.core :refer [ocall]]
              [time-align-mobile.handlers]
              [time-align-mobile.subs]))

(def ReactNative (js/require "react-native"))

(def expo (js/require "expo"))
(def AtExpo (js/require "@expo/vector-icons"))
(def ionicons (.-Ionicons AtExpo))
(def ic (r/adapt-react-class ionicons))

(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Alert (.-Alert ReactNative))

(def gesture-handler (.-GestureHandler expo))
(def drawer-layout (r/adapt-react-class (.-DrawerLayout gesture-handler)))
(def position-right (.-Right (.-positions (.-DrawerLayout gesture-handler))))

(defn alert [title]
  (.alert Alert title))

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
         :render-navigation-view (fn [] (r/as-element [view
                                                       {:style
                                                        {:flex 1
                                                         :justify-content "center"
                                                         :align-items "center"}}
                                                       [text "in drawer"]]))}

        [view {:style {:flex 1 :justify-content "center" :align-items "center"}}
         [text "screen"]]]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (ocall expo "registerRootComponent" (r/reactify-component app-root)))
