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

(deftest test-root-url
  (is (= "https://api.twitter.com" (root-url example-request))))

(deftest test-oauth-request-signature
  (let [request (oauth-request-signature example-request)]
    (is request)))

(deftest test-oauth-signature-base-string
  (is (= "POST&https%3A%2F%2Fapi.twitter.com&include_entities%3Dtrue"
         (oauth-signature-base-string example-request))))
