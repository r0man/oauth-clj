(ns oauth.v1
  (:refer-clojure :exclude (replace))
  (:use [clj-http.client :only (wrap-request wrap-url)]
        [clj-http.core :only (request)]
        [clj-http.util :only (base64-encode)]
        [clojure.java.browse :only (browse-url)]
        [clojure.string :only (join replace)]
        [inflections.transform :only (transform-keys)]
        oauth.util))

(def ^:dynamic *oauth-signature-method* "HMAC-SHA1")

(def ^:dynamic *oauth-version* "1.0")

(defn oauth-authorization-header
  "Returns the OAuth header of `request`."
  [request]
  (-> (merge (oauth-params (dissoc request :oauth-consumer-secret :oauth-token-secret))
             (oauth-params (:query-params request)))
      (format-authorization)))

(defn oauth-authorize
  "Send the user to the authorization url via `browse-url`."
  [url oauth-token] (browse-url (format "%s?oauth_token=%s" url oauth-token)))

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

(defn oauth-signature-base
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
            (oauth-signature-base request)
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
  "Sign the OAuth request with `consumer-key` and `token-secret`."
  [request consumer-secret & [token-secret]]
  (let [signature (oauth-request-signature request consumer-secret token-secret)]
    (assoc request :oauth-signature signature)))

(defn- update-authorization-header
  "Update the OAuth authorization header in `request`."
  [request]
  (assoc-in request [:headers "Authorization"]
            (oauth-authorization-header request)))

(defn wrap-oauth-authorization
  "Returns a HTTP client that adds the OAuth authorization header to request."
  [client] (fn [request] (client (update-authorization-header request))))

(defn oauth-callback-confirmed?
  "Returns true if the :oauth-callback-confirmed key in
  `request-token` is true, otherwise false."
  [request-token] (Boolean/parseBoolean (str (:oauth-callback-confirmed request-token))))

(defn wrap-oauth-defaults
  "Returns a HTTP client with OAuth"
  [client & [params]]
  (fn [request]
    (->> {:oauth-nonce (oauth-nonce)
          :oauth-signature-method *oauth-signature-method*
          :oauth-timestamp (str (oauth-timestamp))
          :oauth-version *oauth-version*}
         (merge params request)
         (client))))

(defn wrap-oauth-signature
  "Returns a HTTP client that signs an OAuth request."
  [client & [consumer-secret token-secret]]
  (fn [request] (client (oauth-sign-request request consumer-secret token-secret))))

(defn make-consumer
  "Returns an OAuth consumer HTTP client."
  [& {:as oauth-defaults}]
  (-> request
      (wrap-request)
      (wrap-content-type x-www-form-urlencoded)
      (wrap-oauth-authorization)
      (wrap-oauth-signature)
      (wrap-url)
      (wrap-oauth-defaults oauth-defaults)
      (wrap-decode-response)))

(defn oauth-access-token
  "Obtain the OAuth access token."
  [url oauth-consumer-key oauth-token oauth-verifier]
  (-> ((make-consumer
        :oauth-consumer-key oauth-consumer-key
        :oauth-token oauth-token
        :oauth-verifier oauth-verifier)
       {:method :post :url url})
      parse-body))

(defn oauth-request-token
  "Obtain the OAuth request token to request user authorization."
  [url oauth-consumer-key oauth-consumer-secret]
  (-> ((make-consumer
        :oauth-consumer-key oauth-consumer-key
        :oauth-consumer-secret oauth-consumer-secret)
       {:method :post :url url})
      parse-body))

(defn oauth-client
  "Returns a HTTP client for version 1 of the OAuth protocol."
  [oauth-consumer-key oauth-consumer-secret oauth-token oauth-token-secret]
  (make-consumer
   :oauth-consumer-key oauth-consumer-key
   :oauth-consumer-secret oauth-consumer-secret
   :oauth-token oauth-token
   :oauth-token-secret oauth-token-secret))
