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
(def theme-provider (r/adapt-react-class (.-ThemeProvider ReactNativeMaterialUI)))

(defn alert [title]
  (.alert Alert title))

(defn app-root []
  (let [current-screen (subscribe [:get-current-screen])]
    (fn []
      [theme-provider {:ui-theme {}}
       [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
        [subheader {:text "material ui"}]
        [image {:source (js/require "./assets/images/cljs.png")
                :style  {:width  200
                         :height 200}}]
        [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} (str @current-screen)]
        [ic {:name "ios-arrow-down" :size 60 :color "green"}]
        [touchable-highlight {:style    {:background-color "#999" :padding 10 :border-radius 5}
                              :on-press #(alert "HELLO!")}
         [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "press me"]]]
       ]
      )))

(defn init []
  (dispatch-sync [:initialize-db])
  (ocall expo "registerRootComponent" (r/reactify-component app-root)))
