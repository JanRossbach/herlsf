(ns herlsf.xml
  (:require
   [clojure.spec.alpha :as spec]
   [herlsf.schema :as db]
   [clojure.data.xml :refer [parse]]
   [clojure.string :as string]
   [com.rpl.specter :as s]))

(defn xmlmap->map
  "Recursively translates given clojure map in xml format, given by data.xml,
  to a map that uses the tag as key and the content as value. Attributes are ignored."
  [xmlmap]
  (if (map? xmlmap)
    (let [{:keys [tag attrs content]} xmlmap
          content-map (map xmlmap->map content)
          res {tag (if (= 1 (count content-map)) (first content-map) (if (empty? content-map)
                                                                       nil
                                                                       content-map))}]
      (if (seq attrs)
        (assoc res :attrs attrs)
        res))
    xmlmap))

(defn veranstaltung?
  [m]
  (or
   (:Veranstaltung m)
   (:VeranstaltungErsteEbene m)
   ))

(defn validate
  [spec data]
  (when-let [error (spec/explain-data spec data)]
    (throw (ex-info
            "Failed validating the following: "
            error))))

;; Create Transactions from the data

(defmulti format-data (fn [d] (first (first d))))

(defmethod format-data
  :VZRaum
  [data]
  {:post [(nil? (validate ::db/raum %))]}
  (let [content (:VZRaum data)]
    (reduce
     (fn [a v]
       (cond
         (:VZRaumName v) (assoc a :raum/name (:VZRaumName v))
         (:VZRaumLangText v) (assoc a :raum/name (:VZRaumLangText v))
         (:VZRaumForm v) (assoc a :raum/form (:VZRaumForm v))
         (:GebKTxt v) (assoc a :raum/gebaeude (:GebKTxt v))
         (:GebDTxt v) (assoc a :raum/gebaeude (:GebDTxt v))
         :else a))
     {}
     content)))

(defmethod format-data
  :VZLehrPerson
  [data]
  {:post [(nil? (validate ::db/lehrperson %))]}
  (let [content (:VZLehrPerson data)]
    (reduce
     (fn [a v]
       (cond
         (:VPesrID v) (assoc a :lehrperson/pers-id (Long/parseLong (:VPesrID v)))
         (:DozName v) (assoc a :lehrperson/name (:DozName v))
         (:DozVorname v) (assoc a :lehrperson/vorname (:DozVorname v))
         :else a))
     {}
     content)))


(defmethod format-data
  :VZeit
  [data]
  {:post [(nil? (validate ::db/vzeit %))]}
  (let [content (:VZeit data)]
    (reduce
     (fn [a v]
       (cond
         (:veransttermin.vtid v) (assoc a :vzeit/id (Long/parseLong (:veransttermin.vtid v)))
         (:VZWoTag v) (assoc a :vzeit/wochentag (:VZWoTag v))
         (:VZBeginn v) (assoc a :vzeit/start-zeit (:VZBeginn v))
         (:VZEnde v) (assoc a :vzeit/end-zeit (:VZEnde v))
         (:VZBeginDat v) (assoc a :vzeit/start-datum (:VZBeginDat v))
         (:VZEndDat v) (assoc a :vzeit/end-datum (:VZEndDat v))
         (:VZRhythmus v) (assoc a :vzeit/rhythmus (:VZRhythmus v))
         (:VZRaum v) (assoc a :vzeit/raum (format-data v))
         :else a))
     {}
     content)))


(defmethod format-data
  :Veranstaltung
  [data]
  {:post [(nil? (validate ::db/veranstaltung %))]}
  (let [content (:Veranstaltung data)]
    (reduce
     (fn [a v]
       (cond
         (:VeranstNummer v) (assoc a :veranstaltung/id (Long/parseLong (:VeranstNummer v)))
         (:VName v) (assoc a :veranstaltung/name (:VName v))
         (:VSWS v) (assoc a :veranstaltung/SWS (Long/parseLong (:VSWS v)))
         (:VECTS v) (assoc a :veranstaltung/ECTS (Long/parseLong (:VECTS v)))
         (:MaxTeilnehmer v) (assoc a :veranstaltung/max-teilnehmer (Long/parseLong (:MaxTeilnehmer v)))
         (:VDTyp v) (assoc a :veranstaltung/typ (:VDTyp v))
         (:VZSemester v) (assoc a :veranstaltung/semester (:VZSemester v))
         (:studiengang v) (assoc a :veranstaltung/studiengang (:studiengang v))
         (:kurskategorie v) (assoc a :veranstaltung/kurskategorie (:kurskategorie v))
         (:VZLehrPerson v) (assoc a :veranstaltung/lehrpersonen (vec (conj (:veranstaltung/lehrpersonen v)
                                                                           (format-data v))))
         (:VZeit v) (assoc a :veranstaltung/vzeiten (vec (conj (:veranstaltung/vzeiten v)
                                                               (format-data v))))
         :else a))
     {:veranstaltung/lehrpersonen []
      :veranstaltung/vzeiten []}
     content)))


(defn add-elem-to-all-sub-veranstaltungen
  [vorlesung elem]
  (s/setval [(s/walker :Veranstaltung) :Veranstaltung s/BEFORE-ELEM]
            elem
            vorlesung))

(defn add-kurskategorie
  [vorlesung]
  (let [[kurskategorie] (s/select [(s/walker :UeBez) :UeBez] vorlesung)]
    (add-elem-to-all-sub-veranstaltungen vorlesung {:kurskategorie kurskategorie})))

(defn add-studiengang
  [vorlesung]
  (let [[studiengang] (s/select [(s/walker :UeBez) :UeBez] vorlesung)]
    (add-elem-to-all-sub-veranstaltungen vorlesung {:studiengang studiengang})))

(def ueebene (fn [e] (s/walker #(= e (:ueebene (:attrs %))))))

(defn xml->entities
  "Takes an xml string and returns a collection of
  app.db/entities. This can be put into the database as a transaction."
  [src]
  {:post [(spec/valid? ::db/entities %)]}
  (let [s (string/replace src #"\n[ ]*|\r" "")
        reader (java.io.StringReader. s)
        data (->> reader
                  parse
                  xmlmap->map
                  (s/transform [(ueebene "1")] add-studiengang)
                  (s/transform [(ueebene "2")] add-studiengang)
                  (s/transform [(ueebene "2")] add-kurskategorie)
                  (s/setval [(s/walker :VeranstaltungErsteEbene) :VeranstaltungErsteEbene s/BEFORE-ELEM]
                            {:studiengang "Erste Ebene"})
                  (s/transform (s/walker #(= % :VeranstaltungErsteEbene)) (fn [_] :Veranstaltung)))
        entities (s/select (s/walker veranstaltung?) data)]
    (mapv format-data entities)))
