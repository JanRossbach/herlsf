(ns herlsf.dev
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.pprint :refer [pprint]]
   [clojure.repl :refer [doc]]
   [datahike.api :as d]
   [com.rpl.specter :as s]
   [clojure.data.xml :refer [parse]]
   [herlsf.core :as gui]
   [herlsf.schema :as db]
   [herlsf.xml :refer [xml->entities xmlmap->map resolve-v-verzeichnis] :as xml]
   [cljfx.api :as fx]))

(def db-cfg {:store {:backend :file
                     :path "resources/db/hike"}})

(def xml-src (slurp "GeheimeDaten.xml"))

(defn reset-db-from-xml [xml-src]
  (let [entities (xml->entities xml-src)]
    (d/delete-database db-cfg)
    (d/create-database db-cfg)
    (let [conn (d/connect db-cfg)]
      (d/transact conn db/schema)
      (d/transact conn entities))))

(comment

;; RESET DB
  (reset-db-from-xml xml-src)

  (def conn (d/connect db-cfg))

  (d/transact conn db/schema)

  (def data (xmlmap->map (parse (java.io.StringReader. (clojure.string/replace xml-src #"\n[ ]*|\r" "")))))

  (s/select [(xml/ueebene "2") :Vorlesung :Ueberschrift s/FIRST :UeBez] data)

  (pprint (s/transform [(xml/ueebene "2")] add-bereich data))

  (resolve-v-verzeichnis data)

  (xml/add-ebenen-data-to-veranstaltungen data "1")

  ()

  (pprint (->> data
               (s/transform [(xml/ueebene "1")] xml/add-studiengang)
               (s/transform [(xml/ueebene "2")] xml/add-studiengang)
               (s/transform [(xml/ueebene "2")] xml/add-kurskategorie)))

  (def v (second (s/select [(s/walker #(= "2" (:ueebene (:attrs %))))] data)))

  (count (s/select [(s/walker :Veranstaltung) :Veranstaltung] v))

  (map :attrs (s/select [(s/walker :Vorlesung)] data))

  (def entities (xml->entities xml-src))

  (pprint entities)

;; REPL

  ;; Start the App
  (def renderer (:renderer (gui/run-app (d/connect db-cfg) true)))
  (d/q '[:find ?kk
         :where
         [_ :veranstaltung/kurskategorie ?kk]]
       @conn)

  (d/q '[:find ?kk
         :where
         [?id :veranstaltung/name "Analysis I"]
         [?id :veranstaltung/kurskategorie ?kk]]
       @conn)

  (pprint (d/q '[:find ?name ?kk
                 :where
                 [?id :veranstaltung/name ?name]
                 [?id :veranstaltung/kurskategorie ?kk]]
               @conn))

  (pprint (d/q '[:find ?kk
                 :where
                 [_ :veranstaltung/kurskategorie ?kk]]
               @conn))

;; Redraw the app after changing code
  (renderer)

  ;; to iterate during development on style, add a watch to var that updates style in app
  ;; state...
  (add-watch #'styles/style :refresh-app (fn [_ _ _ _] (swap! herlsf.core/*state assoc :style styles/style)))
  ;; ... and remove it when you are done
  (remove-watch #'styles/style :refresh-app)

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
              @conn))

  (pprint (d/pull
           @conn
           '["*"
             {:veranstaltung/lehrpersonen [:lehrperson/name
                                           :lehrperson/vorname]}
             {:veranstaltung/vzeiten [:vzeit/start-zeit]}]
           322))

  (d/q
   '[:find ?name
     :in $ ?search
     :where
     [_ :veranstaltung/name ?name]
     [(re-matches ?search ?name)]]
   conn
   (re-pattern (str ".*" "Graph" ".*")))

  (spit "initial_transaction.edn" entities)

  (d/q '[:find [?stg ...]
         :where
         [_ :veranstaltung/studiengang ?stg]]
       @conn)

  (d/pull
   @conn
   '[*]
   132))
