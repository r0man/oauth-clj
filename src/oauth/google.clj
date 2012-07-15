(ns oauth.google
  (:require [clj-http.client :refer [request]]
            [clojure.java.browse :refer [browse-url]]
            [oauth.v2 :as v2]
            [oauth.util :refer [parse-body]]))

(def scopes
  {:email "https://www.googleapis.com/auth/userinfo.email"
   :profile "https://www.googleapis.com/auth/userinfo.profile"})

(def ^:dynamic *oauth-access-token-url*
  "https://accounts.google.com/o/oauth2/token")

(def ^:dynamic *oauth-authorization-url*
  "https://accounts.google.com/o/oauth2/auth")

(def ^:dynamic *oauth-authorization-defaults*
  {:access-type "offline" :response-type "code" :scope (:email scopes)})

(defn oauth-authorization-url
  "Returns Google's OAuth authorization url using the default options."
  [client-id redirect-uri & {:as options}]
  (let [options (merge *oauth-authorization-defaults* options)]
    (apply v2/oauth-authorization-url *oauth-authorization-url*
           client-id redirect-uri (mapcat concat options))))

(defn oauth-access-token
  "Obtain the OAuth access token from Google."
  [client-id client-secret code redirect-uri]
  (v2/oauth-access-token *oauth-access-token-url* client-id client-secret code redirect-uri))

(defn oauth-access-token
  "Obtain the OAuth access token from Google."
  [client-id client-secret code redirect-uri & [grant-type]]
  (-> {:method :post
       :url *oauth-access-token-url*
       :form-params
       {"client_id" client-id
        "client_secret" client-secret
        "code" code
        "redirect_uri" redirect-uri
        "grant_type" (or grant-type "authorization_code")}}
      request :body))

(defn oauth-authorize
  "Sends the user to Google's authorization endpoint."
  [client-id redirect-uri & options]
  (browse-url (apply oauth-authorization-url client-id redirect-uri options)))

(defn oauth-client
  "Returns a Google OAuth client."
  [access-token] (v2/oauth-client access-token))
