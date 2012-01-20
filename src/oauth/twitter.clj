(ns oauth.twitter
  (:require [oauth.v1 :as v1])
  (:use oauth.util))

(def ^:dynamic *oauth-access-token-url*
  "https://api.twitter.com/oauth/access_token")

(def ^:dynamic *oauth-authorization-url*
  "https://api.twitter.com/oauth/authorize")

(def ^:dynamic *oauth-request-token-url*
  "https://api.twitter.com/oauth/request_token")

(defn oauth-authorize
  "Sends the user to Twitter's authorization endpoint."
  [oauth-token] (v1/oauth-authorize *oauth-authorization-url* oauth-token))

(defn oauth-request-token
  "Obtain a OAuth request token from Twitter to request user authorization."
  [consumer-key consumer-secret]
  (v1/oauth-request-token *oauth-request-token-url* consumer-key consumer-secret))

;; (let [response (oauth-request-token "qcz2O57srPsb5eZA2Jyw" "lfs5WjmIzPc3OlDNoHSfbxVBmPNmduTDq4rQHhNN7Q")]
;;   (prn response))
