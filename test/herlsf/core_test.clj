(ns herlsf.core-test
  (:require [herlsf.core :as sut]
            [herlsf.schema :refer [schema]]
            [cljfx.context :as ctx]
            [clojure.test :as t]))

(def db-cfg {:store {:backend :mem
                     :id "test"}
             :initial-tx schema})

(t/deftest test-core
  (let [app (sut/run-app db-cfg false)
        renderer (:renderer app)]
    (t/testing "General"
     (t/is (renderer) "Calling the renderer does not throw an exception."))
    (t/testing "Events"
      (t/is (renderer {:event/type ::navigate
                       :panel :konflikte
                       :new-view [:home "Graph"]})
            ()))))

(def app (sut/run-app db-cfg false))

(def renderer (:renderer app))

(renderer {:event/type ::navigate})
