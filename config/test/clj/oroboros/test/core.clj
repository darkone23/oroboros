(ns oroboros.test.core
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [oroboros.core :refer :all]))

(deftest test-config

  (testing "can template regular maps"
    (is (= {:x "foo" :y "foo"} (template-map {:x "foo" :y "{{ x }}"}))))

  (testing "templating resolves to nearest ancestor"
    (let [m {:x 1 :y {:x 2, :y "{{ x }}"}}]
      (is (= 2 (get-in (template-map m) [:y :y])))))

  (testing "can template across several contexts"
    (let [m (template-map {:y "{{x}}"})]
      (is (= {:y "{{x}}"} m))
      (is (= {:x 23 :sub {:y 23}}
             (assoc (template-map {:x 23}) :sub m)))))

  (testing "can overlay one context onto another"
    (let [x (template-map {:foo [{:bar "{{ x }}"}]})
          y {:x "baz"}]
      (is (= {:foo [{:bar "baz"}]} (overlay x y)))))

  (testing "can find config names in a directory"
    (is (= #{"jerry"} (find-names "../examples/simple"))))

  (testing "can find config files in a directory"
    (is (= [(fs/file "../examples/simple/config.yaml")]
           (find-configs "../examples/simple")))

    (is (= [(fs/file "../examples/simple/config.yaml")
            (fs/file "../examples/simple/jerry.yaml")]
           (find-configs "../examples/simple" "foobar" "jerry"))))

  (testing "can load config files as template maps"
    (let [config (load-config "../examples/simple" "jerry")]
      (is (= {:cat "tom", :mouse "jerry", :name "jerry & tom"} config))))

  (testing "can recursively load templated configs"
    (is (= {:advanced {:web {:port 1337, :protocol "http", :host "web.example.com:1337",
                       :api, "http://web.example.com:1337/v/1.2.3",
                       :command "./bin/start --db db.example.com"}
                       :db {:host "db.example.com"},
                       :version "1.2.3"}
            :simple {:cat "tom", :mouse "jerry", :name "tom & jerry"}}
           (load-config "../examples")))

    (is (= {:web {:port 1337, :protocol "https", :host "expensive-server.example.com",
                  :api "https://expensive-server.example.com/v/1.2.3",
                  :command "./bin/start --db prod-db.example.com"},
            :db {:host "prod-db.example.com"}, :version "1.2.3"}
           (load-config "../examples/advanced" "production")))))
