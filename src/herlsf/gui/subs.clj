(ns herlsf.gui.subs
  (:require
   [cljfx.api :as fx]
   [datahike.api :as d]))

(defn- query-sub [context query & inputs]
  (apply d/q query (fx/sub-val context :db) inputs))

(defn alle-veranstaltungs-ids
  [context]
  (fx/sub-ctx context query-sub
              '[:find [?id ...]
                :where [?id :veranstaltung/name]]))
