(ns oauth.util
  (:refer-clojure :exclude (replace))
  (:import javax.crypto.Mac javax.crypto.spec.SecretKeySpec)
  (:use [clj-http.util :only (url-encode url-decode)]
        [clojure.string :only (replace)]))

(defn hmac
  ([^String algorithm ^String msg ^String key]
     (hmac algorithm msg key "UTF8"))
  ([^String algorithm ^String msg ^String key ^String encoding]
     (let [key (SecretKeySpec. (.getBytes key "UTF8") algorithm)
           mac (doto (Mac/getInstance algorithm)
                 (.init key))]
       (.doFinal mac (.getBytes msg encoding)))))

(defn percent-encode
  "Percent encode `unencoded` according to RFC 3986, Section 2.1."
  [unencoded]
  (-> (url-encode unencoded)
      (replace "%7E" "~")
      (replace "*" "%2A")
      (replace "+" "%20")))
