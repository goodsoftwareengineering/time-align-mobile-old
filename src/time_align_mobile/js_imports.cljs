(ns time-align-mobile.js-imports
  (:require [reagent.core :as r :refer [atom]]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]))

(def ReactNative (js/require "react-native"))
(def expo (js/require "expo"))
(def AtExpo (js/require "@expo/vector-icons"))

(def evil-icons (oget AtExpo "EvilIcons"))
(def font-awesome (oget AtExpo "FontAwesome"))
(def ionicons (oget AtExpo "Ionicons"))
(def entypo (oget AtExpo "Entypo"))
(def material-icons (oget AtExpo "MaterialIcons"))

(def ei (r/adapt-react-class evil-icons))
(def fa (r/adapt-react-class font-awesome))
(def ic (r/adapt-react-class ionicons))
(def en (r/adapt-react-class entypo))
(def mi (r/adapt-react-class material-icons))

(def react-native-component-tree (js/require "ReactNativeComponentTree"))
(defn get-instance-from-node [id] (ocall react-native-component-tree "getInstanceFromNode" id))
(def pan-responder (oget ReactNative "PanResponder"))
(def status-bar (r/adapt-react-class (oget ReactNative "StatusBar")))
(def Animated (oget ReactNative "Animated"))
(def animated-xy (oget Animated "ValueXY"))
(def animated-view (r/adapt-react-class (oget Animated "View")))
(def dimensions (oget ReactNative "Dimensions"))
(def text (r/adapt-react-class (oget ReactNative "Text")))
(def view (r/adapt-react-class (oget ReactNative "View")))
(def scroll-view (r/adapt-react-class (oget ReactNative "ScrollView")))
(def image (r/adapt-react-class (oget ReactNative "Image")))
(def flat-list (r/adapt-react-class (oget ReactNative "FlatList")))
(def touchable-highlight (r/adapt-react-class (oget ReactNative "TouchableHighlight")))
(def modal (r/adapt-react-class (oget ReactNative "Modal")))
(def picker (r/adapt-react-class (oget ReactNative "Picker")))
(def picker-item (r/adapt-react-class (oget ReactNative "Picker" "Item")))
(def Alert (oget ReactNative "Alert"))
(defn alert
  ([title]
   (ocall Alert "alert" title))
  ([title subtitle]
   (ocall Alert "alert" title subtitle))
  ([title subtitle options]
   ;; TODO wrap options in a clj->js and camel->kebab thread
   (ocall Alert "alert" title subtitle options)))
(def switch (r/adapt-react-class (oget ReactNative "Switch")))

(def gesture-handler (oget expo "GestureHandler"))
(def drawer-layout (r/adapt-react-class (oget gesture-handler "DrawerLayout")))
;; (def position-left (oget gesture-handler "DrawerLayout" "positions" "Left")) ;; just using the string "left" because that is all this resolves to

(def text-input (r/adapt-react-class (oget ReactNative "TextInput")))

(def kasv (js/require "react-native-keyboard-aware-scroll-view"))
(def keyboard-aware-scroll-view (r/adapt-react-class (oget kasv "KeyboardAwareScrollView")))
(def platform (oget ReactNative "Platform"))

(def react-native-color-picker (js/require "react-native-color-picker"))
(def ColorPicker (oget react-native-color-picker "ColorPicker"))
(def color-picker (r/adapt-react-class ColorPicker))

(def react-native-date-picker (js/require "react-native-modal-datetime-picker"))
(def DatePicker (oget react-native-date-picker "default"))
(def date-time-picker (r/adapt-react-class DatePicker))

(def moment-timezone (js/require "moment-timezone"))
(def moment-tz (oget moment-timezone "tz"))

(defn get-default-timezone []
  (ocall moment-tz "guess"))
(defn set-hour-for-date [date hour zone]
  (-> (moment-tz date zone)
      (ocall "hour" hour)
      (ocall "startOf" "hours")
      js/Date.))
(defn start-of-today [date zone]
  (set-hour-for-date date 0 zone))
(defn end-of-today [date zone]
  (set-hour-for-date date 20 zone)) ;;Set to 20 to avoid straddling the date line
(defn make-date
  ([] (ocall (moment-tz (js/Date.) "UTC") "toDate"))
  ( [year month day]
   (make-date year month day 0))
  ( [year month day hour]
   (make-date year month day hour 0))
  ( [year month day hour minute]
   (make-date year month day hour minute 0))
  ( [year month day hour minute second]
   (make-date year month day hour minute second 0))
  ( [year month day hour minute second millisecond]
   (-> (js/Date. (ocall js/Date "UTC" year (- 1 month) day hour minute second millisecond))
       (moment-tz "UTC"))))
(defn format-date [date]
  (ocall (moment-tz date (get-default-timezone))
         "format"
         "YYYY-MM-DD-HH-mm-ss"))
(defn format-time [date]
  (ocall (moment-tz date (get-default-timezone))
          "format"
          "hh-mm"))
