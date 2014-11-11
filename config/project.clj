(defproject oroboros/config "0.2.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :test-paths ["test/clj" "test/jvm"]
  :java-source-paths ["src/jvm"]
  :junit ["test/jvm"]
  :aot :all
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-http "1.0.0"]
                 [me.raynes/fs "1.4.6"]
                 [circleci/clj-yaml "0.5.3"]
                 [eggsby/stencil "0.3.4-eggsby"]
                 [matross/mapstache "0.3.3"]]
  :plugins [[lein-junit "1.1.2"]]
  :profiles
  {:dev {:dependencies [[junit/junit "4.11"]]}})
