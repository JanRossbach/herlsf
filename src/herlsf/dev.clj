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
   [herlsf.xml :refer [xml->entities xmlmap->map]]
   [cljfx.api :as fx]))

(comment

  (doc fx/sub-ctx)

  (doc d/db)

  (doc d/listen)

  (doc fx/create-app)

  (def renderer (:renderer (gui/run-app)))

  (renderer)

  ((:renderer (gui/run-app)))

  (def conn (d/connect {:store {:backend :file
                                :path "resources/db/hike"}}))

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
   conn
   (re-pattern (str ".*" "Graph" ".*")))

  (spit "initial_transaction.edn" entities)

  (d/create-database {:store {:backend :file
                              :path "resources/db/hike"}})

  (d/transact conn db/schema)

  (d/transact conn entities)

  (d/q veranstaltung-ids @conn)

  (d/q '[:find ?id
         :where
         [?v :lehrperson/name "Klau"]
         [?v :lehrperson/pers-id ?id]]
       @conn)

  (d/pull
   @conn
   '[*]
   132)

;; REPL

  (run-app)

  ;; to iterate during development on style, add a watch to var that updates style in app
  ;; state...
  (add-watch #'styles/style :refresh-app (fn [_ _ _ _] (swap! *state assoc :style styles/style)))
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
              @conn)))
