(ns oauth.util-test
  (:import org.apache.http.entity.StringEntity)
  (:require [clojure.string :refer [blank?]]
            [clojure.test :refer :all]
            [oauth.twitter-test :refer :all]
            [oauth.util :refer :all]))

(deftest test-byte-array?
  (is (not (byte-array? nil)))
  (is (not (byte-array? "")))
  (is (byte-array? (.getBytes "123"))))

(deftest test-compact-map
  (is (= {} (compact-map {})))
  (is (= {:a "1"} (compact-map {:a "1"})))
  (is (= {:a "1"} (compact-map {:a "1" :b nil})))
  (is (= {:a "1"} (compact-map {:a "1" :b ""} blank?))))

(deftest test-content-type
  (are [type expected]
    (is (= expected (content-type {:headers {"content-type" type}})))
    "text/javascript" "text/javascript"
    "text/javascript; charset=UTF-8" "text/javascript"))

(deftest test-format-base-url
  (are [request expected]
    (is (= expected (format-base-url request)))
    twitter-update-status "https://api.twitter.com/1.1/statuses/update.json"))

(deftest test-format-authorization
  (is (string? (format-authorization twitter-update-status))))

(deftest test-format-query-params
  (are [params expected]
    (is (= expected (format-query-params params)))
    nil nil
    {} nil
    {:client-id "287169314674516"} "client_id=287169314674516"
    {"client_id" "287169314674516"} "client_id=287169314674516"
    {"client-id" "287169314674516"} "client-id=287169314674516"))

(deftest test-format-http-method
  (are [request expected]
    (is (= expected (format-http-method request)))
    {:method :get} "GET"
    {:request-method :get} "GET"))

(deftest test-format-options
  (is (= ["body=\"status%3DHello%2520Ladies%2520%252b%2520Gentlemen%252c%2520a%2520signed%2520OAuth%2520request%2521\""
          "method=\"%3Apost\""
          "oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\""
          "oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\""
          "oauth_signature_method=\"HMAC-SHA1\""
          "oauth_timestamp=\"1318622958\""
          "oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\""
          "oauth_version=\"1.0\""
          "query_params=\"%7B%3Ainclude_entities+true%7D\""
          "scheme=\"https\""
          "server_name=\"api.twitter.com\""
          "uri=\"%2F1.1%2Fstatuses%2Fupdate.json\""]
         (format-options twitter-update-status))))

(deftest test-root-url
  (are [request expected]
    (is (= expected (root-url request)))
    twitter-update-status "https://api.twitter.com"))

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
  (testing "body must be key/value pairs"
    (is (thrown? Exception (parse-body-params {:body "x"}))))
  (are [request expected]
    (is (= expected (parse-body-params request)))
    {} nil
    {:body ""} nil
    twitter-update-status
    {"status" "Hello Ladies + Gentlemen, a signed OAuth request!"}
    (assoc twitter-update-status :body (-> twitter-update-status
                                           ^String (:body)
                                           .getBytes))
    {"status" "Hello Ladies + Gentlemen, a signed OAuth request!"}
    (assoc twitter-update-status :body "x=foo&y=bar")
    {"x" "foo" "y" "bar"}
    {:body (StringEntity. (:body twitter-update-status))}
    {"status" "Hello Ladies + Gentlemen, a signed OAuth request!"}))

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

(deftest test-oauth-params
  (are [map expected]
    (is (= expected (oauth-params map)))
    {} {}
    {:oauth-signature-method "HMAC-SHA1" :oauth-version "1.0" :other-key "x"}
    {"oauth_signature_method" "HMAC-SHA1" "oauth_version" "1.0"}
    {:oauth_signature_method "HMAC-SHA1" :oauth-version "1.0" :q 1}
    {"oauth_signature_method" "HMAC-SHA1" "oauth_version" "1.0"}))

(deftest test-wrap-content-type
  (is (= {:content-type x-www-form-urlencoded}
         ((wrap-content-type identity x-www-form-urlencoded) {})))
  (is (= {:content-type "text/plain"}
         ((wrap-content-type identity x-www-form-urlencoded) {:content-type "text/plain"}))))
