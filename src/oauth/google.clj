(ns oauth.google
  (:require [clojure.string :refer [join]]
            [clojure.java.browse :refer [browse-url]]
            [oauth.v2 :as v2]
            [oauth.io :refer [request]]
            [oauth.util :refer [parse-body]]))

(def ^:dynamic *oauth-access-token-url*
  "https://accounts.google.com/o/oauth2/token")

(def ^:dynamic *oauth-authorization-url*
  "https://accounts.google.com/o/oauth2/auth")

(def ^:dynamic *oauth-scopes*
  {:email "https://www.googleapis.com/auth/userinfo.email"
   :profile "https://www.googleapis.com/auth/userinfo.profile"})

(def ^:dynamic *oauth-authorization-defaults*
  {:access-type "offline"
   :response-type "code"
   :scope (join " " (vals *oauth-scopes*))})

(defn user-info
  "Returns the Google user info about the current user."
  [client] (client {:method :get :url "https://www.googleapis.com/oauth2/v1/userinfo"}))

(defn oauth-authorization-url
  "Returns Google's OAuth authorization url using the default options."
  [client-id redirect-uri & {:as options}]
  (apply v2/oauth-authorization-url *oauth-authorization-url* client-id redirect-uri
         (mapcat concat (merge *oauth-authorization-defaults* options))))

(defn oauth-access-token
  "Obtain the OAuth access token from Google."
  [client-id client-secret code redirect-uri]
  (v2/oauth-access-token *oauth-access-token-url* client-id client-secret code redirect-uri))

(defn oauth-authorize
  "Sends the user to Google's authorization endpoint."
  [client-id redirect-uri & options]
  (browse-url (apply oauth-authorization-url client-id redirect-uri options)))

(defn oauth-client
  "Returns a Google OAuth client."
  [access-token] (v2/oauth-client access-token))

(defn scopes
  "Returns the Google OAuth scopes separated by a blank."
  [& scopes] (join " " (map *oauth-scopes* scopes)))
