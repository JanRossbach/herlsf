(ns herlsf.gui.core
  (:require
   [clojure.repl :refer [doc]]
   [clojure.pprint :refer [pprint]]
   [cljfx.api :as fx]
   [cljfx.css :as css]
   [herlsf.core :as core]
   [herlsf.db.interface :as db]
   ))

(def style
  (let [text (fn [size weight]
               {:-fx-text-fill "#111"
                :-fx-wrap-text true
                :-fx-font-weight weight
                :-fx-font-size size})]

    (css/register
     ::style
     {".app-label" (text 25 :normal)})))

(defonce *state
  (atom {:name "HER-LSF"
         :style style}))

(defn root [{:keys [name style]}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :stylesheets [(::css/url style)]
           :root {:fx/type :v-box
                  :children
                  [{:fx/type :label
                    :style-class "app-label"
                    :text "Label with some text"}
                   {:fx/type :label
                    :style-class "app-label"
                    :text "Another label"}]}}})

(defn map-event-handler
  [e]
  )

(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)
   :opts {:fx.opt/map-event-handler map-event-handler}))


(defn run
  []
  (fx/mount-renderer *state renderer))

(comment

  (run)

  ;; to iterate during development on style, add a watch to var that updates style in app
  ;; state...
  (add-watch #'style :refresh-app (fn [_ _ _ _] (swap! *state assoc :style style)))
  ;; ... and remove it when you are done
  (remove-watch #'style :refresh-app)

)
