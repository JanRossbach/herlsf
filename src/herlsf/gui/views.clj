(ns herlsf.gui.views
  (:require
   [cljfx.api :as fx]
   [cljfx.css :as css]
   [herlsf.gui.events :as events]
   [herlsf.gui.subs :as subs])
  (:import [org.kordamp.bootstrapfx BootstrapFX]))

(defn root [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :stylesheets [(::css/url (fx/sub-val context :style))
                         (BootstrapFX/bootstrapFXStylesheet)]
           :root {:fx/type :v-box
                  :spacing 10
                  :padding 20
                  :children [{:fx/type :label
                              :text (str (fx/sub-val context :name))
                              :style-class "text-primary"}
                             {:fx/type :label
                              :text (str (fx/sub-val context :counter))}
                             {:fx/type :button
                              :text "Inc"
                              :style-class "btn-primary"
                              :on-action {:event/type ::events/hello}}]}}})
