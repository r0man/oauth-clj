(ns oauth.util
  (:refer-clojure :exclude [replace])
  (:require [clj-http.client :as http]
            [clj-http.util :refer [base64-encode url-encode url-decode]]
            [clojure.data.json :refer [read-str]]
            [clojure.string :refer [blank? join replace split upper-case]]
            [inflections.core :refer [hyphenize underscore]]
            [inflections.transform :refer [transform-keys transform-values]])
  (:import java.security.SecureRandom
           javax.crypto.Mac
           javax.crypto.spec.SecretKeySpec
           org.apache.http.entity.StringEntity))

(def x-www-form-urlencoded "application/x-www-form-urlencoded")

(defn byte-array?
  "Returns true if `arg` is a byte array, otherwise false."
  [arg] (instance? (Class/forName "[B") arg))

(defn compact-map
  "Returns a `map` with all entries removed, where the entrie's value
  matches `pred`."
  [map & [pred]]
  (let [pred (or pred nil?)]
    (reduce
     #(if (pred (get map %2)) %1 (assoc %1 %2 (get map %2)))
     {} (keys map))))

(defn content-type
  "Returns the content type of `response`."
  [response]
  (first (split (get (:headers response) "content-type") #";")))

(defn decode-body
  "Returns the :body key of `request`. If body is an instance of
  clojure.lang.IMeta, the rest of response will be attached as meta
  data."
  [{:keys [body] :as response}]
  (if (instance? clojure.lang.IMeta body)
    (with-meta body (dissoc response :body))
    body))

(defn decode-json
  "Docode the body of `response` via `read-str` and attach the rest
  of the response as meta data."
  [response]
  (if (string? (:body response))
    (-> (decode-body (assoc response :body (read-str (:body response) :key-fn keyword)))
        (transform-keys hyphenize))
    (decode-body response)))

(defmulti decode-response
  "Decode `response` according to the content-type header."
  (fn [response] (content-type response)))

(defmethod decode-response "application/json" [response]
  (decode-json response))

(defmethod decode-response "text/javascript" [response]
  (decode-json response))

(defmethod decode-response :default [response]
  (decode-body response))

(defn format-option [[k v]]
  (format "%s=\"%s\"" (underscore (name k)) (url-encode (str v))))

(defn format-options [options]
  (map format-option (sort options)))

(defn format-authorization [options]
  (str "OAuth "(join ", " (format-options options))))

(defn format-query-params [params]
  (let [params (compact-map params)]
    (if-not (empty? params)
      (->> (transform-keys params #(if (string? %1) %1 (-> %1 name underscore)))
           seq flatten (apply sorted-map)
           (http/generate-query-string)))))

(defn root-url [{:keys [scheme server-name server-port]}]
  (str (name scheme) "://" server-name (when (and server-port
                                                  (not (#{80 443} server-port)))
                                         (str ":" server-port))))

(defn format-http-method [request]
  (upper-case (name (or (:method request) (:request-method request)))))

(defn format-base-url [request]
  (str (root-url request) (:uri request)))

(defn hmac
  ([^String algorithm ^String msg ^String key]
     (hmac algorithm msg key "UTF8"))
  ([^String algorithm ^String msg ^String key ^String encoding]
     (let [key (SecretKeySpec. (.getBytes key "UTF8") algorithm)
           mac (doto (Mac/getInstance algorithm)
                 (.init key))]
       (.doFinal mac (.getBytes msg encoding)))))

(defn- to-str [obj]
  (cond
   (nil? obj) nil
   (instance? StringEntity obj)
   (let [stream (.getContent (.clone obj))
         buffer (byte-array (.available stream))]
     (.read stream buffer)
     (String. buffer))
   (byte-array? obj)
   (String. obj)
   :else (str obj)))

(defn parse-body
  "Parse `body` and return a map with hypenized keys and their values."
  [body]
  (reduce
   #(let [[k v] (split %2 #"=")]
      (assoc %1 (hyphenize (keyword k)) v))
   {} (split body #"&")))

(defn parse-body-params
  "Parse the body of `request` as an URL encoded parameter list."
  [request]
  (let [body (to-str (:body request))]
    (if-not (blank? body)
      (-> (apply hash-map (split body #"[=&]"))
          (transform-values url-decode)))))

(defn percent-encode
  "Percent encode `unencoded` according to RFC 3986, Section 2.1."
  [unencoded]
  (-> (url-encode (str unencoded))
      (replace "%7E" "~")
      (replace "*" "%2A")
      (replace "+" "%20")))

(defn format-params
  "Returns OAuth formatted OAuth params."
  [params]
  (->> (seq params)
       (map #(str (percent-encode (first %1)) "=" (percent-encode (last %1))))
       (sort) (join "&")))

(defn random-bytes
  "Returns a random byte array of the specified size."
  [size]
  (let [seed (byte-array size)]
    (.nextBytes (SecureRandom/getInstance "SHA1PRNG") seed)
    seed))

(defn random-base64
  "Returns a Base64 encoded string from a random byte array of the
  specified size."
  [size] (base64-encode (random-bytes size)))

(defn oauth-keys
  "Returns the OAuth keys in `map`."
  [map] (filter #(re-matches #"^oauth(-|_).*" (name %1)) (keys map)))

(defn oauth-params
  "Returns a map containing only the OAuth entries."
  [map] (transform-keys (select-keys map (oauth-keys map)) (comp name underscore)))

(defn wrap-content-type
  "Returns a HTTP client that sets the Content-Type header to `request`."
  [client content-type]
  (fn [request]
    (->> (or (:content-type request) content-type)
         (assoc request :content-type)
         (client))))

(defn wrap-decode-response
  "Returns an HTTP client that decodes the request body accoring to
  the content-type header."
  [client] (fn [request] (decode-response (client request))))
