(ns herlsf.db.schema
  (:require
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.alpha :as spec]
   [provisdom.spectomic.core :as spectomic]))

;; specs

;(def raeume (into #{} (read-string (slurp "resources/raeume.edn"))))

(spec/def :raum/name string?)
(spec/def :raum/form string?)
(spec/def :raum/gebaeude string?)

(spec/def ::raum (spec/keys :req [:raum/name :raum/form :raum/gebaeude]))

;; (spec/def ::raum raeume)

(spec/def :lehrperson/pers-id (spec/and int? #(> % 0)))
(spec/def :lehrperson/name string?)
(spec/def :lehrperson/vorname string?)

(spec/def ::lehrperson (spec/keys :req [:lehrperson/pers-id]
                                  :opt [:lehrperson/name
                                        :lehrperson/vorname]))



;; Vorlesungzeit

(spec/def :vzeit/id (spec/and int? #(> % 0)))
(spec/def :vzeit/wochentag #{"Montag" "Dienstag" "Mittwoch" "Donnerstag" "Freitag" "Samstag" "Sonntag" "-"})

(spec/def :vzeit/start-zeit string?)
(spec/def :vzeit/end-zeit string?)
(spec/def :vzeit/start-datum string?)
(spec/def :vzeit/end-datum string?)
(spec/def :vzeit/rhythmus #{"wöchentlich" "Blockveranstaltung"})
(spec/def :vzeit/raum ::raum)

(spec/def ::vzeit (spec/keys :req [:vzeit/id]
                             :opt [:vzeit/start-datum
                                   :vzeit/end-datum
                                   :vzeit/rhythmus
                                   :vzeit/wochentag
                                   :vzeit/start-zeit
                                   :vzeit/raum
                                   :vzeit/end-zeit]))


(spec/def :veranstaltung/id (spec/and int? #(> % 0)))
(spec/def :veranstaltung/name string?)
(spec/def :veranstaltung/SWS (spec/and int? #(> % 0)))
(spec/def :veranstaltung/ECTS (spec/and int? #(> % 0)))
(spec/def :veranstaltung/max-teilnehmer (spec/and int? #(> % 1)))
(spec/def :veranstaltung/typ string?)
(spec/def :veranstaltung/semester string?)
(spec/def :veranstaltung/studiengang string?)
(spec/def :veranstaltung/lehrpersonen (spec/coll-of ::lehrperson))
(spec/def :veranstaltung/vzeiten (spec/coll-of ::vzeit))

(spec/def ::veranstaltung (spec/keys :req
                                     [:veranstaltung/id
                                      :veranstaltung/name
                                      :veranstaltung/typ
                                      :veranstaltung/semester
                                      :veranstaltung/lehrpersonen
                                      :veranstaltung/studiengang
                                      :veranstaltung/vzeiten]

                                     :opt
                                     [:veranstaltung/SWS
                                      :veranstaltung/ECTS
                                      :veranstaltung/max-teilnehmer
                                      ]))


(spec/def ::entity (spec/or :raum ::raum
                            :lehrperson ::lehrperson
                            :vzeit ::vzeit
                            :veranstaltung ::veranstaltung))

(spec/def ::entities (spec/coll-of ::entity))


;; define schema from specs


(def veranstaltungs-schema
  (spectomic/datomic-schema
   [[:veranstaltung/id {:db/unique :db.unique/identity}]
    :veranstaltung/name
    :veranstaltung/SWS
    :veranstaltung/ECTS
    :veranstaltung/max-teilnehmer
    :veranstaltung/typ
    :veranstaltung/semester
    :veranstaltung/studiengang
    :veranstaltung/lehrpersonen
    :veranstaltung/vzeiten]))

(def vzeit-schema
  (spectomic/datomic-schema
   [[:vzeit/id {:db/unique :db.unique/identity}]
    :vzeit/wochentag
    :vzeit/start-zeit
    :vzeit/end-zeit
    :vzeit/start-datum
    :vzeit/end-datum
    :vzeit/rhythmus
    :vzeit/raum]))

(def lehrperson-schema
  (spectomic/datomic-schema
   [[:lehrperson/pers-id {:db/unique :db.unique/identity}]
    :lehrperson/name
    :lehrperson/vorname]))

(def raum-schema
  (spectomic/datomic-schema
   [[:raum/name {:db/unique :db.unique/identity}]
    :raum/form
    :raum/gebaeude]))

(def schema
  (vec (concat
        veranstaltungs-schema
        vzeit-schema
        raum-schema
        lehrperson-schema)))

(defn generate-mock-db
  []
  (gen/generate (spec/gen ::entities)))
