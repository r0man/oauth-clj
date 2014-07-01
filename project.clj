(defproject oauth-clj "0.1.13-SNAPSHOT"
  :description "Clojure OAuth library."
  :url "https://github.com/r0man/oauth-clj"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[clj-http "0.9.2"]
                 [inflections "0.9.9"]
                 [noencore "0.1.15"]
                 [org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-log4j12 "1.7.7"]]
                   :resource-paths ["test-resources"]}})
