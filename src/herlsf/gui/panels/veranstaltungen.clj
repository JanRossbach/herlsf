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
   []})

(defmethod active-buttons :home [_] home-buttons)

(def other-buttons
  {:fx/type :h-box
   :spacing 5
   :children
   [{:fx/type :button
     :text "Back"
     :style-class ["btn" "btn-danger"]
     :on-action {:event/type ::events/navigate-back
                 :panel :veranstaltungen}}
    ]})

(defmethod active-buttons :details [_] other-buttons)
(defmethod active-buttons :default [_] other-buttons)

;; Panel Views

(defmulti active-panel first)

(defmethod active-panel :home
  [_]
  (fn [{:keys [fx/context]}]
    {:fx/type list-view/with-selection-props
     :props {:selection-mode :single
             :on-selected-item-changed {:event/type ::events/navigate-list
                                        :panel :veranstaltungen}}
     :desc {:fx/type :list-view
            :min-height 960
            :cell-factory {:fx/cell-type :list-cell
                           :describe (fn [[_ name]]
                                       {:style-class "p"
                                        :text (str name)})}
            :items (fx/sub-ctx context subs/alle-veranstaltungen)}}))

(defmethod active-panel :details
  [[_ id]]
  (fn [{:keys [fx/context]}]
    (let [v (fx/sub-ctx context subs/veranstaltung-details id)]
      {:fx/type :label
       :style-class "h3"
       :text (str v)})))

(defmethod active-panel :default
  [active-view]
  {:fx/type :label
   :text (str "Something went wrong with navigation for view: " active-view)})

(defn root [{:keys [fx/context]}]
  (let [active-view (fx/sub-ctx context subs/active-view :veranstaltungen)]
    {:fx/type :v-box
     :fill-width true
     :spacing 5
     :children [{:fx/type (active-panel active-view)}
                (active-buttons active-view)]}))
