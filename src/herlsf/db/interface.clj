(ns herlsf.db.interface
  (:require
   [herlsf.db.queries :as query]
   [datahike.api :as d]))

(def db-cfg {:store {:backend :file
                  :path "resources/db/hike"}})

(defonce conn (d/connect db-cfg))

(defn search-veranstaltungen-by-name
  [search-term]
  (query/search-veranstaltungen-by-name @conn search-term))
