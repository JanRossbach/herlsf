(ns herlsf.core
  (:require
   [herlsf.gui.core :as gui])
  (:gen-class))

(defn -main
  []
  (gui/run-app))
