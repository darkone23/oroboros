(defproject oroboros "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [me.raynes/fs "1.4.6"]
                 [circleci/clj-yaml "0.5.3"]
                 [stencil "0.3.5"]
                 [matross/mapstache "0.3.1-SNAPSHOT"]
                 [compojure "1.1.9"]]
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler oroboros.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
