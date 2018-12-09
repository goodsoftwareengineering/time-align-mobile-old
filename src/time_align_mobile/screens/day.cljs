(ns time-align-mobile.screens.day
  (:require [time-align-mobile.js-imports :refer [view
                                                  scroll-view
                                                  text
                                                  flat-list
                                                  format-date
                                                  touchable-highlight
                                                  status-bar
                                                  get-instance-from-node
                                                  animated-view
                                                  mi
                                                  mci
                                                  fa
                                                  modal
                                                  animated-xy
                                                  pan-responder]]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [time-align-mobile.helpers :refer [same-day?]]
            [time-align-mobile.components.list-items :as list-items]
            [goog.string :as gstring]
            [zprint.core :refer [zprint]]
            [goog.string.format]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]))

;; constants
(def day-ms
  ;; 24 hours in millis
  (* 24 60 60 1000))

(def padding 10)

(def play-modal-visible (r/atom false))

;; helper functions
(defn get-ms
  "takes a js/date and returns milliseconds since 00:00 that day. Essentially relative ms for the day."
  [date]
  (let [h  (.getHours date)
        m  (.getMinutes date)
        s  (.getSeconds date)
        ms (.getMilliseconds date)]
    (+
     (-> h
         (* 60)
         (* 60)
         (* 1000))
     (-> m
         (* 60)
         (* 1000))
     (-> s (* 1000))
     ms)))

(defn date->y-pos [date-time total-height]
  (-> date-time
      (get-ms)
      (/ day-ms)
      (* total-height)))

(defn y-pos->ms [y-pos total-height]
  (-> y-pos
      (/ total-height)
      (* day-ms)))

(defn duration->height [duration-ms total-height]
  (-> duration-ms
      (/ day-ms)
      (* total-height)))

(defn bound-start [start day]
  (if (same-day? day start)
    start
    (js/Date. (.getFullYear day)
              (.getMonth day)
              (.getDate day)
              0
              0)))

(defn bound-stop [stop day]
  (if (same-day? day stop)
    stop
    ;; use the end of the day otherwise
    (js/Date. (.getFullYear day)
              (.getMonth day)
              (.getDate day)
              23
              59)))

(defn back-n-days [date n]
  (let [days (.getDate date)
        month (.getMonth date)
        year (.getFullYear date)]
    (js/Date. year month (- days n))))

(defn forward-n-days [date n]
  (let [days (.getDate date)
        month (.getMonth date)
        year (.getFullYear date)]
    (js/Date. year month (+ days n))))

(defn reset-relative-ms [ms date]
  (let [year           (.getFullYear date)
        month          (.getMonth date)
        day            (.getDate date)
        zero-day    (js/Date. year month day 0 0 0)
        zero-day-ms (.valueOf zero-day)]
    (js/Date. (+ zero-day-ms ms))))

