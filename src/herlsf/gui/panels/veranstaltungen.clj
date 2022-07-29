(ns herlsf.gui.panels.veranstaltungen
  (:require
   [cljfx.ext.list-view :as list-view]
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   [herlsf.gui.components :as util]
   ))

(def ^:const panel-name :veranstaltungen)

;; Panel Views

(defmulti active-panel first)
(defmethod active-panel :default
  [active-view]
  (util/navigation-error-panel panel-name active-view))

(defmethod active-panel :home
  [[_ search-term]]
  (fn [{:keys [fx/context]}]
    {:fx/type :v-box
     :spacing 10
     :padding 10
     :children
     [{:fx/type util/navbar
       :panel-name panel-name
       :search true}
      {:fx/type list-view/with-selection-props
       :props {:selection-mode :single
               :on-selected-item-changed {:event/type ::events/navigate-list
                                          :panel panel-name}}
       :desc {:fx/type :list-view
              :min-height 960
              :cell-factory {:fx/cell-type :list-cell
                             :padding 10
                             :describe (fn [[_ name]]
                                         {:style-class ["h4"]
                                          :text (str name)})}
              :items (fx/sub-ctx context subs/veranstaltungen-filtered search-term)}}]}))

(defn lehrperson->string
  [{:keys [:lehrperson/name :lehrperson/vorname]}]
  (str vorname " " name))

(defmethod active-panel :details
  [[_ id]]
  (fn [{:keys [fx/context]}]
    (let [v (fx/sub-ctx context subs/veranstaltung-details id)]
      {:fx/type :v-box
       :spacing 10
       :padding 10
       :children [{:fx/type util/navbar
                   :panel-name panel-name
                   :search false}
                  {:fx/type :label
                   :style-class "h4"
                   :text (str (:veranstaltung/name v))}
                  {:fx/type :label
                   :text (str "Verantwortliche Personen: " (apply str (map lehrperson->string
                                                                           (:veranstaltung/lehrpersonen v))))}
                  {:fx/type :label
                   :text (str "Studiengang: " (:veranstaltung/studiengang v))}
                  {:fx/type :label
                   :text (str "SWS: " (:veranstaltung/SWS v))}
                  {:fx/type :label
                   :text (str "Teilnehmergrenze: " (if (:veranstaltung/max-teilnemher v)
                                                     (:veranstaltung/max-teilnehmer v)
                                                     "Keine"))}
                  {:fx/type :label
                   :text (str "Veranstaltungstyp: " (:veranstaltung/typ v))}
                  {:fx/type :label
                   :text (if (:veranstaltung/ECTS v)
                           (str "ECTS: " (:veranstaltung/ECTS v))
                           "")}]})))


(defn root [{:keys [fx/context]}]
  (let [active-view (fx/sub-ctx context subs/active-view panel-name)]
    {:fx/type :v-box
     :fill-width true
     :spacing 10
     :children [{:fx/type (active-panel active-view)}]}))
