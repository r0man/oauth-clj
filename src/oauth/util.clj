(ns oauth.util
  (:refer-clojure :exclude (replace))
  (:import java.security.SecureRandom
           javax.crypto.Mac
           javax.crypto.spec.SecretKeySpec)
  (:use [clj-http.util :only (base64-encode url-encode url-decode)]
        [clojure.string :only (replace split)]
        [inflections.core :only (hyphenize)]))

(defn compact-map
  "Returns a `map` with all entries removed, where the entrie's value
  matches `pred`."
  [map & [pred]]
  (let [pred (or pred nil?)]
    (reduce
     #(if (pred (%2 map)) %1 (assoc %1 %2 (%2 map)))
     {} (keys map))))

(defn hmac
  ([^String algorithm ^String msg ^String key]
     (hmac algorithm msg key "UTF8"))
  ([^String algorithm ^String msg ^String key ^String encoding]
     (let [key (SecretKeySpec. (.getBytes key "UTF8") algorithm)
           mac (doto (Mac/getInstance algorithm)
                 (.init key))]
       (.doFinal mac (.getBytes msg encoding)))))

(defn parse-response
  "Parse `response` and return a map with hypenized keys and their values."
  [response]
  (reduce
   #(let [[k v] (split %2 #"=")]
      (assoc %1 (hyphenize (keyword k)) v))
   {} (split response #"&")))

(defn percent-encode
  "Percent encode `unencoded` according to RFC 3986, Section 2.1."
  [unencoded]
  (-> (url-encode unencoded)
      (replace "%7E" "~")
      (replace "*" "%2A")
      (replace "+" "%20")))

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

(defn select-oauth-keys
  "Returns the OAuth keys in `map`."
  [map] (filter #(.startsWith (name %1) "oauth-") (keys map)))

(defn select-oauth-map
  "Returns a map containing only the OAuth entries."
  [map] (select-keys map (select-oauth-keys map)))
