(ns oroboros.test.Config
  (:import [oroboros Config])
  (:require [clojure.test :refer :all]))

(deftest java-test
  (testing "works as a java library"
    (let [config (Config/circle "examples/simple")]
      (is (= {"cat" "tom", "mouse" "jerry",
              "name" "tom & jerry",
              "best" "{{favorite}}"} config))
      (is (instance? java.util.Map config))
      (is (instance? clojure.lang.Associative config))
      (is (= "circle"  (-> config
                           (.assoc "favorite" "circle")
                           (.get "best")))))))
