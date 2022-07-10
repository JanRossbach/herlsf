(ns herlsf.gui.events
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs])
  (:import [javafx.stage FileChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]))


(defmulti event-handler :event/type)

(defmethod event-handler :default
  [event]
  (prn event))

(defmethod event-handler ::open-xml-file
  [{:keys [^ActionEvent fx/event]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (FileChooser.)
                  (.setTitle "Open File"))]
    (when-let [file (.showOpenDialog chooser window)]
      {:xml (slurp file)})))

(defmethod event-handler ::select-veranstaltung
  [{:keys [:fx/event]}]
  (let [[id name] event]
    (prn (str "ID: " id " Name: " name))))

(defmethod event-handler ::navigate
  [{:keys [:fx/context panel new-view]}]
  {:context (fx/swap-context
             context
             (fn [c]
               (update-in c [:panels panel]
                          (fn [{:keys [history active-view]}]
                            {:history (conj history active-view)
                             :active-view new-view}))))})

(defmethod event-handler ::navigate-back
  [{:keys [fx/context panel]}]
  {:context (fx/swap-context
             context
             (fn [c]
               (update-in c [:panels panel]
                          (fn [{:keys [history]}]
                            {:history (pop history)
                             :active-view (peek history)}))))})
