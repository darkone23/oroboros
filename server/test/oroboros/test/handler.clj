(ns oroboros.test.handler
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [oroboros.handler :refer :all]
            [ring.mock.request :as mock]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) (fs/file "resources/public/index.html")))))
  
  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
