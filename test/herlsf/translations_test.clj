(ns herlsf.translations-test
  (:require [herlsf.translation :as sut]
            [clojure.test :as t]))

(def example-v-comp-state
  {:Name "Graphentheorie"
   :Typ "Vorlesung/Übung/Praktikum"
   :Studiengang "Bachelor Informatik"
   :Kategorie "Wahlpflicht- und Schwerpunktmodule (Bachelor-Studiengang)"})

(def example-v-entity
  {:veranstaltung/kurskategorie "Wahlpflicht- und Schwerpunktmodule (Bachelor-Studiengang)",
   :veranstaltung/vzeiten [#:db{:id 220}],
   :veranstaltung/typ "Vorlesung/Übung/Praktikum",
   :veranstaltung/semester "WiSe 2022/23",
   :veranstaltung/name "Grundlagen der Computernetzwerke (vormals: Rechnernetze)",
   :db/id 219,
   :veranstaltung/lehrpersonen [#:db{:id 196}],
   :veranstaltung/studiengang "Bachelor Informatik",
   :veranstaltung/id 233080})

(t/deftest comp-state->entity-test
  (t/are [comp-state entity] (= entity (sut/comp-state->entity comp-state))
    example-v-comp-state
    example-v-entity))
