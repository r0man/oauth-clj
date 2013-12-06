(ns oauth.github
  (:require [clojure.java.browse :refer [browse-url]]
            [oauth.v2 :as v2]))

(def ^:dynamic *oauth-access-token-url*
  "https://github.com/login/oauth/access_token")

(def ^:dynamic *oauth-authorization-url*
  "https://github.com/login/oauth/authorize")

(defn oauth-authorization-url
  "Returns Github's OAuth authorization url."
  [client-id redirect-uri & options]
  (apply v2/oauth-authorization-url *oauth-authorization-url* client-id redirect-uri options))

(defn oauth-access-token
  "Obtain the OAuth access token from Github."
  [client-id client-secret code redirect-uri]
  (v2/oauth-access-token *oauth-access-token-url* client-id client-secret code redirect-uri))

(defn oauth-authorize
  "Sends the user to Github's authorization endpoint."
  [client-id redirect-uri & options]
  (browse-url (apply oauth-authorization-url client-id redirect-uri options)))

(defn oauth-client
  "Returns a Github OAuth client."
  [access-token] (v2/oauth-client access-token))
