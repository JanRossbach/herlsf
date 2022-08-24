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
  [context {:keys [search-term studiengang] :as arg}]
  (let [search-regex (re-pattern (str ".*" search-term ".*"))]
    (if (and studiengang (not= studiengang "Studiengang"))
      (fx/sub-ctx context query-sub
                  '[:find ?id ?name
                    :in $ ?search-term ?stg
                    :where
                    [?id :veranstaltung/name ?name]
                    [?id :veranstaltung/studiengang ?stg]
                    [(re-matches ?search-term ?name)]]
                  search-regex
                  studiengang)
      (fx/sub-ctx context query-sub
                  '[:find ?id ?name
                    :in $ ?search-term
                    :where
                    [?id :veranstaltung/name ?name]
                    [(re-matches ?search-term ?name)]]
                  search-regex))))

(defn raeume-filtered
  [context {:keys [search-term]}]
  (let [search-regex (re-pattern (str ".*" search-term ".*"))]
    (fx/sub-ctx context query-sub
                '[:find ?id ?name
                  :in $ ?search-term
                  :where
                  [?id :raum/name ?name]
                  [(re-matches ?search-term ?name)]]
                search-regex)))

(defn conflicts-filtered
  [context {:keys [search-term]}]
  (let [search-regex (re-pattern (str ".*" search-term ".*"))]
    (fx/sub-ctx context query-sub
                '[:find ?zeit ?n ?b ?m ?v ?w
                  :in $ ?search-term
                  :where
                  [?v :veranstaltung/name ?n]
                  [?w :veranstaltung/name ?m]
                  [(< ?v ?w)]
                  (or-join [?n ?m ?search-term]
                           [(re-matches ?search-term ?n)]
                           [(re-matches ?search-term ?m)])
                  [?zeit :vzeit/start-zeit ?time]
                  [?zeit2 :vzeit/start-zeit ?time]
                  [?zeit :vzeit/wochentag ?tag]
                  [?zeit2 :vzeit/wochentag ?tag]
                  [?v :veranstaltung/vzeiten ?zeit]
                  [?w :veranstaltung/vzeiten ?zeit2]
                  [?v :veranstaltung/kurskategorie ?b]
                  [?w :veranstaltung/kurskategorie ?b]
                  [?v :veranstaltung/typ "Vorlesung Präsenz"]
                  [?w :veranstaltung/typ "Vorlesung Präsenz"]]
                search-regex)))

(defn active-view
  [context panel]
  (let [panels (fx/sub-val context :panels)]
    (:active-view (panel panels))))

(defn search-text
  [context panel]
  (let [panels (fx/sub-val context :panels)]
    (:search-text (panel panels))))

(defn search-filter [context panel]
  (second (:active-view (panel (fx/sub-val context :panels)))))

(defn studiengang-filter-value
  [context]
  (if-let [value (:studiengang (fx/sub-ctx context search-filter :veranstaltungen))]
    value
    "Studiengang"))

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
   '[:find ?start-zeit ?end-zeit ?wochentag ?vname
     :in $ ?raum-id
     :where
     [?id :vzeit/raum ?raum-id]
     [?id :vzeit/start-zeit ?start-zeit]
     [?id :vzeit/end-zeit ?end-zeit]
     [?id :vzeit/wochentag ?wochentag]
     [?vid :veranstaltung/vzeiten ?id]
     [?vid :veranstaltung/name ?vname]]
   raum-id))

(defn studiengaenge
  [context]
  (fx/sub-ctx context query-sub '[:find [?stg ...]
                                  :where
                                  [_ :veranstaltung/studiengang ?stg]]))
