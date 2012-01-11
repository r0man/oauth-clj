(ns oauth.v1
  (:require [clj-http.client :as http])
  (:use [clj-http.util :only (url-encode)]
        [clojure.string :only (join upper-case)]
        [inflections.core :only (underscore)]))

(def ^:dynamic *oauth-consumer-key* nil)

(def ^:dynamic *oauth-signature-method* "HMAC-SHA1")

(def ^:dynamic *oauth-version* "1.0")

(defn format-options [options]
  (map #(format "%s=\"%s\"" (underscore (name (first %1))) (url-encode (last %1))) options))

(defn format-header [options]
  (str "OAuth " (join ", " (format-options options))))

(defn root-url [{:keys [scheme server-name server-port]}]
  (str scheme "://" server-name (when server-port (str ":" server-port))))

(defn oauth-signature-base-string [request]
  (->> [(upper-case (name (or (:method request) (:request-method request))))
        (url-encode (root-url request))
        (url-encode (http/generate-query-string (:query-params request)))]
       (join "&")))

(defn oauth-request-signature [request]
  (oauth-signature-base-string request))
