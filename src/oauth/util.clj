(ns oauth.util
  (:refer-clojure :exclude (replace))
  (:import java.security.SecureRandom
           javax.crypto.Mac
           javax.crypto.spec.SecretKeySpec)
  (:use [clj-http.util :only (base64-encode url-encode url-decode)]
        [clojure.string :only (join replace split)]
        [inflections.core :only (hyphenize underscore)]
        [inflections.transform :only (transform-keys transform-values)]))

(defn compact-map
  "Returns a `map` with all entries removed, where the entrie's value
  matches `pred`."
  [map & [pred]]
  (let [pred (or pred nil?)]
    (reduce
     #(if (pred (get map %2)) %1 (assoc %1 %2 (get map %2)))
     {} (keys map))))

(defn hmac
  ([^String algorithm ^String msg ^String key]
     (hmac algorithm msg key "UTF8"))
  ([^String algorithm ^String msg ^String key ^String encoding]
     (let [key (SecretKeySpec. (.getBytes key "UTF8") algorithm)
           mac (doto (Mac/getInstance algorithm)
                 (.init key))]
       (.doFinal mac (.getBytes msg encoding)))))

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
  (if (string? (:body request))
    (-> (apply hash-map (split (:body request) #"="))
        (transform-values url-decode))))

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

(defn oauth-map
  "Returns a map containing only the OAuth entries."
  [map] (transform-keys (select-keys map (oauth-keys map)) (comp name underscore)))
