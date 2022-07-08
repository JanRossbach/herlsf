(ns herlsf.gui.events
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs])
  (:import [javafx.stage FileChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]))


(defmulti event-handler :event/type)

(defmethod event-handler :default
  [{:keys [:fx/event]}]
  (prn event))

(defmethod event-handler ::open-xml-file
  [{:keys [^ActionEvent fx/event]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (FileChooser.)
                  (.setTitle "Open File"))]
    (when-let [file (.showOpenDialog chooser window)]
      {:xml (slurp file)})))
