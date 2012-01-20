(ns oauth.test.util
  (:use [clojure.string :only (blank?)]
        clojure.test
        oauth.test.examples
        oauth.util))

(deftest test-compact-map
  (is (= {} (compact-map {})))
  (is (= {:a "1"} (compact-map {:a "1"})))
  (is (= {:a "1"} (compact-map {:a "1" :b nil})))
  (is (= {:a "1"} (compact-map {:a "1" :b ""} blank?))))

(deftest test-parse-respone
  (are [response expected]
    (is (= expected (parse-body response)))
    "oauth_token=Z6eEdO8MOmk394WozF5oKyuAv855l4Mlqo7hhlSLik&oauth_callback_confirmed=true"
    {:oauth-callback-confirmed "true" :oauth-token "Z6eEdO8MOmk394WozF5oKyuAv855l4Mlqo7hhlSLik"}))

(deftest test-percent-encode
  (are [unencoded expected]
    (is (= expected (percent-encode unencoded)))
    "" ""
    "Ladies + Gentlemen" "Ladies%20%2B%20Gentlemen"
    "An encoded string!" "An%20encoded%20string%21"
    "Dogs, Cats & Mice" "Dogs%2C%20Cats%20%26%20Mice"
    "☃" "%E2%98%83"))
                                        ;
(deftest test-parse-body-params
  (are [request expected]
    (is (= expected (parse-body-params request)))
    {} nil
    twitter-update-status {"status" "Hello Ladies + Gentlemen, a signed OAuth request!"}))

(deftest test-random-base64
  (is (string? (random-base64 1)))
  (is (not (= (random-base64 1) (random-base64 1)))))

(deftest test-random-bytes
  (is (not (= (seq (random-bytes 1)) (seq (random-bytes 1))))))

(deftest test-oauth-keys
  (are [map expected]
    (is (= expected (oauth-keys map)))
    {} []
    {:oauth-signature-method "HMAC-SHA1" "oauth_version" "1.0" :other-key "x"}
    [:oauth-signature-method "oauth_version"]))

(deftest test-oauth-map
  (are [map expected]
    (is (= expected (oauth-map map)))
    {} {}
    {:oauth-signature-method "HMAC-SHA1" :oauth-version "1.0" :other-key "x"}
    {"oauth_signature_method" "HMAC-SHA1" "oauth_version" "1.0"}
    {:oauth_signature_method "HMAC-SHA1" :oauth-version "1.0" :q 1}
    {"oauth_signature_method" "HMAC-SHA1" "oauth_version" "1.0"}))
