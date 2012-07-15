(defproject oauth-clj/oauth-clj "0.0.6-SNAPSHOT"
  :description "Clojure OAuth library."
  :min-lein-version "2.0.0"
  :dependencies [[clj-http "0.4.0"]
                 [inflections "0.7.1-SNAPSHOT"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/tools.logging "0.2.3"]]
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-log4j12 "1.6.4"]]
                   :resource-paths ["test-resources"]}})
