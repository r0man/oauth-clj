(ns oauth.test.v1
  (:require [clj-http.client :as http])
  (:use clojure.test
        oauth.test.examples
        oauth.v1))

(def example-options
  {:oauth-consumer-key "xvz1evFS4wEEPTGEFPHBog"
   :oauth-nonce "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"
   :oauth-signature "tnnArxj06cWHq44gCs1OSKk/jLY="
   :oauth-signature-method "HMAC-SHA1"
   :oauth-timestamp "1318622958"
   :oauth-token "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
   :oauth-version "1.0"})

(deftest test-format-base-url
  (are [request expected]
    (is (= expected (format-base-url request)))
    create-signature-request "https://api.twitter.com/1/statuses/update.json"))

(deftest test-format-authorization-header
  (= (str "OAuth" (format-options example-options)) (format-authorization-header example-options)))

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
  (are [request expected]
    (is (= expected (root-url request)))
    create-signature-request "https://api.twitter.com"))

(deftest test-oauth-authorization-header
  (is (= (str "OAuth oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", "
              "oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", "
              "oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1318622958\", "
              "oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", "
              "oauth_version=\"1.0\"")
         (oauth-authorization-header create-signature-request))))

(deftest test-oauth-nonce
  (is (string? (oauth-nonce)))
  (is (not (= (oauth-nonce) (oauth-nonce)))))

(deftest test-oauth-parameter-string
  (is (= (str "include_entities=true&"
              "oauth_consumer_key=xvz1evFS4wEEPTGEFPHBog&"
              "oauth_nonce=kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg&"
              "oauth_signature_method=HMAC-SHA1&"
              "oauth_timestamp=1318622958&"
              "oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb&"
              "oauth_version=1.0&"
              "status=Hello%20Ladies%20%2B%20Gentlemen%2C%20a%20signed%20OAuth%20request%21")
         (oauth-parameter-string create-signature-request))))

(deftest test-oauth-signature-parameters
  (is (= {} (oauth-signature-parameters nil)))
  (is (= {} (oauth-signature-parameters {})))
  (let [params (oauth-signature-parameters create-signature-request)]
    (is (= 8 (count params)))
    (is (= true (get params "include_entities")))
    (is (= "xvz1evFS4wEEPTGEFPHBog" (get params "oauth_consumer_key")))
    (is (= "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg" (get params "oauth_nonce")))
    (is (= "HMAC-SHA1" (get params "oauth_signature_method")))
    (is (= "1318622958" (get params "oauth_timestamp")))
    (is (= "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb" (get params "oauth_token")))
    (is (= "1.0" (get params "oauth_version")))
    (is (= "Hello Ladies + Gentlemen, a signed OAuth request!" (get params "status")))))

(deftest test-oauth-request-signature
  (is (= "tnnArxj06cWHq44gCs1OSKk/jLY="
         (oauth-request-signature create-signature-request oauth-consumer-secret oauth-token-secret)))
  (is (= "8wUi7m5HFQy76nowoCThusfgB+Q="
         (oauth-request-signature twitter-request-token "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98" nil))))

(deftest test-oauth-signature-base-string
  (is (= (str "POST&"
              "https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json&"
              "include_entities%3Dtrue%26"
              "oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26"
              "oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26"
              "oauth_signature_method%3DHMAC-SHA1%26"
              "oauth_timestamp%3D1318622958%26"
              "oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26"
              "oauth_version%3D1.0%26"
              "status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521")
         (oauth-signature-base-string create-signature-request)))
  (is (= (str "POST&"
              "https%3A%2F%2Fapi.twitter.com%2Foauth%2Frequest_token&"
              "oauth_callback%3Dhttp%253A%252F%252Flocalhost%253A3005%252Fthe_dance%252Fprocess_callback%253Fservice_provider_id%253D11%26"
              "oauth_consumer_key%3DGDdmIQH6jhtmLUypg82g%26"
              "oauth_nonce%3DQP70eNmVz8jvdPevU3oJD2AfF7R7odC2XJcn4XlZJqk%26"
              "oauth_signature_method%3DHMAC-SHA1%26"
              "oauth_timestamp%3D1272323042%26"
              "oauth_version%3D1.0")
         (oauth-signature-base-string twitter-request-token))))

(deftest test-oauth-signing-key
  (are [consumer-secret token-secret expected]
    (is (= expected (oauth-signing-key consumer-secret token-secret)))
    "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98" nil "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98&"
    "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98" "" "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98&"
    oauth-consumer-secret oauth-token-secret "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"))

(deftest test-oauth-timestamp
  (is (number? (oauth-timestamp))))

(deftest test-wrap-oauth-sign-request
  ((wrap-oauth-sign-request
    #(is (= "tnnArxj06cWHq44gCs1OSKk/jLY=" (:oauth-signature %1)))
    oauth-consumer-secret oauth-token-secret)
   create-signature-request))

(deftest test-make-consumer
  (let [consumer (make-consumer oauth-consumer-secret oauth-token-secret)]
    (is (fn? consumer))))
