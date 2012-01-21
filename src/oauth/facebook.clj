(ns oauth.facebook
  (:require [oauth.v2 :as v2]))

(def ^:dynamic *oauth-access-token-url*
  "https://graph.facebook.com/oauth/access_token")

(def ^:dynamic *oauth-authorization-url*
  "https://www.facebook.com/dialog/oauth")

(defn oauth-authorization-url
  "Returns Facebook's OAuth authorization url."
  [client-id redirect-uri & options]
  (apply v2/oauth-authorization-url *oauth-authorization-url* client-id redirect-uri options))

(defn oauth-access-token
  "Obtain the OAuth access token from Facebook."
  [client-id client-secret code redirect-uri]
  (v2/oauth-access-token *oauth-access-token-url* client-id client-secret code redirect-uri))

(defn oauth-authorize
  "Sends the user to Facebook's authorization endpoint."
  [client-id redirect-uri & options]
  (apply v2/oauth-authorize *oauth-authorization-url* client-id redirect-uri options))

(defn oauth-client
  "Returns a Facebook OAuth client."
  [access-token]
  (v2/make-consumer access-token))
