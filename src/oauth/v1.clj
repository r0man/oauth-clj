(ns oauth.v1
  (:refer-clojure :exclude (replace))
  (:require [clj-http.client :as http])
  (:use [clj-http.util :only (base64-encode)]
        [clojure.string :only (join replace)]
        [inflections.transform :only (transform-keys)]
        oauth.util))

(def ^:dynamic *oauth-signature-method* "HMAC-SHA1")

(def ^:dynamic *oauth-version* "1.0")

(def oauth-signature-keys
  #{:oauth-consumer-key
    :oauth-nonce
    :oauth-signature-method
    :oauth-timestamp
    :oauth-token
    :oauth-version})

(defn oauth-authorization-header
  "Returns the OAuth header of `request`."
  [request]
  (-> (merge (oauth-params (dissoc request :oauth-consumer-secret :oauth-token-secret))
             (oauth-params (:query-params request)))
      (format-authorization)))

(defn oauth-signature-parameters
  "Returns the OAuth signature parameters from `request`."
  [request]
  (-> (merge (parse-body-params request)
             (oauth-params (dissoc request :oauth-consumer-secret :oauth-token-secret))
             (transform-keys (:query-params request) name))
      (compact-map)))

(defn oauth-parameter-string
  "Returns the OAuth parameter string from `request`."
  [request] (format-params (oauth-signature-parameters request)))

(defn oauth-signature-base-string
  "Returns the OAuth signature base string from `request`."
  [request]
  (->> [(format-http-method request)
        (percent-encode (format-base-url request))
        (percent-encode (oauth-parameter-string request))]
       (join "&")))

(defn oauth-signing-key
  "Returns the OAuth signing key."
  [key secret] (str key "&" secret))

(defn oauth-request-signature
  "Calculates the OAuth signature from `request`."
  [request & [consumer-secret token-secret]]
  (-> (hmac "HmacSHA1"
            (oauth-signature-base-string request)
            (oauth-signing-key
             (or consumer-secret (:oauth-consumer-secret request))
             (or token-secret (:oauth-token-secret request))))
      (base64-encode)))

(defn oauth-nonce
  "Returns a random OAuth nonce. The OAuth nonce is a unique token an
  application should generate for each unique request."
  [] (replace (random-base64 32) #"(?i)[^a-z0-9]" ""))

(defn oauth-timestamp
  "Returns the current OAuth timestamp. The current time in seconds
  since the Unix epoch."
  [] (int (/ (.getTime (java.util.Date.)) 1000)))

(defn oauth-sign-request
  "Sign the OAuth request with `key` and `secret`."
  [request consumer-secret & [token-secret]]
  (let [signature (oauth-request-signature request consumer-secret token-secret)]
    (assoc request :oauth-signature signature)))

(defn wrap-oauth-authorize-request
  "Returns a HTTP client that adds the OAuth authorization header to
  request."
  [client]
  (fn [request]
    (clojure.pprint/pprint request)
    (-> (assoc-in
         request [:headers "Authorization"]
         (oauth-authorization-header request))
        (client))))

(defn wrap-oauth-default-params
  "Returns a HTTP client with OAuth"
  [client & [params]]
  (fn [request]
    (->> {:oauth-nonce (oauth-nonce)
          :oauth-signature-method *oauth-signature-method*
          :oauth-timestamp (str (oauth-timestamp))
          :oauth-version *oauth-version*}
         (merge params request)
         (client))))

(defn wrap-oauth-sign-request
  "Returns a HTTP client that signs an OAuth request."
  [client & [consumer-secret token-secret]]
  (fn [request]
    (client (oauth-sign-request request consumer-secret token-secret))))

(defn make-consumer
  "Returns an OAuth consumer HTTP client."
  [oauth-keys]
  (-> clj-http.core/request
      (wrap-oauth-authorize-request)
      (wrap-oauth-sign-request)
      (wrap-oauth-default-params oauth-keys)
      (http/wrap-request)))
