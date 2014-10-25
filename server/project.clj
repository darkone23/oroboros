(defproject oroboros/server "0.2.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :source-paths ["src"]
  :test-paths ["test"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [oroboros/config "0.2.0-SNAPSHOT"]
                 [compojure "1.1.9"]]
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler oroboros.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [junit/junit "4.11"]
                        [ring-mock "0.1.5"]]}})
