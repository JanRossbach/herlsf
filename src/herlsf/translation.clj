(ns herlsf.translation
  (:require
   [clojure.spec.alpha :as spec]
   [herlsf.schema :as schema]
   [clojure.set :as set]))

(def translate
  {:Name :veranstaltung/name
   :Typ :veranstaltung/typ
   :SWS :veranstaltung/SWS})


(def translate-back
  (set/map-invert translate))

(defmulti comp-state->entity identity)
(defmulti entity->comp-state identity)

(defmethod comp-state->entity :SWS ;; Nur Veranstaltungen haben SWS
  [comp-state]
  {:post [(spec/valid? ::schema/entity %)]}
  comp-state)

(defmethod entity->comp-state :veranstaltung/name
  [entity]
  {:pre [(spec/valid? ::schema/entity entity)]}
  entity)
