(ns herlsf.gui.panels.konflikte
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.panels.components :as util]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   [cljfx.ext.list-view :as list-view]))

(def ^:const panel-name :konflikte)

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
     [(util/navbar panel-name true)
      {:fx/type list-view/with-selection-props
       :props {:selection-mode :single
               :on-selected-item-changed {:event/type ::events/navigate-list
                                          :panel panel-name}}
       :desc {:fx/type :list-view
              :min-height 960
              :cell-factory {:fx/cell-type :list-cell
                             :padding 10
                             :describe (fn [[_ name1 _ name2]]
                                         {:style-class ["h4"]
                                          :text (str name1 " vs " name2)})}
              :items (fx/sub-ctx context subs/conflicts-filtered search-term)}}]}))

(defmethod active-panel :details
  [[_ id]]
  (fn [{:keys [fx/context]}]
    (let [r (fx/sub-ctx context subs/raum-details id)]
      {:fx/type :v-box
       :spacing 10
       :padding 10
       :children [(util/navbar panel-name false)
                  {:fx/type :label
                   :text (str r)}]})))

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
