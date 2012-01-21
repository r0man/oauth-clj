(ns oauth.v2
  (:require [clj-http.client :as http])
  (:use [clojure.string :only (blank? join)]
        [inflections.core :only (hyphenize underscore)]
        [inflections.transform :only (transform-keys)]
        oauth.util))

(defn oauth-authorization-url
  "Returns the OAuth authorization url."
  [url client-id redirect-uri & {:as options}]
  (let [params
        (-> (assoc options :redirect-uri redirect-uri)
            (transform-keys (comp name underscore))
            (http/generate-query-string))]
    (if (blank? params) url (str url "?" params))))
