(defproject oroboros/config "0.2.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :test-paths ["test/clj" "test/jvm"]
  :java-source-paths ["src/jvm"]
  :junit ["test/jvm"]
  :aot :all
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [cpath-clj "0.1.2"]
                 [me.raynes/fs "1.4.6" :exclusions [org.apache.commons/commons-compress]]
                 [circleci/clj-yaml "0.5.3"]
                 [eggsby/stencil "0.3.4-eggsby"]
                 [matross/mapstache "0.3.3"]]
  :plugins [[lein-junit "1.1.8"]]
  :profiles
  {:dev {:resource-paths ["test/fixtures"]
         :dependencies [[junit/junit "4.11"]]}})
