(ns herlsf.core
  (:require
   [cljfx.api :as fx]
   [datahike.api :as d]
   [clojure.core.cache :as cache]
   [herlsf.gui.events :as events]
   [herlsf.gui.views :as views]
   [herlsf.xml :as xml]
   [herlsf.gui.style :as styles])
  (:gen-class))

(def init-panel
  {:active-view [:home {:search-term ""}]
   :history []
   :back-history []
   :search-text ""})

(defn initial-state [conn]
  {:db @conn
   :panels {:veranstaltungen init-panel
            :raeume init-panel
            :konflikte init-panel}
   :comp-state {}
   :style styles/style})

(defn xml-effect
  "
  A side effect, that takes the string read from an xml file, turns it into
  a datomic transaction and dispatches a transact effect.
  "
  [^String v dispatch!]
  (try (let [transaction (xml/xml->entities v)]
         (dispatch! {:event/type :transact
                     :transaction transaction}))
       (catch Exception e
         (throw (ex-info (str "XML Import failed with exception: " e)
                         {:event-value v})))))

(defn run-app
  "
  App Entry Point. Returns a clfx app instance.
  If showing? is true, the app will show in a new window.
  Takes an active database connection for testability.
  "
  [conn showing?]
    (let [*state (atom (fx/create-context
                        (initial-state conn)
                        #(cache/lru-cache-factory % :threshold 4096)))]
    ;; Listen to changes on the datahike connection and update the state atom
      (d/listen conn :ui (fn [_] (swap! *state fx/swap-context assoc :db @conn)))
      (fx/create-app
       *state
       :event-handler events/event-handler
       :effects {:transact (fn [tx-data _] (d/transact conn tx-data))
                 :xml xml-effect}
       :desc-fn (fn [_] {:fx/type views/root
                         :showing showing?}))))

(def config (read-string (slurp "resources/config.edn")))

(defn -main []
  (let [db-cfg {:store {:backend :file
                        :path (:db-location config)}}]
    (if (not (d/database-exists? db-cfg))
      (d/create-database db-cfg)
      nil)
    (run-app (d/connect db-cfg) true)))
