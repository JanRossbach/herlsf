(ns herlsf.gui.subs
  (:require
   [cljfx.api :as fx]
   ;[com.rpl.specter :as s]
   [datahike.api :as d]))

(defn- query-sub [context query & inputs]
  (apply d/q query (fx/sub-val context :db) inputs))

(defn- pull-sub [context pull-expr id]
  (d/pull (fx/sub-val context :db) pull-expr id))

(defn alle-veranstaltungen
  [context]
  (fx/sub-ctx context query-sub
              '[:find ?id ?name
                :where [?id :veranstaltung/name ?name]]))


(defn active-view
  [context panel]
  (let [panels (fx/sub-val context :panels)]
    (:active-view (panel panels))))

(defn veranstaltung-details
  [context id]
  (fx/sub-ctx context pull-sub '[*] id))
