(ns herlsf.gui.views.button-row
  (:require
   [herlsf.gui.subs :as subs]
   [herlsf.gui.events :as events]
   [cljfx.api :as fx]))

(defmulti button-row (fn [[panel-kw]] panel-kw))

(def home-buttons
  {:fx/type :h-box
   :spacing 5
   :children
   [{:fx/type :button
     :text "Back"
     :style-class ["btn" "btn-danger"]
     :on-action {:event/type ::events/navigate
                 :target [:home]}}
    {:fx/type :button
     :text "Info"
     :style-class ["btn" "btn-info"]
     :on-action {:event/type ::events/navigate
                 :target [:other]}}
    {:fx/type :button
     :text "Success"
     :style-class ["btn" "btn-success"]
     :on-action {:event/type ::events/navigate
                 :target [:other]}}]})

(defmethod button-row :home [_] home-buttons)

(def other-buttons
  {:fx/type :h-box
   :spacing 5
   :children
   [{:fx/type :button
     :text "Success"
     :style-class ["btn" "btn-success"]
     :on-action {:event/type ::events/navigate
                 :target [:home]}}]})

(defmethod button-row :other [_] other-buttons)
