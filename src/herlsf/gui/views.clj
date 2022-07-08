(ns herlsf.gui.views
  (:require
   [cljfx.api :as fx]
   ;[herlsf.gui.events :as events]
   [cljfx.css :as css]
   [herlsf.gui.subs :as subs]
   ))

(defn root [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :stylesheets [(::css/url (:style context))]
           :root {:fx/type :v-box
                  :children
                  [{:fx/type :label
                    :style-class "app-label"
                    :text "lksjsdfLabelstuff with some text"}
                   {:fx/type :label
                    :style-class "app-label"
                    :text "Another label"}]}}})
