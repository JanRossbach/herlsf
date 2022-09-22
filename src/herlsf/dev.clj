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
   [herlsf.xml :refer [xml->entities xmlmap->map] :as xml]
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

(def conn (d/connect db-cfg))

(comment

  ;; Start the App
  (def renderer (:renderer (gui/run-app (d/connect db-cfg) true)))

  (renderer)

;; RESET DB
  (reset-db-from-xml xml-src)


  (d/transact conn db/schema)

  (def data (xmlmap->map (parse (java.io.StringReader. (clojure.string/replace xml-src #"\n[ ]*|\r" "")))))

  (s/select [(xml/ueebene "2") :Vorlesung :Ueberschrift s/FIRST :UeBez] data)

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

  (d/q '[:find ?name
         :where
         [60 :veranstaltung/name ?name]]
       @conn)

  (d/transact conn [[:db/retractEntity 60]])

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

;; Redraw the app after changing code
  (renderer)

  ;; to iterate during development on style, add a watch to var that updates style in app
  ;; state...
  (add-watch #'styles/style :refresh-app (fn [_ _ _ _] (swap! herlsf.core/*state assoc :style styles/style)))
  ;; ... and remove it when you are done
  (remove-watch #'styles/style :refresh-app)

  (d/q '[:find ?zeit ?n ?zeit2 ?m ?s
         :where
         [?v :veranstaltung/name ?n]
         [?w :veranstaltung/name ?m]
         [(< ?v ?w)]
         ;; (or-join [?n ?m ?search-term]
         ;;          [(re-matches ?search-term ?n)]
         ;;          [(re-matches ?search-term ?m)])
         [?zeit :vzeit/start-zeit ?time]
         [?zeit2 :vzeit/start-zeit ?time]
         [?zeit :vzeit/wochentag ?tag]
         [?zeit2 :vzeit/wochentag ?tag]
         [?v :veranstaltung/vzeiten ?zeit]
         [?w :veranstaltung/vzeiten ?zeit2]
         [?v :veranstaltung/typ ?s]
         [?w :veranstaltung/typ ?s]
         [?v :veranstaltung/kurskategorie ?b]
         [?w :veranstaltung/kurskategorie ?b]]
       @conn)

  (d/q '[:find ?n ?m
         :where
         [?v :veranstaltung/name ?n]
         [?w :veranstaltung/name ?m]
         [(< (count ?n) (count ?m))]]
       @conn)

  (< (count "Hello") (count "Worldlökasjdf"))

  (re-matches #".*KfW.*" "KfW, Kreditanstalt f. Wiederaufbau" )

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

  (d/q '[:find ?id
         :where
         [?id :veranstaltung/name _]]
       @conn)


  (d/q '[:find [?stg ...]
         :where
         [_ :veranstaltung/studiengang ?stg]]
       @conn)

  (d/pull
   @conn
   '[*]
   132))
