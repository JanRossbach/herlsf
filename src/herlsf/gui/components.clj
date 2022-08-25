(ns herlsf.gui.components
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]))


;; Namespace of reusable components


(defn button-with-confirmation-dialog
  [{:keys [fx/context
           state-id
           on-confirmed
           button
           dialog-pane]}]
  {:fx/type fx/ext-let-refs
   :refs {::dialog {:fx/type :dialog
                    :showing
                    (fx/sub-val context get-in [:comp-state state-id :showing] false)
                    :on-hidden {:event/type ::events/on-confirmation-dialog-hidden
                                :state-id state-id
                                :on-confirmed on-confirmed}
                    :dialog-pane (merge {:fx/type :dialog-pane
                                         :button-types [:cancel :ok]}
                                        dialog-pane)}}
   :desc (merge {:fx/type :button
                 :on-action {:event/type ::events/show-confirmation
                             :state-id state-id}}
                button)})


;; Navbar

(defmulti quick-search-bar identity)
(defmethod quick-search-bar :default
  [_]
  (fn [_]
    {:fx/type :label
     :text ""}))

(defmethod quick-search-bar :veranstaltungen
  [panel-name]
  (fn [{:keys [fx/context]}]
    {:fx/type :combo-box
     :value (fx/sub-ctx context subs/studiengang-filter-value)
     :style-class ["split-menu-btn" "split-menu-btn-primary"]
     :on-value-changed {:event/type ::events/update-panel-filter
                        :panel panel-name
                        :key :studiengang}
     :items (conj (fx/sub-ctx context subs/studiengaenge) "Studiengang")}))

(defn search-bar
  [{:keys [fx/context panel-name]}]
  (let [search-text (fx/sub-ctx context subs/search-text panel-name)
        old-filter (fx/sub-ctx context subs/search-filter panel-name)]
    {:fx/type :h-box
     :spacing 10
     :children [{:fx/type :button
                 :style-class ["btn" "btn-info" "btn-sm"]
                 :text "Search"
                 :on-action {:event/type ::events/navigate
                             :panel panel-name
                             :new-view [:home (assoc old-filter :search-term search-text)]}}
                {:fx/type :text-field
                 :text search-text
                 :on-text-changed {:event/type ::events/set-search-text
                                   :panel panel-name}
                 :on-key-pressed {:event/type ::events/search-key-press
                                  :panel panel-name}}
                {:fx/type (quick-search-bar panel-name)}
                ]}))

(defn back-button
  [panel-name]
  {:fx/type :button
   :alignment :baseline-left
   :text "Zurück"
   :style-class ["btn" "btn-danger" "btn-sm"]
   :on-action {:event/type ::events/navigate-back
               :panel panel-name}})

(defn forward-button
  [panel-name]
  {:fx/type :button
   :alignment :baseline-left
   :text "Weiter"
   :style-class ["btn" "btn-success" "btn-sm"]
   :on-action {:event/type ::events/navigate-forward
               :panel panel-name}})


(defn navbar
  [{:keys [panel-name search]}]
  {:fx/type :h-box
   :spacing 15
                                        ;:fill-width true
   :children [(back-button panel-name)
              (forward-button panel-name)
              (if search
                {:fx/type search-bar
                 :panel-name panel-name}
                {:fx/type :label :text ""})]})

;; Errors

(defn navigation-error-panel
  [panel-name active-view]
  (fn [_]
    {:fx/type :v-box
     :children
     [{:fx/type :label
       :text (str "Something went wrong with navigation for view: " active-view)}
      {:fx/type :button
       :text "Reset Panel"
       :style-class ["btn" "btn-danger"]
       :on-action {:event/type ::events/reset-panel
                   :panel panel-name}}]}))


;; CREATE

(defn create-button
  [{:keys [panel-name]}]
  {:fx/type :button
   :style-class ["btn" "btn-success"]
   :text "Neu"
   :on-action {:event/type ::events/navigate
               :panel panel-name
               :new-view [:create]}})

(defn submit-button
  [{:keys [fx/context state-id]}]
  (let [state (fx/sub-val context get-in [:comp-state state-id] {})]
    {:fx/type button-with-confirmation-dialog
     :dialog-pane {:content-text "Wirklich erstellen?"}
     :state-id ::submit-confirmation-dialog
     :button {:text "Bestätigen"
              :style-class ["btn" "btn-success" "btn-lg"]}
     :on-confirmed {:event/type ::events/create-entity
                    :state state}}))

(defn text-input
  [{:keys [label state-id fx/context]}]
  (let [path [:comp-state state-id (keyword label)]
        value (fx/sub-val context get-in path "")]
    {:fx/type :v-box
     :children
     [{:fx/type :label
       :text label}
      {:fx/type :text-field
       :text value
       :on-text-changed {:event/type ::events/set-comp-state-by-event
                         :path path}}]}))


(defn create-veranstaltung-form
  [{:keys [state-id]}]
  {:fx/type :v-box
   :spacing 10
   :padding 10
   :style-class ["container"]
   :children
   [{:fx/type text-input
     :label "Name"
     :state-id state-id}
    {:fx/type text-input
     :label "Typ"
     :state-id state-id}
    {:fx/type text-input
     :label "SWS"
     :state-id state-id}
    {:fx/type text-input
     :label "ECTS"
     :state-id state-id}
    {:fx/type text-input
     :label "Studiengang"
     :state-id state-id}
    {:fx/type text-input
     :label "Kategorie"
     :state-id state-id}]})

(defn create-lehrperson-form
  []
  )

(defn create-vzeit-form
  []
  )

(defn create-raum-form
  []
  )

;; READ

(defn- lehrperson->string
  [{:keys [:lehrperson/name :lehrperson/vorname]}]
  (str vorname " " name))

(defn veranstaltung-details
  [panel-name id]
  (fn [{:keys [fx/context]}]
    (let [v (fx/sub-ctx context subs/veranstaltung-details id)]
      {:fx/type :v-box
       :spacing 10
       :padding 10
       :children [{:fx/type navbar
                   :panel-name panel-name
                   :search false}
                  {:fx/type :label
                   :style-class "h2"
                   :text (str (:veranstaltung/name v))}
                  {:fx/type :label
                   :text (str "Verantwortliche Personen: "
                              (apply str (map lehrperson->string
                                              (:veranstaltung/lehrpersonen v))))}
                  {:fx/type :label
                   :text (str "Studiengang: " (:veranstaltung/studiengang v))}
                  {:fx/type :label
                   :text (str "SWS: " (:veranstaltung/SWS v))}
                  {:fx/type :label
                   :text (str "Teilnehmergrenze: " (if (:veranstaltung/max-teilnemher v)
                                                     (:veranstaltung/max-teilnehmer v)
                                                     "Keine"))}
                  {:fx/type :label
                   :text (str "Veranstaltungstyp: " (:veranstaltung/typ v))}
                  {:fx/type :label
                   :text (if (:veranstaltung/ECTS v)
                           (str "ECTS: " (:veranstaltung/ECTS v))
                           "")}]})))

;; Update

;; Delete

(defn delete-button
  [{:keys [state-id entity-id]}]
  {:fx/type button-with-confirmation-dialog
   :state-id state-id
   :dialog-pane {:content-text "Wirklich löschen?"}
   :button {:text "Delete"
            :style-class ["btn" "btn-danger"]}
   :on-confirmed {:event/type ::events/delete-entity
                  :entity-id entity-id}})
