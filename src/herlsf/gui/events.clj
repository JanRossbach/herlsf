(ns herlsf.gui.events
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs])
  (:import [javafx.stage FileChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]
           [javafx.scene.input KeyEvent KeyCode]
           [javafx.scene.control DialogEvent Dialog ButtonBar$ButtonData ButtonType]))


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

(defmethod event-handler ::navigate
  [{:keys [:fx/context panel new-view]}]
  {:context (fx/swap-context
             context
             (fn [c]
               (update-in c [:panels panel]
                          (fn [{:keys [history active-view] :as old-val}]
                            (assoc old-val
                                   :history (conj history active-view)
                                   :active-view new-view)))))})

(defmethod event-handler ::navigate-list
  [{:keys [panel fx/event]}]
  {:dispatch {:event/type ::navigate
              :panel panel
              :new-view [:details event]}})

(defmethod event-handler ::navigate-back
  [{:keys [fx/context panel]}]
  {:context (fx/swap-context
             context
             (fn [c]
               (update-in c [:panels panel]
                          (fn [{:keys [history back-history active-view] :as old-val}]
                            (if (seq history)
                              {:history (pop history)
                               :active-view (peek history)
                               :back-history (conj back-history active-view)}
                              old-val)))))})


(defmethod event-handler ::navigate-forward
  [{:keys [fx/context panel]}]
  {:context (fx/swap-context
             context
             (fn [c]
               (update-in c [:panels panel]
                          (fn [{:keys [history back-history active-view] :as old-val}]
                            (if (seq back-history)
                              {:history (conj history active-view)
                               :active-view (peek back-history)
                               :back-history (pop back-history)}
                              old-val)))))})

(defmethod event-handler ::set-search-text
  [{:keys [fx/context panel fx/event]}]
  {:context (fx/swap-context
             context
             (fn [c]
               (assoc-in c [:panels panel :search-text] event)))})


(defmethod event-handler ::search-key-press
  [{:keys [fx/context panel ^KeyEvent fx/event]}]
  (if (= KeyCode/ENTER (.getCode event))
    (let [search-text (fx/sub-ctx context subs/search-text panel)
          old-filter (fx/sub-ctx context subs/search-filter panel)
          new-view [:home (assoc old-filter :search-term search-text)]]
      {:dispatch {:event/type ::navigate
                  :panel panel
                  :new-view new-view}})
    {}))

(defmethod event-handler ::update-panel-filter
  [{:keys [fx/context panel key fx/event]}]
  (let [current-filter (fx/sub-ctx context subs/search-filter panel)
        new-filter (assoc current-filter key event)]
    {:dispatch {:event/type ::navigate
                :panel panel
                :new-view [:home new-filter]}}))

(defmethod event-handler ::delete-entity
  [{:keys [entity-id]}]
  {:transact [[:db/retractEntity entity-id]]})

(defmethod event-handler ::set-comp-state-by-event
  [{:keys [path fx/context fx/event]}]
  {:context (fx/swap-context context assoc-in path event)})

(defmethod event-handler ::show-confirmation
  [{:keys [fx/context state-id]}]
  {:context (fx/swap-context context assoc-in [:comp-state state-id :showing] true)})

(defmethod event-handler ::on-confirmation-dialog-hidden
  [{:keys [fx/context ^DialogEvent fx/event state-id on-confirmed]}]
  (println "Hello")
  (condp = (.getButtonData ^ButtonType (.getResult ^Dialog (.getSource event)))
    ButtonBar$ButtonData/CANCEL_CLOSE
    {:context (fx/swap-context context assoc-in [:comp-state state-id :showing] false)}

    ButtonBar$ButtonData/OK_DONE
    {:context (fx/swap-context context assoc-in [:comp-state state-id :showing] false)
     :dispatch on-confirmed}))
