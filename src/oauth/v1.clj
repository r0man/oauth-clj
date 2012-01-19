(ns oauth.v1
  (:refer-clojure :exclude (replace))
  (:require [clj-http.client :as http])
  (:use [clj-http.util :only (base64-encode url-encode url-decode)]
        [clojure.string :only (join replace split upper-case)]
        [inflections.core :only (underscore)]
        [inflections.transform :only (transform-keys)]
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

(defn oauth-signature-parameters
  "Returns the OAuth signature parameters from `request`."
  [request]
  (merge (parse-body-params request)
         (transform-keys (select-oauth-map request) (comp name underscore))
         (transform-keys (:query-params request) name)))

(defn oauth-parameter-string
  "Returns the OAuth parameter string from `request`."
  [request]
  (->> (oauth-signature-parameters request)
       (map #(str (percent-encode (first %1)) "=" (percent-encode (last %1))))
       (sort) (join "&")))

(defn oauth-signature-base-string
  "Returns the OAuth signature base string from `request`."
  [request]
  (->> [(format-http-method request)
        (percent-encode (format-base-url request))
        (percent-encode (oauth-parameter-string request))]
       (join "&")))

(defn oauth-signing-key
  "Returns the OAuth signing key."
  [oauth-consumer-secret oauth-token-secret]
  (str oauth-consumer-secret "&" oauth-token-secret))

(defn oauth-signature
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

(defn oauth-authorize-request [request]
  )

(defn wrap-oauth-request [client]
  (fn [request]
    (-> {:oauth-nonce (oauth-nonce)
         :oauth-signature-method *oauth-signature-method*
         :oauth-timestamp (str (oauth-timestamp))
         :oauth-version *oauth-version*}
        (merge request)
        (client))))

(defn wrap-oauth-sign-request [client]
  (fn [request]
    (client
     (assoc-in
      request [:headers "Authorization"]
      (oauth-signature-base-string request)))))

(def request
  (-> ;; http/request
   (fn [request]
     (assoc request :status 200 :body ""))
   (wrap-oauth-sign-request)
   (wrap-oauth-request)))

;; (request
;;  {:method :post
;;   :url "http://api.twitter.com/oauth/request_token"
;;   :oauth-callback "http://localhost:3005/the_dance/process_callback?service_provider_id=11"
;;   :oauth-consumer-key "GDdmIQH6jhtmLUypg82g" })
