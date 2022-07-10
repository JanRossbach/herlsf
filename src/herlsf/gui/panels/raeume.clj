(ns herlsf.gui.panels.raeume
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   ))

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


(defn root [_]
  {:fx/type table-view})
