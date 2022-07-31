(ns herlsf.core-test
  (:require [herlsf.core :as sut]
            [herlsf.schema :as schema]
            [clojure.spec.alpha :as spec]
            [cljfx.api :as fx]
            [herlsf.gui.events :as events]
            [herlsf.gui.subs :as subs]
            [datahike.api :as d]
            [clojure.test :as t])
  (:import [javafx.scene.input KeyEvent KeyCode]))

(def db-cfg {:store {:backend :mem
                     :id "test"}
             :initial-tx (concat schema/schema
                                 (schema/generate-mock-db))})

(if (not (d/database-exists? db-cfg))
  (d/create-database db-cfg)
  nil)

(def test-db-conn (d/connect db-cfg))


(t/deftest test-core
  (let [app (sut/run-app test-db-conn false)
        renderer (:renderer app)]
    (t/testing "General"
     (t/is (renderer) "Calling the renderer does not throw an exception."))
    (t/testing "Events"
      (t/is (renderer {:event/type ::navigate
                       :panel :konflikte
                       :new-view [:home "Graph"]})
            ()))))


(t/deftest subs-test
  (let [context (fx/create-context (sut/initial-state test-db-conn) identity)]
    (t/is (map? context))
    (t/is (spec/valid? (spec/coll-of (spec/tuple :db/id :veranstaltung/name)) (subs/veranstaltungen-filtered context "")))
    (t/is (spec/valid? (spec/coll-of (spec/tuple :db/id :raum/name)) (subs/raeume-filtered context "")))
    (t/is (spec/valid? (spec/coll-of (spec/tuple :db/id :veranstaltung/name :db/id :veranstaltung/name)) (subs/conflicts-filtered context "")))
    (t/is (= [:home ""] (subs/active-view context :veranstaltungen)))
    (t/is (= [:home ""] (subs/active-view context :raeume)))
    (t/is (= [:home ""] (subs/active-view context :konflikte)))
    (t/is (= "" (subs/search-text context :veranstaltungen)))
    (t/is (spec/valid? map? (subs/veranstaltung-details context (first (first (subs/veranstaltungen-filtered context ""))))))))


(t/deftest events-test
  (let [context (fx/create-context (sut/initial-state test-db-conn) identity)]
    (t/is (= [:details 123] (subs/active-view (:context (events/event-handler {:event/type ::events/navigate
                                                                               :fx/context context
                                                                               :panel :veranstaltungen
                                                                               :new-view [:details 123]}))
                                              :veranstaltungen))
          "Navigate Event Changes the active-view")
    (t/is (= "Graph" (subs/search-text (:context (events/event-handler {:event/type ::events/set-search-text
                                                                        :fx/context context
                                                                        :panel :veranstaltungen
                                                                        :fx/event "Graph"}))
                                       :veranstaltungen))
          "Changing the search text from Text Field Event sets the search-text correctly")
    (t/is (= {:event/type ::events/navigate
              :panel :veranstaltungen
              :new-view [:home "Graph"]}
             (:dispatch (events/event-handler {:event/type ::events/search-key-press
                                               :fx/context (:context (events/event-handler {:event/type ::events/set-search-text
                                                                                            :fx/context context
                                                                                            :panel :veranstaltungen
                                                                                            :fx/event "Graph"}))
                                               :panel :veranstaltungen
                                               :fx/event (KeyEvent. KeyEvent/KEY_PRESSED
                                                                    "Enter"
                                                                    "Enter"
                                                                    KeyCode/ENTER
                                                                    false
                                                                    false
                                                                    false
                                                                    false)})))
          "Confirm Search Event dispatches the correct navigation Event")
    (t/is (= [:home ""] (subs/active-view (:context (events/event-handler {:event/type ::events/navigate-back
                                                                           :fx/context (:context (events/event-handler {:event/type ::events/navigate
                                                                                                                        :fx/context context
                                                                                                                        :panel :veranstaltungen
                                                                                                                        :new-view [:details 123]}))
                                                                           :panel :veranstaltungen}))
                                          :veranstaltungen))
          "Navigating Back after navigating to details returns to home")
    (t/is (= [:details 123] (subs/active-view (:context (events/event-handler {:event/type ::events/navigate-forward
                                                                               :panel :veranstaltungen
                                                                               :fx/context (:context (events/event-handler {:event/type ::events/navigate-back
                                                                                                                            :fx/context (:context (events/event-handler {:event/type ::events/navigate
                                                                                                                                                                         :fx/context context
                                                                                                                                                                         :panel :veranstaltungen
                                                                                                                                                                         :new-view [:details 123]}))
                                                                                                                            :panel :veranstaltungen}))}))
                                              :veranstaltungen))
          "Navigating Forward after navigating back returns to the correct view")))
