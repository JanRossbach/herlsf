(ns herlsf.gui.subs
  (:require
   [cljfx.api :as fx]
   ;[com.rpl.specter :as s]
   [datahike.api :as d]))

(defn- query-sub [context query & inputs]
  (apply d/q query (fx/sub-val context :db) inputs))

(defn- pull-sub [context pull-expr id]
  (d/pull (fx/sub-val context :db) pull-expr id))

(defn veranstaltungen-filtered
  [context search-term]
  (let [search-regex (re-pattern (str ".*" search-term ".*"))]
    (fx/sub-ctx context query-sub
                '[:find ?id ?name
                  :in $ ?search-term
                  :where
                  [?id :veranstaltung/name ?name]
                  [(re-matches ?search-term ?name)]]
                search-regex)))


(defn raeume-filtered
  [context search-term]
  (let [search-regex (re-pattern (str ".*" search-term ".*"))]
    (fx/sub-ctx context query-sub
                '[:find ?id ?name
                  :in $ ?search-term
                  :where
                  [?id :raum/name ?name]
                  [(re-matches ?search-term ?name)]]
                search-regex)))

(defn conflicts-filtered
  [context search-term]
  (let [search-regex (re-pattern (str ".*" search-term ".*"))]
    (fx/sub-ctx context query-sub
                '[:find ?zeit ?n ?zeit2 ?m
                  :in $ ?search-term
                  :where
                  [?v :veranstaltung/name ?n]
                  [?w :veranstaltung/name ?m]
                  [(not= ?v ?w)]
                  (or-join [?n ?m ?search-term]
                           [(re-matches ?search-term ?n)]
                           [(re-matches ?search-term ?m)])
                  [?zeit :vzeit/start-zeit ?time]
                  [?zeit2 :vzeit/start-zeit ?time]
                  [?zeit :vzeit/wochentag ?tag]
                  [?zeit2 :vzeit/wochentag ?tag]
                  [?v :veranstaltung/vzeiten ?zeit]
                  [?w :veranstaltung/vzeiten ?zeit2]
                  [?v :veranstaltung/studiengang ?s]
                  [?w :veranstaltung/studiengang ?s]]
                search-regex)))

(defn active-view
  [context panel]
  (let [panels (fx/sub-val context :panels)]
    (:active-view (panel panels))))

(defn search-text
  [context panel]
  (let [panels (fx/sub-val context :panels)]
    (:search-text (panel panels))))

(defn veranstaltung-details
  [context id]
  (fx/sub-ctx context pull-sub '["*"
                                 {:veranstaltung/lehrpersonen
                                  [:lehrperson/name :lehrperson/vorname]}
                                 {:veranstaltung/vzeiten
                                  [:vzeit/raum]}]
              id))

(defn pull-all
  [context id]
  (fx/sub-ctx context pull-sub '["*"] id))

(defn v-zeiten
  [context raum-id]
  (fx/sub-ctx context query-sub
   '[:find [?id ...]
     :in $ ?raum-id
     :where
     [?id :vzeit/raum ?raum-id]]
   raum-id))
