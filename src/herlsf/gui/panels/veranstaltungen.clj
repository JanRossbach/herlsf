(ns herlsf.gui.panels.veranstaltungen
  (:require
   [cljfx.ext.list-view :as list-view]
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   [herlsf.gui.components :as util]))

(def ^:const panel-name :veranstaltungen)

;; Panel Views

(defmulti active-panel first)
(defmethod active-panel :default
  [active-view]
  (util/navigation-error-panel panel-name active-view))

(defmethod active-panel :home
  [[_ search-filter]]
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
              :min-height 850
              :cell-factory {:fx/cell-type :list-cell
                             :padding 10
                             :describe (fn [[_ name]]
                                         {:style-class ["h4"]
                                          :text (str name)})}
              :items (fx/sub-ctx context subs/veranstaltungen-filtered search-filter)}}
      {:fx/type util/create-button
       :panel-name panel-name}]}))

(defmethod active-panel :create
  [_]
  (fn [_]
    {:fx/type :v-box
     :spacing 10
     :padding 10
     :children
     [{:fx/type util/navbar
       :panel-name panel-name
       :search false}
      {:fx/type util/veranstaltung-form
       :panel-name panel-name
       :state-id ::create-v-form}
      {:fx/type util/submit-button
       :state-id ::create-v-form}]}))

(defmethod active-panel :details
  [[_ [id]]]
  (fn [_]
    {:fx/type :v-box
     :spacing 10
     :padding 10
     :children
     [{:fx/type util/navbar
       :panel-name panel-name
       :search false}
      {:fx/type util/veranstaltung-details
       :id id}
      {:fx/type :h-box
       :spacing 10
       :children [{:fx/type util/edit-button
                   :panel-name panel-name
                   :entity-id id}
                  {:fx/type util/delete-button
                   :entity-id id}]}]}))

(defmethod active-panel :edit
  [[_ id]]
  (fn [_]
    {:fx/type :v-box
     :spacing 10
     :padding 10
     :children
     [{:fx/type util/navbar
       :panel-name panel-name
       :search false}
      {:fx/type util/veranstaltung-form
       :state-id (keyword (str "edit-form-" id))}]}))

(defn root [{:keys [fx/context]}]
  (let [active-view (fx/sub-ctx context subs/active-view panel-name)]
    {:fx/type :v-box
     :fill-width true
     :spacing 10
     :children [{:fx/type (active-panel active-view)}]}))
