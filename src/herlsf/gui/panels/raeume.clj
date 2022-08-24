(ns herlsf.gui.panels.raeume
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   [herlsf.gui.components :as util]
   [cljfx.ext.list-view :as list-view]
   [com.rpl.specter :as s]
   [clojure.string :as string]))

(def ^:const panel-name :raeume)

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
              :min-height 960
              :cell-factory {:fx/cell-type :list-cell
                             :padding 10
                             :describe (fn [[_ name]]
                                         {:style-class ["h4"]
                                          :text (str name)})}
              :items (fx/sub-ctx context subs/raeume-filtered search-filter)}}]}))

(defn label [[k v]]
  {:fx/type :label
   :style-class ["p"]
   :text (str k " : " v)})

(defn v-zeit
  [{:keys [fx/context v-id]}]
  (let [v-zeit (fx/sub-ctx context subs/pull-all v-id)]
    {:fx/type :v-box
     :padding 3
     :children (vec (for [kv (seq v-zeit)]
                      (label kv)))}))

(defn vz-list
  [{:keys [fx/context v-zeiten]}]
  {:fx/type :list-view
   :min-height 200
   :cell-factory {:fx/cell-type :list-cell :padding 10
                  :describe (fn [v] {:style-class ["h4"]
                                    :text (string/join "-" v)})}
   :items v-zeiten})

(defn raum-view
  [{:keys [raum v-zeiten]}]
  (let [{:keys [:raum/form :raum/gebaeude :raum/name]} raum]
    {:fx/type :v-box
     :spacing 10
     :padding 10
     :children [{:fx/type :label
                 :style-class ["h2"]
                 :text name}
                {:fx/type :label
                 :style-class ["h3"]
                 :text (str "Form: " form)}
                {:fx/type :label
                 :style-class ["h4"]
                 :text (str "Gebäude: " gebaeude)}
                {:fx/type vz-list
                 :v-zeiten v-zeiten}]}))

(defmethod active-panel :details
  [[_ [id]]]
  (fn [{:keys [fx/context]}]
    (let [r (fx/sub-ctx context subs/pull-all id)
          v-zeiten (fx/sub-ctx context subs/v-zeiten id)]
      {:fx/type :v-box
       :spacing 10
       :padding 10
       :children [{:fx/type util/navbar
                   :panel-name panel-name
                   :search false}
                  {:fx/type raum-view
                   :raum r
                   :v-zeiten v-zeiten}
                  {:fx/type util/delete-button
                   :state-id ::delete-raum-button
                   :entity-id id}
                  ]})))

(defn table-view [_]
  {:fx/type :table-view
   :row-factory {:fx/cell-type :table-row
                 :describe (fn [x]
                             {:style {:-fx-border-color x}})}
   :columns [{:fx/type :table-column
              :text "pr-str"
              :cell-value-factory identity
              :cell-factory {:fx/cell-type :table-cell
                             :describe (fn [x]
                                         {:text (pr-str x)})}}
             {:fx/type :table-column
              :text "bg color"
              :cell-value-factory identity
              :cell-factory {:fx/cell-type :table-cell
                             :describe (fn [i]
                                         {:style {:-fx-background-color i}})}}]
   :items [:red :green :blue "#ccc4" "#ccc4"]})


(defn root [{:keys [fx/context]}]
  (let [active-view (fx/sub-ctx context subs/active-view panel-name)]
    {:fx/type :v-box
     :fill-width true
     :spacing 10
     :children [{:fx/type (active-panel active-view)}]}))
