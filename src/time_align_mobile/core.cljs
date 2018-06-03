(ns time-align-mobile.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [oops.core :refer [ocall]]
              [time-align-mobile.handlers]
              [time-align-mobile.subs]))

(def ReactNative (js/require "react-native"))
(def ReactNativeMaterialUI (js/require "react-native-material-ui"))

(def expo (js/require "expo"))
(def AtExpo (js/require "@expo/vector-icons"))
(def ionicons (.-Ionicons AtExpo))
(def ic (r/adapt-react-class ionicons))

(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Alert (.-Alert ReactNative))

(def subheader (r/adapt-react-class (.-Subheader ReactNativeMaterialUI)))
(def drawer (r/adapt-react-class (.-Drawer ReactNativeMaterialUI)))
(def drawer-section (r/adapt-react-class (.-Section (.-Drawer ReactNativeMaterialUI))))
(def theme-provider (r/adapt-react-class (.-ThemeProvider ReactNativeMaterialUI)))

(defn alert [title]
  (.alert Alert title))

(defn app-root []
  (let [current-screen (subscribe [:get-current-screen])
        drawer-state (subscribe [:get-drawer-state])]
    (fn []
      [theme-provider {:ui-theme {}}
       [view {:style {:flex 1}}
        [drawer
         [drawer-section
          {:items [{:icon "bookmark-border" :value "Notifications"}]
           :divider true}]]]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (ocall expo "registerRootComponent" (r/reactify-component app-root)))
