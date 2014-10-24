(ns oroboros.test.core
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [oroboros.core :refer :all]))

(deftest test-circle

  (testing "can template regular maps"
    (is (= {:x "foo" :y "foo"} (template-map {:x "foo" :y "{{ x }}"}))))

  (testing "can template across several contexts"
    (let [m (template-map {:y "{{x}}"})]
      (is (= {:y "{{x}}"} m))
      (is (= {:x 23 :sub {:y "23"}}
             (assoc (template-map {:x 23}) :sub m)))))

  (testing "can overlay one context onto another"
    (let [x {:foo {:bar "{{ x }}"}}
          y {:x "baz"}]
      (is (= {:foo {:bar "baz"}} (overlay x y)))))

  (testing "can find config names in a directory"
    (is (= #{"jerry"} (find-config-names "./examples/simple"))))

  (testing "can find config files in a directory"
    (is (= [(fs/file "./examples/simple/config.yaml")]
           (find-config-files "./examples/simple")))

    (is (= [(fs/file "./examples/simple/config.yaml")
            (fs/file "./examples/simple/jerry.yaml")]
           (find-config-files "./examples/simple" "foobar" "jerry"))))

  (testing "can load config files as template maps"
    (let [config (circle "./examples/simple" "jerry")]
      (is (= {:cat "tom", :mouse "jerry", :name "jerry & tom"} config))))

  (testing "can place loaded configs into a larger structure"
    (is (= {:examples {:simple {:cat "tom", :mouse "jerry",
                                :name "tom & jerry"}}}
           (load-config "./examples/simple/config.yaml" :examples :simple))))

  (testing "can turn directories into cursors"
    (is ( = [:examples :simple] (config-to-cursor "." "./examples/simple/tom.yaml"))))

  (testing "can recursively load templated configs"
    (is (= {:web {:port 1337, :protocol "http", :host "web.example.com:1337",
                  :api, "http://web.example.com:1337/v/1.2.3",
                  :command "./bin/start --db db.example.com"}
            :db {:host "db.example.com"}, :version "1.2.3"}
           (circle "./examples/advanced")))

    (is (= {:web {:port 1337, :protocol "https", :host "expensive-server.example.com",
                  :api "https://expensive-server.example.com/v/1.2.3",
                  :command "./bin/start --db prod-db.example.com"},
            :db {:host "prod-db.example.com"}, :version "1.2.3"}
           (circle "./examples/advanced" "production")))))
