(ns time-align-mobile.js-imports
  (:require [reagent.core :as r :refer [atom]]))

(def ReactNative (js/require "react-native"))
(def expo (js/require "expo"))
(def AtExpo (js/require "@expo/vector-icons"))

(def evil-icons (.-EvilIcons AtExpo))
(def font-awesome (.-FontAwesome AtExpo))
(def ionicons (.-Ionicons AtExpo))
(def entypo (.-Entypo AtExpo))
(def material-icons (.-MaterialIcons AtExpo))

(def ei (r/adapt-react-class evil-icons))
(def fa (r/adapt-react-class font-awesome))
(def ic (r/adapt-react-class ionicons))
(def en (r/adapt-react-class entypo))
(def mi (r/adapt-react-class material-icons))

(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Alert (.-Alert ReactNative))
(defn alert
  ([title]
   (.alert Alert title))
  ([title subtitle]
   (.alert Alert title subtitle))
  ([title subtitle options]
   (.alert Alert title subtitle options)))
(def switch (r/adapt-react-class (.-Switch ReactNative)))

(def gesture-handler (.-GestureHandler expo))
(def drawer-layout (r/adapt-react-class (.-DrawerLayout gesture-handler)))
;; (def position-left (.-Left (.-positions (.-DrawerLayout gesture-handler)))) ;; just using the string "left" because that is all this resolves to

(def text-input (r/adapt-react-class (.-TextInput ReactNative)))

;; (def date-library (js/require "react-native-modal-datetime-picker"))
;; (def date-picker-raw-react (.-DateTimePicker date-library))
;; (def date-time-picker (r/adapt-react-class date-picker-raw-react))
