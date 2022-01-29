(ns oauth.strava
  (:require [oauth.v2 :as oauth])
  (:gen-class))

(def ^:dynamic *oauth-authorize-url* "https://www.strava.com/oauth/authorize")
(def ^:dynamic *oauth-access-token-url* "https://www.strava.com/oauth/token")
(def ^:dynamic *strava-endpoint-url* "https://www.strava.com/api/v3/")
(def ^:dynamic *redirect-url* "http://localhost/")

(defn oauth-authorize [key & redirect]
  (oauth/oauth-authorize *oauth-authorize-url* key (or redirect *redirect-url*) :response-type "code"))

(defn oauth-access-token [key secret code & redirect]
  (oauth/oauth-access-token *oauth-access-token-url*
                            key
                            secret
                            code
                            (or redirect *redirect-url*)))

(defn oauth-client [{:keys [access-token]}]
  (oauth/oauth-client access-token))
