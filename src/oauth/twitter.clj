(ns oauth.twitter
  (:require [oauth.v1 :as v1]))

(def ^:dynamic *oauth-access-token-url*
  "https://api.twitter.com/oauth/access_token")

(def ^:dynamic *oauth-authentication-url*
  "https://api.twitter.com/oauth/authenticate")

(def ^:dynamic *oauth-authorization-url*
  "https://api.twitter.com/oauth/authorize")

(def ^:dynamic *oauth-request-token-url*
  "https://api.twitter.com/oauth/request_token")

(defn oauth-access-token
  "Obtain a OAuth access token from Twitter."
  [oauth-consumer-key oauth-token oauth-verifier]
  (v1/oauth-access-token *oauth-access-token-url* oauth-consumer-key oauth-token oauth-verifier))

(defn oauth-authentication-url
  "Returns Twitter's OAuth authentication url."
  [oauth-token] (format "%s?oauth_token=%s" *oauth-authentication-url* oauth-token))

(defn oauth-authorization-url
  "Returns Twitter's OAuth authorization url."
  [oauth-token] (format "%s?oauth_token=%s" *oauth-authorization-url* oauth-token))

(defn oauth-authorize
  "Sends the user to Twitter's authorization endpoint."
  [oauth-token] (v1/oauth-authorize *oauth-authorization-url* oauth-token))

(defn oauth-client
  "Returns a OAuth Twitter client."
  [oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret]
  (v1/oauth-client oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret))

(defn oauth-request-token
  "Obtain a OAuth request token from Twitter to request user authorization."
  [oauth-consumer-key oauth-consumer-secret]
  (v1/oauth-request-token *oauth-request-token-url* oauth-consumer-key oauth-consumer-secret))
