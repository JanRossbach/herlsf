(ns herlsf.gui.panels.veranstaltungen
  (:require
   [cljfx.api :as fx]
   [cljfx.ext.list-view :as list-view]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   ))

;; Button Rows


(defmulti active-buttons first)

(def home-buttons
  {:fx/type :h-box
   :spacing 5
   :children
   [{:fx/type :button
     :text "Back"
     :style-class ["btn" "btn-danger"]
     :on-action {:event/type ::events/navigate
                 :target [:home]}}
    {:fx/type :button
     :text "Info"
     :style-class ["btn" "btn-info"]
     :on-action {:event/type ::events/navigate
                 :target [:other]}}
    {:fx/type :button
     :text "Success"
     :style-class ["btn" "btn-success"]
     :on-action {:event/type ::events/navigate
                 :target [:other]}}]})

(defmethod active-buttons :home [_] home-buttons)

(def other-buttons
  {:fx/type :h-box
   :spacing 5
   :children
   [{:fx/type :button
     :text "Success"
     :style-class ["btn" "btn-success"]
     :on-action {:event/type ::events/navigate
                 :target [:home]}}]})

(defmethod active-buttons :other [_] other-buttons)

;; Panel Views

(defmulti active-panel first)

(defmethod active-panel :home
  [_]
  (fn [{:keys [fx/context]}]
    {:fx/type list-view/with-selection-props
     :props {:selection-mode :single
             :on-selected-item-changed {:event/type ::events/select-veranstaltung}}
     :desc {:fx/type :list-view
            :cell-factory {:fx/cell-type :list-cell
                           :describe (fn [[_ name]]
                                       {:style-class "p"
                                        :text (str name)})}
            :items (fx/sub-ctx context subs/alle-veranstaltungen)}}))

(defn root [{:keys [fx/context]}]
  (let [active-view (fx/sub-ctx context subs/active-view :veranstaltungen)]
    {:fx/type :v-box
     :fill-width true
     :spacing 5
     :children [{:fx/type (active-panel active-view)}
                (active-buttons active-view)]}))
