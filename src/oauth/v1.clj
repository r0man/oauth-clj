(ns oauth.v1
  (:refer-clojure :exclude (replace))
  (:require [clj-http.client :as http])
  (:use [clj-http.util :only (url-encode url-decode)]
        [clojure.string :only (join replace split upper-case)]
        [inflections.core :only (underscore)]))

(def ^:dynamic *oauth-consumer-key* nil)

(def ^:dynamic *oauth-signature-method* "HMAC-SHA1")

(def ^:dynamic *oauth-version* "1.0")

(defn format-options [options]
  (map #(format "%s=\"%s\"" (underscore (name (first %1))) (url-encode (last %1))) options))

(defn format-header [options]
  (str "OAuth " (join ", " (format-options options))))

(defn percent-encode
  "Percent encode `unencoded` according to RFC 3986, Section 2.1."
  [unencoded]
  (-> (url-encode unencoded)
      (replace "%7E" "~")
      (replace "*" "%2A")
      (replace "+" "%20")))

(defn root-url [{:keys [scheme server-name server-port]}]
  (str scheme "://" server-name (when server-port (str ":" server-port))))

(defn format-http-method [request]
  (upper-case (name (or (:method request) (:request-method request)))))

(defn format-base-url [request]
  (str (root-url request) (:uri request)))

(defn oauth-signature-base-string [request]
  (->> [(format-http-method request)
        (url-encode (root-url request))
        (url-encode (http/generate-query-string (:query-params request)))]
       (join "&")))

(defn oauth-request-signature [request]
  (oauth-signature-base-string request))

(defn oauth-parameters [request]
  (let [body (split (:body request) #"=")
        params  (reduce #(concat %1 [(name (first %2)) (str (last %2))]) nil (:query-params request))]
    (assoc (apply sorted-map (map url-decode (concat body params)))
      "oauth_consumer_key" (:oauth-consumer-key request)
      "oauth_nonce" (:oauth-nonce request)
      "oauth_signature_method" (:oauth-signature-method request)
      "oauth_timestamp" (:oauth-timestamp request)
      "oauth_token" (:oauth-token request)
      "oauth_version" (:oauth-version request))))

(defn oauth-parameter-string [request]
  (->> (oauth-parameters request)
       (map #(str (percent-encode (first %1)) "=" (percent-encode (last %1))))
       (join "&")))

;; (prn (oauth-parameters
;;       {:method :post
;;        :scheme "https"
;;        :server-name "api.twitter.com"
;;        :uri "/1/statuses/update.json"
;;        :query-params {:include_entities true}
;;        :body "status=Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21"}))
