(ns oauth.patreon
  (:require [clojure.java.browse :refer [browse-url]]
            [oauth.v2 :as v2]))

(def ^:dynamic *oauth-access-token-url*
  "https://www.patreon.com/api/oauth2/token")

(def ^:dynamic *oauth-authorization-url*
  "https://www.patreon.com/oauth2/authorize")

(defn oauth-authorization-url
  "Returns Patreon's OAuth authorization url."
  [client-id redirect-uri & options]
  (str (apply v2/oauth-authorization-url *oauth-authorization-url* client-id redirect-uri options) "&response_type=code"))

(defn oauth-access-token
  "Obtain the OAuth access token from Patreon."
  [client-id client-secret code redirect-uri]
  (v2/oauth-access-token *oauth-access-token-url* client-id client-secret code redirect-uri))

(defn oauth-authorize
  "Sends the user to Patreon's authorization endpoint."
  [client-id redirect-uri & options]
  (browse-url (apply oauth-authorization-url client-id redirect-uri options)))

(defn oauth-client
  "Returns a Patreon OAuth client."
  [access-token] (v2/oauth-client access-token))
