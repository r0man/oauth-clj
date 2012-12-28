(defproject oauth-clj "0.1.1"
  :description "Clojure OAuth library."
  :min-lein-version "2.0.0"
  :dependencies [[clj-http "0.6.3"]
                 [inflections "0.7.4"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.0"]]
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-log4j12 "1.6.6"]]
                   :resource-paths ["test-resources"]}})
