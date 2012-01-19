(ns oauth.v1
  (:refer-clojure :exclude (replace))
  (:require [clj-http.client :as http])
  (:use [clj-http.util :only (base64-encode url-encode url-decode)]
        [clojure.string :only (join replace split upper-case)]
        [inflections.core :only (underscore)]
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

(defn format-option [[k v]]
  (format "%s=\"%s\"" (underscore (name k)) (url-encode (str v))))

(defn format-options [options]
  (map format-option (sort options)))

(defn format-authorization [options]
  (str "OAuth "(join ", " (format-options options))))

(defn root-url [{:keys [scheme server-name server-port]}]
  (str scheme "://" server-name (when server-port (str ":" server-port))))

(defn format-http-method [request]
  (upper-case (name (or (:method request) (:request-method request)))))

(defn format-base-url [request]
  (str (root-url request) (:uri request)))

(defn oauth-authorization-header
  "Returns the OAuth header of `request`."
  [request]
  (-> (oauth-map request)
      (transform-keys (comp name underscore))
      (format-authorization)))

(defn oauth-signature-parameters
  "Returns the OAuth signature parameters from `request`."
  [request]
  (merge (parse-body-params request)
         (transform-keys (select-keys request oauth-signature-keys) (comp name underscore))
         (transform-keys (:query-params request) name)))

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
  [request & [oauth-consumer-secret oauth-token-secret]]
  (-> (hmac "HmacSHA1"
            (oauth-signature-base-string request)
            (oauth-signing-key oauth-consumer-secret oauth-token-secret))
      (base64-encode)))

(defn oauth-nonce
  "Returns the OAuth nonce."
  [] (random-base64 32))

(defn oauth-timestamp
  "Returns the current timestamp for an OAuth request."
  [] (.getTime (java.util.Date.)))

(defn wrap-oauth-authorize-request
  "Returns a HTTP client that adds the OAuth authorization header to
  request."
  [client]
  (fn [request]
    (-> (assoc-in
         request [:headers "Authorization"]
         (oauth-authorization-header request))
        (client))))

(defn wrap-oauth-default-params
  "Returns a HTTP client with OAuth"
  [client & params]
  (fn [request]
    (->> {:oauth-nonce (oauth-nonce)
          :oauth-signature-method *oauth-signature-method*
          :oauth-timestamp (str (oauth-timestamp))
          :oauth-version *oauth-version*}
         (merge params request)
         (client))))

(defn wrap-oauth-sign-request
  "Returns a HTTP client that signs an OAuth request."
  [client]
  (fn [{:keys [oauth-consumer-secret oauth-token-secret] :as request}]
    (let [signature (oauth-request-signature request oauth-consumer-secret oauth-token-secret)]
      (client (assoc request :oauth-signature signature)))))

(defn make-consumer
  "Returns an OAuth consumer HTTP client."
  [oauth-keys]
  (-> http/request
      (wrap-oauth-authorize-request)
      (wrap-oauth-sign-request)
      (wrap-oauth-default-params oauth-keys)))

;; (def request (make-consumer "0NKq8e0RoSVR1kOmWcYyg" "1YwAbw0ZmwPjEQsGE6l0tkA9ifjSXJgkVxxrrgiZ0s"))

;; (request twitter-request-token)

;; (def request
;;   (-> ;; http/request
;;    (fn [request]
;;      (assoc request :status 200 :body ""))
;;    (wrap-oauth-authorize-request)
;;    (wrap-oauth-sign-request "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw" "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE")
;;    (wrap-oauth-default-params)))

;; (request
;;  {:method :post
;;   :scheme "https"
;;   :server-name "api.twitter.com"
;;   :uri "/1/statuses/update.json"
;;   :query-params {:include_entities true}
;;   :body "status=Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21"
;;   :oauth-consumer-key "xvz1evFS4wEEPTGEFPHBog"
;;   :oauth-nonce "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"
;;   :oauth-signature-method "HMAC-SHA1"
;;   :oauth-timestamp "1318622958"
;;   :oauth-token "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
;;   :oauth-version "1.0"})

;; (request
;;  {:method :post
;;   :url "http://api.twitter.com/oauth/request_token"
;;   :oauth-callback "http://localhost:3005/the_dance/process_callback?service_provider_id=11"
;;   :oauth-consumer-key "GDdmIQH6jhtmLUypg82g" })

;; (println
;;  (oauth-auth-headers
;;   {:method :post
;;    :scheme "https"
;;    :server-name "api.twitter.com"
;;    :uri "/1/statuses/update.json"
;;    :query-params {:include_entities true}
;;    :body "status=Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21"
;;    :oauth-consumer-key "xvz1evFS4wEEPTGEFPHBog"
;;    :oauth-nonce "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"
;;    :oauth-signature-method "HMAC-SHA1"
;;    :oauth-timestamp "1318622958"
;;    :oauth-token "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
;;    :oauth-version "1.0"}))
