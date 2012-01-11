(ns oauth.test.v1
  (:require [clj-http.client :as http])
  (:use clojure.test
        oauth.v1))

(def example-options
  {:oauth-consumer-key "xvz1evFS4wEEPTGEFPHBog"
   :oauth-nonce "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"
   :oauth-signature "tnnArxj06cWHq44gCs1OSKk/jLY="
   :oauth-signature-method "HMAC-SHA1"
   :oauth-timestamp "1318622958"
   :oauth-token "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
   :oauth-version "1.0"})

(def example-request
  {:method :post
   :scheme "https"
   :server-name "api.twitter.com"
   :uri "/1/statuses/update.json"
   :query-params {:include_entities true}
   :body "status=Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21"})

(def example-oauth-request
  (merge example-request example-options))

(deftest test-format-base-url
  (are [request expected]
    (is (= expected (format-base-url request)))
    example-request "https://api.twitter.com/1/statuses/update.json"))

(deftest test-format-header
  (= (str "OAuth" (format-options example-options)) (format-header example-options)))

(deftest test-format-http-method
  (are [request expected]
    (is (= expected (format-http-method request)))
    {:method :get} "GET"
    {:request-method :get} "GET"))

(deftest test-format-options
  (= (str "oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", "
          "oauth_signature_method=\"HMAC-SHA1\", "
          "oauth_timestamp=\"1318622958\", "
          "oauth_signature=\"tnnArxj06cWHq44gCs1OSKk%2FjLY%3D\", "
          "oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", "
          "oauth_version=\"1.0\", "
          "oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\"")
     (format-options example-options)))

(deftest test-percent-encode
  (are [unencoded expected]
    (is (= expected (percent-encode unencoded)))
    "" ""
    "Ladies + Gentlemen" "Ladies%20%2B%20Gentlemen"
    "An encoded string!" "An%20encoded%20string%21"
    "Dogs, Cats & Mice" "Dogs%2C%20Cats%20%26%20Mice"
    "☃" "%E2%98%83")) ; https://dev.twitter.com/docs/auth/percent-encoding-parameters

(deftest test-root-url
  (is (= "https://api.twitter.com" (root-url example-request))))

(deftest test-oauth-parameter-string
  (is (= (str "include_entities=true&"
              "oauth_consumer_key=xvz1evFS4wEEPTGEFPHBog&"
              "oauth_nonce=kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg&"
              "oauth_signature_method=HMAC-SHA1&"
              "oauth_timestamp=1318622958&"
              "oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb&"
              "oauth_version=1.0&"
              "status=Hello%20Ladies%20%2B%20Gentlemen%2C%20a%20signed%20OAuth%20request%21")
         (oauth-parameter-string example-oauth-request))))

(deftest test-oauth-parameters
  (is (map? (oauth-parameters example-oauth-request)))
  (let [params (seq (oauth-parameters example-oauth-request))]
    (is (= 8 (count params)))
    (is (= ["include_entities" "true"] (nth params 0)))
    (is (= ["oauth_consumer_key" "xvz1evFS4wEEPTGEFPHBog"] (nth params 1)))
    (is (= ["oauth_nonce" "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"] (nth params 2)))
    (is (= ["oauth_signature_method" "HMAC-SHA1"] (nth params 3)))
    (is (= ["oauth_timestamp" "1318622958"] (nth params 4)))
    (is (= ["oauth_token" "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"] (nth params 5)))
    (is (= ["oauth_version" "1.0"] (nth params 6)))
    (is (= ["status" "Hello Ladies + Gentlemen, a signed OAuth request!"] (nth params 7)))))

(deftest test-oauth-request-signature
  (let [request (oauth-request-signature example-request)]
    (is request)))

(deftest test-oauth-signature-base-string
  (is (= "POST&https%3A%2F%2Fapi.twitter.com&include_entities%3Dtrue"
         (oauth-signature-base-string example-request))))
