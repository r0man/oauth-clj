(defproject oauth-clj "0.1.6-SNAPSHOT"
  :description "Clojure OAuth library."
  :url "https://github.com/r0man/oauth-clj"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-http "0.7.6"]
                 [inflections "0.8.2"]
                 [org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-log4j12 "1.7.5"]]
                   :resource-paths ["test-resources"]}})
