(ns oauth.v1
  (:refer-clojure :exclude (replace))
  (:require [clj-http.client :as http])
  (:use [clj-http.util :only (base64-encode url-encode url-decode)]
        [clojure.string :only (join replace split upper-case)]
        [inflections.core :only (underscore)]
        oauth.util))

(def ^:dynamic *oauth-consumer-key* nil)

(def ^:dynamic *oauth-signature-method* "HMAC-SHA1")

(def ^:dynamic *oauth-version* "1.0")

(defn format-options [options]
  (map #(format "%s=\"%s\"" (underscore (name (first %1))) (url-encode (last %1))) options))

(defn format-header [options]
  (str "OAuth " (join ", " (format-options options))))

(defn root-url [{:keys [scheme server-name server-port]}]
  (str scheme "://" server-name (when server-port (str ":" server-port))))

(defn format-http-method [request]
  (upper-case (name (or (:method request) (:request-method request)))))

(defn format-base-url [request]
  (str (root-url request) (:uri request)))

(defn oauth-parameters
  "Returns the OAuth parameters from `request`."
  [request]
  (let [body (split (:body request) #"=")
        params  (reduce #(concat %1 [(name (first %2)) (str (last %2))]) nil (:query-params request))]
    (assoc (apply sorted-map (map url-decode (concat body params)))
      "oauth_consumer_key" (:oauth-consumer-key request)
      "oauth_nonce" (:oauth-nonce request)
      "oauth_signature_method" (:oauth-signature-method request)
      "oauth_timestamp" (:oauth-timestamp request)
      "oauth_token" (:oauth-token request)
      "oauth_version" (:oauth-version request))))

(defn oauth-parameter-string
  "Returns the OAuth parameter string from `request`."
  [request]
  (->> (oauth-parameters request)
       (map #(str (percent-encode (first %1)) "=" (percent-encode (last %1))))
       (join "&")))

(defn oauth-signature-base-string
  "Returns the OAuth signature base string from `request`."
  [request]
  (->> [(format-http-method request)
        (percent-encode (format-base-url request))
        (percent-encode (oauth-parameter-string request))]
       (join "&")))

(defn oauth-signing-key
  "Returns the OAuth signing key from `request`. The signing gets
  constructed from the :oauth-consumer-secret and :oauth-token-secret
  keys in `request`."
  [request] (str (:oauth-consumer-secret request) "&" (:oauth-token-secret request)))

(defn oauth-signature
  "Calculates the OAuth signature from `request`."
  [request]
  (-> (hmac "HmacSHA1"
            (oauth-signature-base-string request)
            (oauth-signing-key request))
      (base64-encode)))

(defn oauth-nonce
  "Returns the OAuth nonce."
  [] (random-base64 32))

(defn oauth-timestamp
  "Returns the current timestamp for an OAuth request."
  [] (.getTime (java.util.Date.)))
