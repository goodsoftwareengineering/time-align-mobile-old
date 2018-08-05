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
(def modal (r/adapt-react-class (.-Modal ReactNative)))
(def picker (r/adapt-react-class (.-Picker ReactNative)))
(def picker-item (r/adapt-react-class (.-Item (.-Picker ReactNative))))
(def Alert (.-Alert ReactNative))
(defn alert
  ([title]
   (.alert Alert title))
  ([title subtitle]
   (.alert Alert title subtitle))
  ([title subtitle options]
   ;; TODO wrap options in a clj->js and camel->kebab thread
   (.alert Alert title subtitle options)))
(def switch (r/adapt-react-class (.-Switch ReactNative)))

(def gesture-handler (.-GestureHandler expo))
(def drawer-layout (r/adapt-react-class (.-DrawerLayout gesture-handler)))
;; (def position-left (.-Left (.-positions (.-DrawerLayout gesture-handler)))) ;; just using the string "left" because that is all this resolves to

(def text-input (r/adapt-react-class (.-TextInput ReactNative)))

;; (def date-library (js/require "react-native-modal-datetime-picker"))
;; (def date-picker-raw-react (.-DateTimePicker date-library))
;; (def date-time-picker (r/adapt-react-class date-picker-raw-react))

(def kasv (js/require "react-native-keyboard-aware-scroll-view"))
(def keyboard-aware-scroll-view (r/adapt-react-class (.-KeyboardAwareScrollView kasv)))
(def platform (.-Platform ReactNative))

(def react-native-color-picker (js/require "react-native-color-picker"))
(def ColorPicker (.-ColorPicker react-native-color-picker))
(def color-picker (r/adapt-react-class ColorPicker))

(def react-native-date-picker (js/require "react-native-modal-datetime-picker"))
(def DatePicker (.-default react-native-date-picker))
(def date-time-picker (r/adapt-react-class DatePicker))

(def moment-tz (.-tz (js/require "moment-timezone")))


(defn get-default-timezone []
  (.guess moment-tz))
(defn set-hour-for-date [date hour zone]
  (-> (moment-tz date zone)
      (.hour hour)
      (.startOf "hours")
      js/Date.))
(defn start-of-today [date zone]
  (set-hour-for-date date 0 zone))
(defn end-of-today [date zone]
  (set-hour-for-date date 20 zone)) ;;Set to 20 to avoid straddling the date line
(defn make-date
  ([] (.toDate (moment-tz (js/Date.) "UTC")))
  ( [year month day]
   (make-date year month day 0))
  ( [year month day hour]
   (make-date year month day hour 0))
  ( [year month day hour minute]
   (make-date year month day hour minute 0))
  ( [year month day hour minute second]
   (make-date year month day hour minute second 0))
  ( [year month day hour minute second millisecond]
   (-> (js/Date. (.UTC js/Date year (- 1 month) day hour minute second millisecond))
       (moment-tz "UTC"))))
(defn format-date [date]
  (.format (moment-tz date (get-default-timezone))
           "YYYY-MM-DD-hh-mm-ss"))
(defn format-time [date]
  (.format (moment-tz date (get-default-timezone))
           "hh-mm"))
