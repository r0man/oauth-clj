(ns oauth.etsy
  (:require [oauth.v1 :as v1]))

(def ^:dynamic *oauth-access-token-url*
  "https://openapi.etsy.com/v2/oauth/access_token")

;; Authorization url is set dynamically after a request-token call to Etsy
(def ^:dynamic *oauth-authorization-url*)

(def ^:dynamic *oauth-request-token-url*
  "https://openapi.etsy.com/v2/oauth/request_token")


(defn oauth-authorize
  "Sends the user to Etsy's authorization endpoint."
  [] (clojure.java.browse/browse-url (java.net.URLDecoder/decode *oauth-authorization-url*)))

(defn oauth-request-token
  "Obtain a OAuth request token from Etsy to request user authorization."
  [oauth-consumer-key oauth-consumer-secret oauth-callback scope]
  (let [request-token (v1/oauth-request-token *oauth-request-token-url* consumer-key consumer-secret {:query-params {:scope scope} :oauth-callback oauth-callback})]
    (alter-var-root #'*oauth-authorization-url* (fn [_] (:login-url request-token)))
    request-token))

(defn oauth-access-token
  "Obtain the OAuth access token from Etsy."
  [oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret oauth-verifier]
  ((v1/make-consumer
    :oauth-consumer-key oauth-consumer-key
    :oauth-consumer-secret oauth-consumer-secret
    :oauth-token oauth-token
    :oauth-token-secret oauth-token-secret
    :oauth-verifier oauth-verifier)
   {:method :post :url *oauth-access-token-url*}))

(defn oauth-client
  "Returns a OAuth Etsy client."
  [oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret]
  (v1/oauth-client oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret))
