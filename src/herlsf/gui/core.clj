(ns herlsf.gui.core
  (:require
   [cljfx.api :as fx]
   [datahike.api :as d]
   [clojure.core.cache :as cache]
   [herlsf.gui.events :as events]
   [herlsf.gui.views :as views]
   [herlsf.db.xml :as xml]
   [herlsf.gui.style :refer [style]]
   ))


(def db-cfg {:store {:backend :file
                     :path "resources/db/hike"}})



(defonce *state
  (atom (fx/create-context
         {:application/name "HER-LSF"
          :application/style style
          :db/conn (d/connect db-cfg)
          :style style}
         cache/lru-cache-factory)))


(defn xml-effect
  [^String v dispatch!]
  (try (let [transaction (xml/xml->entities v)]
         (dispatch! {:event/type :transact
                     :transaction transaction}))
       (catch Exception e
         (throw (ex-info (str "XML Import failed with exception: " e)
                         {:event-value v})))))

(def event-handler
  (-> events/event-handler
      (fx/wrap-co-effects
       {:fx/context (fx/make-deref-co-effect *state)})
      (fx/wrap-effects
       {:context (fx/make-reset-effect *state)
        :dispatch fx/dispatch-effect
        :transact (fn [v _] (d/transact (:conn @*state) (:transaction v)))
        :xml xml-effect})))

(def renderer
  (fx/create-renderer
   :middleware (comp fx/wrap-context-desc
                  (fx/wrap-map-desc (fn [_] {:fx/type views/root})))
   :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                       (fx/fn->lifecycle-with-context %))
          :fx.opt/map-event-handler event-handler}))

(defn run
  []
  (fx/mount-renderer *state renderer))

;; REPL

(comment

  (run)

  (renderer)

  ;; to iterate during development on style, add a watch to var that updates style in app
  ;; state...
  (add-watch #'style :refresh-app (fn [_ _ _ _] (swap! *state assoc :style style)))
  ;; ... and remove it when you are done
  (remove-watch #'style :refresh-app)

)
