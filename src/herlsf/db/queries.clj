(ns herlsf.db.queries
  (:require
   [datahike.api :as d]))

(defn search-veranstaltungen-by-name
  [conn search-term]
  (let [search-regex (re-pattern (str ".*" search-term ".*"))]
    (d/q
     '[:find ?name
       :in $ ?search-term
       :where
       [_ :veranstaltung/name ?name]
       [(re-matches ?search-term ?name)]]
     conn
     search-regex)))
