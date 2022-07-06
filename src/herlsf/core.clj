(ns herlsf.core
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.pprint :refer [pprint]]
   [datahike.api :as d]
   [com.rpl.specter :as s]
   [clojure.data.xml :refer [parse]]
   [herlsf.db.interface :as db]
   [herlsf.gui.core :as gui]
   [herlsf.xml :refer [xml->entities xmlmap->map]]))




(def conn (d/connect cfg))

(defn get-db-connection
  []
  (d/connect cfg))

(def xml-src (slurp "/home/jan/School/GeheimeDaten.xml"))

(def data (xmlmap->map (parse (java.io.StringReader. (clojure.string/replace xml-src #"\n[ ]*|\r" "")))))

(def entities (xml->entities xml-src))

(spec/explain ::db/entities entities)

(def veranstaltung-ids
  '[:find ?id
    :where
    [?n :veranstaltung/id ?id]])

(d/q
 '[:find ?name
   :in $ ?search
   :where
   [_ :veranstaltung/name ?name]
   [(re-matches ?search ?name)]]
 @conn
 (re-pattern (str ".*" "Graph" ".*")))

(comment
  (spit "initial_transaction.edn" entities)

  (d/create-database cfg)

  (d/transact conn db/schema)

  (d/transact conn entities)

  (d/q veranstaltung-ids @conn)

  (d/q '[:find ?id
         :where
         [?v :lehrperson/name "Klau"]
         [?v :lehrperson/pers-id ?id]]
       @conn)

  (count (d/q '[:find ?n ?m ?time ?tag ?s
                :where
                [?zeit :vzeit/start-zeit ?time]
                [?zeit2 :vzeit/start-zeit ?time]
                [?zeit :vzeit/wochentag ?tag]
                [?zeit2 :vzeit/wochentag ?tag]
                [?v :veranstaltung/vzeiten ?zeit]
                [?w :veranstaltung/vzeiten ?zeit2]
                [(not= ?v ?w)]
                [?v :veranstaltung/studiengang ?s]
                [?w :veranstaltung/studiengang ?s]
                [?v :veranstaltung/name ?n]
                [?w :veranstaltung/name ?m]]
              @conn)

         )

)

(defn run
  []
  (gui/run))
