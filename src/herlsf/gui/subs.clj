(ns herlsf.gui.subs
  (:require
   [cljfx.api :as fx]
   ;[com.rpl.specter :as s]
   [datahike.api :as d]))

(defn- query-sub [context query & inputs]
  (apply d/q query (fx/sub-val context :db) inputs))

(defn alle-veranstaltungen
  [context]
  (fx/sub-ctx context query-sub
              '[:find ?id ?name
                :where [?id :veranstaltung/name ?name]]))

(defn active-panel
  [context]
  (fx/sub-val context :active-panel))
