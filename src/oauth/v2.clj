(ns oauth.v2
  (:require [clj-http.client :as http])
  (:use [clojure.java.browse :only (browse-url)]
        [clojure.string :only (blank? join)]
        [inflections.core :only (hyphenize underscore)]
        [inflections.transform :only (transform-keys)]
        oauth.util))

(defn oauth-authorization-url
  "Returns the OAuth authorization url."
  [url client-id redirect-uri & {:as options}]
  (let [params
        (-> (assoc options
              :client-id client-id
              :redirect-uri redirect-uri)
            (transform-keys (comp name underscore))
            (http/generate-query-string))]
    (if (blank? params) url (str url "?" params))))

(defn oauth-authorize
  "Send the user to the authorization url via `browse-url`."
  [url client-id redirect-uri & options]
  (browse-url (apply oauth-authorization-url url client-id redirect-uri options)))
