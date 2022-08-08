(ns herlsf.gui.components
  (:require
   [cljfx.api :as fx]
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]))


;; Namespace of reusable components

;; Navbar

(def quick-search-button-style ["btn" "btn-info" "btn-sm"])

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


;;
