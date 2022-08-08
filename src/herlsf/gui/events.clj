(ns herlsf.gui.events
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs])
  (:import [javafx.stage FileChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]
           [javafx.scene.input KeyEvent KeyCode]))


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
  (let [[id _] event
        new-view [:details id]]
    {:dispatch {:event/type ::navigate
                :panel panel
                :new-view new-view}}))

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


;; (defmethod event-handler ::reset-panel
;;   [{:keys [fx/context panel]}]
;;   {:context (fx/swap-context
;;              context
;;              (fn [c]
;;                (assoc-in c [:panels panel] (herlsf.core/initial-state))))})
