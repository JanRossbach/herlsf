(ns herlsf.gui.style
  (:require
   [cljfx.css :as css])
  )

(def style
  (let [text (fn [size weight]
               {:-fx-text-fill "#111"
                :-fx-wrap-text true
                :-fx-font-weight weight
                :-fx-font-size size})]

    (css/register
     ::style
     {".app-label" (text 52 :normal)})))
