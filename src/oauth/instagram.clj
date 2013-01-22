(ns oauth.instagram
  (:require [clojure.java.browse :refer [browse-url]]
            [oauth.v2 :as v2]))

(def ^:dynamic *oauth-access-token-url*
  "https://api.instagram.com/oauth/access_token")

(def ^:dynamic *oauth-authorization-url*
  "https://api.instagram.com/oauth/authorize")

(defn oauth-authorization-url
  "Returns Instagram's OAuth authorization url."
  [client-id redirect-uri & {:as options}]
  (apply v2/oauth-authorization-url *oauth-authorization-url* client-id redirect-uri
         (mapcat identity (assoc options :response-type "code"))))

(defn oauth-access-token
  "Obtain the OAuth access token from Instagram."
  [client-id client-secret code redirect-uri]
  (v2/oauth-access-token *oauth-access-token-url* client-id client-secret code redirect-uri))

(defn oauth-authorize
  "Sends the user to Instagram's authorization endpoint."
  [client-id redirect-uri & options]
  (browse-url
   (apply oauth-authorization-url client-id redirect-uri options)))

(defn oauth-client
  "Returns a Instagram OAuth client."
  [client-id & [access-token]]
  (-> (v2/oauth-client access-token)
      (v2/wrap-client-id client-id)))
