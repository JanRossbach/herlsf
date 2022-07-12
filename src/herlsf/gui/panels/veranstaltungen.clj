(ns herlsf.gui.panels.veranstaltungen
  (:require
   [cljfx.api :as fx]
   [cljfx.ext.list-view :as list-view]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   ))

(def ^:const panel-name :veranstaltungen)

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
                 :panel panel-name}}
    ]})

(defmethod active-buttons :details [_] other-buttons)
(defmethod active-buttons :default [_] other-buttons)

;; Panel Views

(defmulti active-panel first)


(defmethod active-panel :home
  [[_ search-term]]
  (fn [{:keys [fx/context]}]
    (let [search-text (fx/sub-ctx context subs/search-text panel-name)]
      {:fx/type :v-box
       :children
       [{:fx/type :h-box
         :spacing 20
         :children [{:fx/type :text-field
                     :text search-text
                     :on-text-changed {:event/type ::events/set-search-text
                                       :panel panel-name}
                     :on-key-pressed {:event/type ::events/search-key-press
                                      :panel panel-name}}
                    {:fx/type :button
                     :style-class ["btn" "btn-info" "btn-sm"]
                     :text "Search"
                     :on-action {:event/type ::events/navigate
                                 :panel panel-name
                                 :new-view [:home search-text]}}]}
        {:fx/type list-view/with-selection-props
         :props {:selection-mode :single
                 :on-selected-item-changed {:event/type ::events/navigate-list
                                            :panel panel-name}}
         :desc {:fx/type :list-view
                :min-height 960
                :cell-factory {:fx/cell-type :list-cell
                               :describe (fn [[_ name]]
                                           {:style-class "p"
                                            :text (str name)})}
                :items (fx/sub-ctx context subs/veranstaltungen-filtered search-term)}}]})))

(defmethod active-panel :details
  [[_ id]]
  (fn [{:keys [fx/context]}]
    (let [v (fx/sub-ctx context subs/veranstaltung-details id)]
      {:fx/type :v-box
       :spacing 5
        :children [{:fx/type :label
                   :style-class "h4"
                   :text (str (:veranstaltung/name v))}
                   {:fx/type :label
                    :text (str (:veranstaltung/lehrpersonen v))}
                   {:fx/type :label
                    :text (str "Studiengang: " (:veranstaltung/studiengang v))}
                   {:fx/type :label
                    :text (str "SWS: " (:veranstaltung/SWS v))}
                   {:fx/type :label
                    :text (str "Teilnehmergrenze: " (:veranstaltung/max-teilnehmer v))}
                   {:fx/type :label
                    :text (str "Veranstaltungstyp: " (:veranstaltung/typ v))}
                   {:fx/type :label
                    :text (str "ECTS: " (:veranstaltung/ECTS v))}]})))

(defmethod active-panel :default
  [active-view]
  {:fx/type :label
   :text (str "Something went wrong with navigation for view: " active-view)})

(defn root [{:keys [fx/context]}]
  (let [active-view (fx/sub-ctx context subs/active-view panel-name)]
    {:fx/type :v-box
     :fill-width true
     :spacing 5
     :children [
               {:fx/type (active-panel active-view)}
                (active-buttons active-view)
                ]}))
