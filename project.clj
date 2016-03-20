(defproject oauth-clj "0.1.16-SNAPSHOT"
  :description "Clojure OAuth library."
  :url "https://github.com/r0man/oauth-clj"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[cheshire "5.5.0"]
                 [clj-http "2.1.0"]
                 [inflections "0.12.1"]
                 [org.clojure/clojure "1.8.0"]]
  :aliases {"ci" ["do" ["test"] ["lint"]]
            "lint" ["do"  ["eastwood"]]}
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-log4j12 "1.7.19"]]
                   :plugins [[jonase/eastwood "0.2.3"]
                             [lein-difftest "2.0.0"]]
                   :resource-paths ["test-resources"]}})