;; components
(defn top-bar [{:keys [top-bar-height dimensions displayed-day now]}]
  [view {:style {:height           top-bar-height
                 :width            (:width @dimensions)
                 :background-color "#b9b9b9"
                 :flex-direction   "column"
                 :align-items      "center"}}
   [view {:style {:flex-direction "row"
                  :align-items    "center"
                  :margin-bottom  4}}
    [text (str now)]]
   [view {:style {:flex-direction  "row"
                  :align-items     "center"
                  :justify-content "center"}}
    ;; back
    [touchable-highlight
     {:on-press      #(dispatch [:update-day-time-navigator (back-n-days displayed-day 1)])
      :on-long-press #(dispatch [:update-day-time-navigator (back-n-days displayed-day 7)])}
     [mi {:name "fast-rewind"
          :size 32 }]]

    ;; displayed day
    [view {:style {:justify-content "center"
                   :align-items     "center"
                   :width           "75%"}}
     [text  (.toDateString displayed-day)]]

    ;; forward
    [touchable-highlight
     {:on-press      #(dispatch [:update-day-time-navigator (forward-n-days displayed-day 1)])
      :on-long-press #(dispatch [:update-day-time-navigator (forward-n-days displayed-day 7)])}
     [mi {:name "fast-forward"
          :size 32}]]]])

(defn period [{:keys [period dimensions displayed-day period-in-play selected-period]}]
  (let [{:keys [id start stop planned color label bucket-label]} period

        adjusted-stop   (bound-stop stop displayed-day)
        adjusted-start  (bound-start start displayed-day)
        top             (-> adjusted-start
                            (date->y-pos (:height dimensions))
                            (max 0)
                            (min (:height dimensions)))
        selected-top    (-> top
                            (- padding)
                            (max 0)
                            (min (:height dimensions)))
        left            (-> dimensions
                            (:width)
                            (/ 2)
                            (#(if planned
                                padding
                                (+ % padding) )))
        selected-left   (-> dimensions
                            (:width)
                            (/ 2)
                            (#(if planned
                                0
                                %)))
        width           (-> dimensions
                            (:width)
                            (/ 2)
                            (- (* 2 padding)))
        selected-width  (-> width
                            (+ (* 2 padding)))
        height          (-> adjusted-stop
                            (.valueOf)
                            (- (.valueOf adjusted-start))
                            (duration->height (:height dimensions))
                            ;; max 1 to actually see recently played periods
                            (max 1))
        selected-height (-> height
                            (+ (* 2 padding))
                            (max 1))
        opacity         (if (= id
                               (:id period-in-play))
                          0.9
                          0.5)]

    [view {:key id}

     (when (= id (:id selected-period))
       [view {:style {:position         "absolute"
                      :top              selected-top
                      :left             selected-left
                      :width            selected-width
                      :height           selected-height
                      :background-color "white"}}])

     [view {:style {:position         "absolute"
                    :top              top
                    :left             left
                    :width            width
                    :height           height
                    :border-radius    0
                    :background-color color
                    :opacity          opacity}}

      [touchable-highlight {:style    {:width          "100%"
                                       :height         "100%"
                                       :padding-left   10
                                       :padding-right  10
                                       :padding-top    0
                                       :padding-bottom 0}
                            :on-press #(dispatch [:select-period id])}
       [view
        [text label]
        [text bucket-label]]]]]))

(defn selection-menu-info [dimensions selected-period]
  (let [heading-style    {:background-color "#bfbfbf"}
        info-style       {}
        heading-sub-comp (fn [heading] [text {:style heading-style} heading])
        info-sub-comp    (fn [info] [text {:style info-style} info])]

    [scroll-view {:style {:background-color "white"
                          :width            "100%"
                          :padding-top      10
                          :padding-left     4
                          :max-height       "30%"}}

     [heading-sub-comp "uuid"]
     [info-sub-comp (:id selected-period)]

     [heading-sub-comp "label"]
     [info-sub-comp (:label selected-period)]

     [heading-sub-comp "bucket"]
     [info-sub-comp (:bucket-label selected-period)]

     [heading-sub-comp "start"]
     [info-sub-comp
      (format-date (:start selected-period))]

     [heading-sub-comp "stop"]
     [info-sub-comp
      (format-date (:stop selected-period))]

     [heading-sub-comp "data"]
     [info-sub-comp
      (with-out-str
        (zprint (:data selected-period)
                {:map {:force-nl? true}}))]]))

(defn selection-menu-button [label icon on-press long-press]
  [touchable-highlight {:on-press      on-press
                        :on-long-press long-press
                        :style         {:background-color "#00ffff"
                                        :border-radius    2
                                        :padding          5
                                        :margin           1
                                        :width            "95%"
                                        :align-self       "flex-start"}}
   [view {:style {:flex-direction "row"
                  :align-items    "center"}}
    icon
    [text label]]])

(defn selection-menu-buttons [{:keys [dimensions selected-period period-in-play displayed-day]}]
  [view {:style {:background-color "white"
                 :width            "100%"
                 :padding-top      10
                 :padding-left     padding
                 :height           "100%"
                 :flex-direction   "column"
                 :flex-wrap        "wrap"}}

   [selection-menu-button
    "cancel"
    [mi {:name "cancel"}]
    #(dispatch [:select-period nil])]

   [selection-menu-button
    "edit"
    [mi {:name "edit"}]
    #(dispatch [:navigate-to {:current-screen :period
                              :params         {:period-id (:id selected-period)}}])]

   [selection-menu-button
    "up"
    [mi {:name "arrow-upward"}]
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (- (* 5 60 1000)) ;; five minutes
                                                        (js/Date.))
                                             :stop  (-> selected-period
                                                        (:stop)
                                                        (.valueOf)
                                                        (- (* 5 60 1000))
                                                        (js/Date.))}}])
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (- (* 60 60 1000)) ;; sixty minutes
                                                        (js/Date.))
                                             :stop  (-> selected-period
                                                        (:stop)
                                                        (.valueOf)
                                                        (- (* 60 60 1000))
                                                        (js/Date.))}}])]

   [selection-menu-button
    "down"
    [mi {:name "arrow-downward"}]
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (+ (* 5 60 1000)) ;; five minutes
                                                        (js/Date.))
                                             :stop  (-> selected-period
                                                        (:stop)
                                                        (.valueOf)
                                                        (+ (* 5 60 1000))
                                                        (js/Date.))}}])
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (+ (* 60 60 1000)) ;; sixty minutes
                                                        (js/Date.))
                                             :stop  (-> selected-period
                                                        (:stop)
                                                        (.valueOf)
                                                        (+ (* 60 60 1000))
                                                        (js/Date.))}}])]
   [selection-menu-button
    "start earlier"
    [mci {:name "arrow-expand-up"}]
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (- (* 5 60 1000))
                                                        (js/Date.))}}])
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (- (* 60 60 1000))
                                                        (js/Date.))}}])]

   [selection-menu-button
    "start later"
    [mci {:name "arrow-collapse-down"}]
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (+ (* 5 60 1000))
                                                        (js/Date.))}}])
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (+ (* 60 60 1000))
                                                        (js/Date.))}}])]

   [selection-menu-button
    "stop later"
    [mci {:name "arrow-expand-down"}]
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:stop (-> selected-period
                                                       (:stop)
                                                       (.valueOf)
                                                       (+ (* 5 60 1000))
                                                       (js/Date.))}}])
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:stop (-> selected-period
                                                       (:stop)
                                                       (.valueOf)
                                                       (+ (* 60 60 1000))
                                                       (js/Date.))}}])]

   [selection-menu-button
    "stop earlier"
    [mci {:name "arrow-collapse-up"}]
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:stop (-> selected-period
                                                       (:stop)
                                                       (.valueOf)
                                                       (- (* 5 60 1000))
                                                       (js/Date.))}}])
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:stop (-> selected-period
                                                       (:stop)
                                                       (.valueOf)
                                                       (- (* 60 60 1000))
                                                       (js/Date.))}}])]

   [selection-menu-button
    "copy over"
    [mi {:name "content-copy"}]
    #(dispatch [:add-period {:period    (merge selected-period
                                               {:planned (not (:planned selected-period))
                                                :id      (random-uuid)})
                             :bucket-id (:bucket-id selected-period)}])]

   [selection-menu-button
    "forward a day"
    [mi {:name "fast-forward"}]
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (+ (* 24 60 60 1000))
                                                        (js/Date.))
                                             :stop  (-> selected-period
                                                        (:stop)
                                                        (.valueOf)
                                                        (+ (* 24 60 60 1000))
                                                        (js/Date.))}}])]

   [selection-menu-button
    "back a day"
    [mi {:name "fast-rewind"}]
    #(dispatch [:update-period {:id         (:id selected-period)
                                :update-map {:start (-> selected-period
                                                        (:start)
                                                        (.valueOf)
                                                        (- (* 24 60 60 1000))
                                                        (js/Date.))
                                             :stop  (-> selected-period
                                                        (:stop)
                                                        (.valueOf)
                                                        (- (* 24 60 60 1000))
                                                        (js/Date.))}}])]

   [selection-menu-button
    "copy next day"
    [view
     [mi {:name "content-copy"}]
     [mi {:name "arrow-forward"}]]
    #(dispatch [:add-period {:period    (merge selected-period
                                               {:start (-> selected-period
                                                           (:start)
                                                           (.valueOf)
                                                           (+ (* 24 60 60 1000))
                                                           (js/Date.))
                                                :stop  (-> selected-period
                                                           (:stop)
                                                           (.valueOf)
                                                           (+ (* 24 60 60 1000))
                                                           (js/Date.))
                                                :id    (random-uuid)})
                             :bucket-id (:bucket-id selected-period)}])]

   [selection-menu-button
    "copy previous day"
    [view
     [mi {:name "content-copy"}]
     [mi {:name "arrow-back"}]]
    #(dispatch [:add-period {:period    (merge selected-period
                                               {:start (-> selected-period
                                                           (:start)
                                                           (.valueOf)
                                                           (- (* 24 60 60 1000))
                                                           (js/Date.))
                                                :stop  (-> selected-period
                                                           (:stop)
                                                           (.valueOf)
                                                           (- (* 24 60 60 1000))
                                                           (js/Date.))
                                                :id    (random-uuid)})
                             :bucket-id (:bucket-id selected-period)}])]

   [selection-menu-button
    "select next"
    [mci {:name "arrow-down-drop-circle"}]
    #(dispatch [:select-next-or-prev-period :next])]

   [selection-menu-button
    "select prev"
    [mci {:name  "arrow-down-drop-circle"
          :style {:transform [{:rotate "180deg"}]}}]
    #(dispatch [:select-next-or-prev-period :prev])]

   [selection-menu-button
    "play from"
    [mi {:name "play-circle-outline"}]
    #(dispatch [:play-from-period  {:id           (:id selected-period)
                                    :time-started (js/Date.)
                                    :new-id       (random-uuid)}])]

   (when (some? period-in-play)
     [selection-menu-button
      "stop playing"
      [mi {:name "stop"}]
      #(dispatch [:stop-playing-period])])

   (when (some? period-in-play)
     [selection-menu-button
      "select playing"
      [mi {:name "play-circle-filled"}]
      #(dispatch [:select-period (:id period-in-play)])])

   [selection-menu-button
    "play"
    [mi {:name "play-arrow"}]
    #(reset! play-modal-visible true)]

   (when (not (or (same-day? (:start selected-period) displayed-day)
                  (same-day? (:stop selected-period) displayed-day)))
     [selection-menu-button
      "jump to selected"
      [fa {:name "dot-circle-o"}]
      #(dispatch [:update-day-time-navigator (:start selected-period)])])])

(defn selection-menu-arrow [dimensions selected-period displayed-day]
  (let [adjusted-start             (bound-start (:start selected-period) displayed-day)
        some-part-on-displayed-day (or (same-day? (:start selected-period) displayed-day)
                                       (same-day? (:stop selected-period) displayed-day))]
    (when some-part-on-displayed-day
      [view {:style {:position            "absolute"
                     :top                 (-> adjusted-start
                                              (date->y-pos (:height dimensions))
                                              (max 0)
                                              (min (:height dimensions))
                                              (- 5))
                     :left                (if (:planned selected-period)
                                            0
                                            (-> dimensions
                                                (:width)
                                                (/ 2)
                                                (- 7.5)))
                     :background-color    "transparent"
                     :border-style        "solid"
                     :border-left-width   10
                     :border-right-width  10
                     :border-bottom-width 15
                     :border-left-color   "transparent"
                     :border-right-color  "transparent"
                     :border-bottom-color "red"
                     :transform           (if (:planned selected-period)
                                            [{:rotate "270deg"}]
                                            [{:rotate "90deg"}])}}])))

(defn selection-menu [{:keys [dimensions selected-period displayed-day period-in-play]}]
  [view {:style {:position         "absolute"
                 :background-color "blue"
                 :top              0
                 :height           (:height dimensions)
                 :width            (-> dimensions
                                       (:width)
                                       (/ 2)
                                       (+ padding))
                 :left             (-> dimensions
                                       (:width)
                                       (/ 2)
                                       (#(if (:planned selected-period)
                                           (- % padding)
                                           0)))}}

   [view {:style {:height           "100%"
                  :width            (-> dimensions
                                        (:width)
                                        (/ 2)
                                        (+ padding))
                  :background-color "#dcdcdc"}}

    ;; [selection-menu-info dimensions selected-period]

    ;; buttons
    [selection-menu-buttons {:dimensions      dimensions
                             :selected-period selected-period
                             :displayed-day   displayed-day
                             :period-in-play  period-in-play}]]

   ;; [selection-menu-arrow dimensions selected-period displayed-day]
   ])

(defn root [params]
  (let [dimensions      (r/atom {:width nil :height nil})
        top-bar-height  50
        periods         (subscribe [:get-periods])
        displayed-day   (subscribe [:get-day-time-navigator])
        selected-period (subscribe [:get-selected-period])
        period-in-play  (subscribe [:get-period-in-play])
        now             (subscribe [:get-now])
        buckets         (subscribe [:get-buckets])
        templates       (subscribe [:get-templates])]

    (r/create-class
     {:reagent-render
      (fn [params]
        [view {:style     {:flex 1 :justify-content "center" :align-items "center"}
               :on-layout (fn [event]
                            (let [layout (-> event
                                             (oget "nativeEvent" "layout")
                                             (js->clj :keywordize-keys true))]
                              (if (nil? (:height dimensions))
                                (reset! dimensions {:width  (:width layout)
                                                    :height (- (:height layout) top-bar-height)}))))}
         ;; make our own status bar
         [status-bar {:hidden true}]
         [top-bar {:top-bar-height top-bar-height
                   :dimensions     dimensions
                   :displayed-day  @displayed-day
                   :now            @now}]

         ;; view that stretches to fill what is left of the screen
         [touchable-highlight
          {:on-press (fn [evt]
                       (let [native-event (oget evt "nativeEvent")
                             location-y   (oget native-event "locationY")
                             location-x   (oget native-event "locationX")
                             page-y       (oget native-event "pageY")
                             page-x       (oget native-event "pageX")
                             relative-ms  (y-pos->ms location-y (:height @dimensions))]
                         (println {:ly   location-y
                                   :ms   relative-ms
                                   :date (.toLocaleString (reset-relative-ms relative-ms @displayed-day))})))}

          [view {:style {:height           (:height @dimensions) ;; this is already adjusted to account for top-bar
                         :width            (:width @dimensions)
                         :background-color "#dedede"}}

           ;; now indicator
           (when (same-day? @now @displayed-day)
             [view {:style {:height           4
                            :width            (:width @dimensions)
                            :background-color "white"
                            :top              (-> @now
                                                  (date->y-pos (:height @dimensions))
                                                  (max 0)
                                                  (min (:height @dimensions)))}}])

           ;; periods
           (doall (->> @periods

                       (filter (fn [{:keys [start stop]}]
                                 (cond (and (some? start) (some? stop))
                                       (or (same-day? @displayed-day start)
                                           (same-day? @displayed-day stop)))))
                       (sort-by #(cond
                                   (= (:id %) (:id @period-in-play))  0
                                   (= (:id %) (:id @selected-period)) 1
                                   :else                              (.valueOf (:start %))))

                       (reverse)

                       (map #(period {:period          %
                                      :displayed-day   @displayed-day
                                      :dimensions      @dimensions
                                      :selected-period @selected-period
                                      :period-in-play  @period-in-play}))))

           ;; selection menu
           (when (some? @selected-period)
             [selection-menu {:dimensions      @dimensions
                              :selected-period @selected-period
                              :displayed-day   @displayed-day
                              :period-in-play  @period-in-play}])]]

         ;; play modal
         [modal {:animation-type "slide"
                 :transparent    false
                 :visible        @play-modal-visible}
          [view {:style {:flex    1
                         :padding 10}}
           [touchable-highlight {:on-press #(reset! play-modal-visible false)}
            [text "Cancel"]]
           [scroll-view {:style {:height "50%"}}
            [text "Select a bucket to make the period with"]
            [flat-list {:data @buckets
                        :render-item
                        (fn [i]
                          (let [item (:item (js->clj i :keywordize-keys true))]
                            (r/as-element
                             (list-items/bucket
                              (merge
                               item
                               {:on-press
                                (fn [_]
                                  (reset! play-modal-visible false)
                                  ;; passing dispatch the parent bucket id
                                  ;; for the period about to be created
                                  (dispatch [:play-from-bucket {:bucket-id (:id item)
                                                                :id        (random-uuid)
                                                                :now       (new js/Date)}]))})))))}]]

           [scroll-view {:style {:height "50%"}}
            [text "Or select a template"]
            [flat-list {:data @templates
                        :render-item
                        (fn [i]
                          (let [item (:item (js->clj i :keywordize-keys true))]
                            (r/as-element
                             (list-items/template
                              (merge
                               item
                               {:on-press
                                (fn [_]
                                  (reset! play-modal-visible false)
                                  ;; passing dispatch the parent bucket id
                                  ;; for the period about to be created
                                  (dispatch [:play-from-template {:template item
                                                                  :id       (random-uuid)
                                                                  :now      (js/Date.)}]))})))))}]]]]])})))

