(ns herlsf.gui.panels.konflikte
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.components :as util]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   [cljfx.ext.list-view :as list-view]
   [herlsf.schema :as db]))

(def ^:const panel-name :konflikte)

(defmulti active-panel first)
(defmethod active-panel :default
  [active-view]
  (util/navigation-error-panel panel-name active-view))

(defn konflikt->str
  [[_ name1 _ name2]]
  (str name1 " || " name2))

(defn konflikt->style-class
  [_]
  "h4"
  ;; (case (db/konflikt->danger-class tuple)
  ;;   :danger "bg-danger"
  ;;   :warning "bg-warning"
  ;;   :normal "h4"
    )

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
                             :describe (fn [tuple]
                                         {:style-class ["h4" (konflikt->style-class tuple)]
                                          :text (konflikt->str tuple)})}
              :items (fx/sub-ctx context subs/conflicts-filtered search-filter)}}]}))

(defmethod active-panel :details
  [[_ [vz-id1 kursname1 kategorie kursname2 kursid1 kursid2]]]
  (fn [{:keys [fx/context]}]
    (let [info (fx/sub-ctx context subs/pull-all vz-id1)]
      {:fx/type :v-box
       :spacing 10
       :padding 10
       :children [{:fx/type util/navbar
                   :panel-name panel-name
                   :search false}
                  {:fx/type :label
                   :text "Konflikt ! :/"
                   :style-class ["h2"]}
                  {:fx/type :label
                   :text (str "Am " (:vzeit/wochentag info) " um "
                              (:vzeit/start-zeit info) " bis " (:vzeit/end-zeit info)
                              " finden die folgenden Kurse statt."
                              " Beide sind in der Kategorie" )
                   :style-class ["h4"]}
                  {:fx/type :label
                   :text (str kategorie)
                   :style-class ["h4"]}
                  {:fx/type :h-box
                   :spacing 10
                   :padding 10
                   :children [{:fx/type :button
                               :style-class ["btn" "btn-default" "btn-sm"]
                               :text kursname1
                               :on-action {:event/type ::events/navigate
                                           :panel panel-name
                                           :new-view [:kurs-details kursid1]}}
                              {:fx/type :button
                               :style-class ["btn" "btn-default" "btn-sm"]
                               :text kursname2
                               :on-action {:event/type ::events/navigate
                                           :panel panel-name
                                           :new-view [:kurs-details kursid2]}}]}]})))


(defmethod active-panel :kurs-details
  [[_ id]]
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
