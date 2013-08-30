(ns oauth.flickr
  (:require [oauth.v1 :as v1]))

(def ^:dynamic *oauth-access-token-url*
  "http://www.flickr.com/services/oauth/access_token")

(def ^:dynamic *oauth-authorization-url*
  "http://www.flickr.com/services/oauth/authorize")

(def ^:dynamic *oauth-request-token-url*
  "http://www.flickr.com/services/oauth/request_token")

(defn oauth-authorization-url
  "Returns Flickr's OAuth authorization url."
  [oauth-token] (format "%s?oauth_token=%s" *oauth-authorization-url* oauth-token))

(defn oauth-authorize
  "Sends the user to Flickr's authorization endpoint."
  [oauth-token] (v1/oauth-authorize *oauth-authorization-url* oauth-token))

(defn oauth-request-token
  "Obtain a OAuth request token from Flickr to request user authorization."
  [oauth-consumer-key oauth-consumer-secret oauth-callback]
  ((v1/make-consumer
    :oauth-consumer-key oauth-consumer-key
    :oauth-consumer-secret oauth-consumer-secret
    :oauth-callback oauth-callback)
   {:method :post :url *oauth-request-token-url*}))

(defn oauth-access-token
  "Obtain the OAuth access token from Flickr."
  [oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret oauth-verifier]
  ((v1/make-consumer
    :oauth-consumer-key oauth-consumer-key
    :oauth-consumer-secret oauth-consumer-secret
    :oauth-token oauth-token
    :oauth-token-secret oauth-token-secret
    :oauth-verifier oauth-verifier)
   {:method :post :url *oauth-access-token-url*}))

(defn oauth-client
  "Returns a OAuth Flickr client."
  [oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret]
  (v1/oauth-client oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret))
