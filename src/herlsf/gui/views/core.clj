(ns herlsf.gui.views.core
  (:require
   [cljfx.api :as fx]
   [cljfx.css :as css]
   [cljfx.ext.list-view :as list-view]
   [herlsf.gui.views.menubar :as menubar]
   [herlsf.gui.events :as events]
   [herlsf.gui.views.button-row :as buttons]
   [herlsf.gui.subs :as subs])
  (:import [org.kordamp.bootstrapfx BootstrapFX]))


(defn veranstaltungen-list-view [{:keys [fx/context]}]
  {:fx/type list-view/with-selection-props
   :props {:selection-mode :single
           :on-selected-item-changed {:event/type ::events/select-veranstaltung}}
   :desc {:fx/type :list-view
          :cell-factory {:fx/cell-type :list-cell
                         :describe (fn [[_ name]]
                                     {:style-class "p"
                                      :text (str name)})}
          :items (fx/sub-ctx context subs/alle-veranstaltungen)}})

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

(def tabs
  [
   {:name "Veranstaltung" :view veranstaltungen-list-view}
   {:name "Räume" :view table-view}
   {:name "Konflikte" :view table-view}
   ])


(defn tab-item
  [{:keys [name view]}]
  {:fx/type :tab
   :text name
   :closable false
   :content {:fx/type view}})

(def main-bar
  {:fx/type :tab-pane
   :pref-width 960
   :pref-height 1024
   :tabs (mapv tab-item tabs)})


(defn root [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :title "HER-LSF"
   :scene {:fx/type :scene
           :stylesheets [(::css/url (fx/sub-val context :style))
                         (BootstrapFX/bootstrapFXStylesheet)]
           :root {:fx/type :v-box
                  :fill-width true
                  :spacing 5
                  :children [{:fx/type menubar/menubar}
                             main-bar
                             (buttons/button-row (fx/sub-val context :active-panel))]}}})
