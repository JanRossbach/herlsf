(ns herlsf.core
  (:require
   [cljfx.api :as fx]
   [datahike.api :as d]
   [clojure.core.cache :as cache]
   [herlsf.gui.events :as events]
   [herlsf.gui.views.core :as views]
   [herlsf.xml :as xml]
   [herlsf.gui.style :as styles])
  (:gen-class))


(def db-cfg {:store {:backend :file
                     :path "resources/db/hike"}})

(def conn (d/connect db-cfg))

(def initial-state
  {:name "HER-LSF"
   :db @conn
   :active-panel [:home]
   :style styles/style
   :counter 0})

(def *state
  (atom (fx/create-context
         initial-state
         #(cache/lru-cache-factory % :threshold 4096))))

;; Listen to changes on the datahike connection and update the state atom
(d/listen conn :ui (fn [_] (swap! *state fx/swap-context assoc :db @conn)))

(defn xml-effect
  [^String v dispatch!]
  (try (let [transaction (xml/xml->entities v)]
         (dispatch! {:event/type :transact
                     :transaction transaction}))
       (catch Exception e
         (throw (ex-info (str "XML Import failed with exception: " e)
                         {:event-value v})))))

(defn run-app []
  (fx/create-app
   *state
   :event-handler events/event-handler
   :effects {:transact (fn [tx-data _] (d/transact conn tx-data))
             :xml xml-effect}
   :desc-fn (fn [_] {:fx/type views/root})))


(defn -main []
  (run-app))
