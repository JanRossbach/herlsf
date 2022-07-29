(ns herlsf.gui.views
  (:require
   [cljfx.api :as fx]
   [cljfx.css :as css]
   [herlsf.gui.panels.veranstaltungen :as v]
   [herlsf.gui.panels.raeume :as r]
   [herlsf.gui.panels.konflikte :as k])
  (:import [org.kordamp.bootstrapfx BootstrapFX]))

(defn file-menu
  [{:keys [fx/context]}]
  {:fx/type :menu
   :text "Files"
   :items [{:fx/type :menu-item
            :text "Import xml file"
            :on-action {:event/type ::dummy}}
           {:fx/type :menu-item
            :text "Open"}]})

(defn settings-menu
  [{:keys [fx/context]}]
  {:fx/type :menu
   :text "Settings"
   :items [{:fx/type :menu-item
            :text "Open Settings"
            :on-action {:event/type ::dummy}}
           {:fx/type :menu-item
            :text "Open"}]})


(defn menubar
  [_]
  {:fx/type :menu-bar
   :menus [{:fx/type file-menu}
           {:fx/type settings-menu}]})


;; To introduce new Tabs, you can add them here and mount the appropriate root component.
;; If you want navigation, you need to also add the appropriate panel name to the inital state in
;; herlsf.core

(def tabs
  [
   {:name "Kurse" :view v/root}
   {:name "Räume" :view r/root}
   {:name "Konflikte" :view k/root}
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
   :pref-height 1824
   :tabs (mapv tab-item tabs)})

(defn root
  [{:keys [fx/context showing]}]
    {:fx/type :stage
     :showing showing
     :title "HER-LSF"
     :scene {:fx/type :scene
             :stylesheets [(::css/url (fx/sub-val context :style))
                           (BootstrapFX/bootstrapFXStylesheet)]
             :root {:fx/type :v-box
                    :fill-width true
                    :spacing 5
                    :children [{:fx/type menubar}
                               main-bar]}}})
