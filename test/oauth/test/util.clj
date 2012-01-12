(ns oauth.test.util
  (:use [clojure.string :only (blank?)]
        clojure.test
        oauth.util))

(deftest test-compact-map
  (is (= {} (compact-map {})))
  (is (= {:a "1"} (compact-map {:a "1"})))
  (is (= {:a "1"} (compact-map {:a "1" :b nil})))
  (is (= {:a "1"} (compact-map {:a "1" :b ""} blank?))))

(deftest test-percent-encode
  (are [unencoded expected]
    (is (= expected (percent-encode unencoded)))
    "" ""
    "Ladies + Gentlemen" "Ladies%20%2B%20Gentlemen"
    "An encoded string!" "An%20encoded%20string%21"
    "Dogs, Cats & Mice" "Dogs%2C%20Cats%20%26%20Mice"
    "☃" "%E2%98%83")) ; https://dev.twitter.com/docs/auth/percent-encoding-parameters

(deftest test-random-base64
  (is (string? (random-base64 1)))
  (is (not (= (random-base64 1) (random-base64 1)))))

(deftest test-random-bytes
  (is (not (= (seq (random-bytes 1)) (seq (random-bytes 1))))))

(deftest test-select-oauth-keys
  (are [map expected]
    (is (= expected (select-oauth-keys map)))
    {} []
    {:oauth-signature-method "HMAC-SHA1" :oauth-version "1.0" :other-key "x"}
    [:oauth-signature-method :oauth-version]))

(deftest test-select-oauth-map
  (are [map expected]
    (is (= expected (select-oauth-map map)))
    {} {}
    {:oauth-signature-method "HMAC-SHA1" :oauth-version "1.0" :other-key "x"}
    {:oauth-signature-method "HMAC-SHA1" :oauth-version "1.0"}))
