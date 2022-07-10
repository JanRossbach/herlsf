(ns herlsf.gui.views.menubar)

(defn file-menu
  [{:keys [fx/context]}]
  {:fx/type :menu
   :text "Files"
   :items [{:fx/type :menu-item
            :text "Import xml file"
            :on-action {:event/type ::dummy}}
           {:fx/type :menu-item
            :text "Open"}]})

(defn settings-menu
  [{:keys [fx/context]}]
  {:fx/type :menu
   :text "Settings"
   :items [{:fx/type :menu-item
            :text "Open Settings"
            :on-action {:event/type ::dummy}}
           {:fx/type :menu-item
            :text "Open"}]})


(defn menubar
  [_]
  {:fx/type :menu-bar
   :menus [{:fx/type file-menu}
           {:fx/type settings-menu}]})
