(ns oauth.twitter-test
  (:require [clojure.test :refer :all]
            [oauth.twitter :refer :all]))

(def example-consumer-key
  "qcz2O57srPsb5eZA2Jyw")

(def example-consumer-secret
  "lfs5WjmIzPc3OlDNoHSfbxVBmPNmduTDq4rQHhNN7Q")

(def example-access-token
  "469240209-QWtxKSP6vzBaH4TrhbgX20zPC2NyjArNlv4rxdFP")

(def example-access-token-secret
  "GZQAudgAQPbnazmqryl4RINMVOzH8QGETPzcHFCg")

(def twitter-client
  (oauth-client
   example-consumer-key
   example-consumer-secret
   example-access-token
   example-access-token-secret))

(def twitter-update-status
  {:method :post
   :scheme "https"
   :server-name "api.twitter.com"
   :uri "/1.1/statuses/update.json"
   :query-params {:include_entities true}
   :body "status=Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21"
   :oauth-consumer-key "xvz1evFS4wEEPTGEFPHBog"
   :oauth-nonce "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"
   :oauth-signature-method "HMAC-SHA1"
   :oauth-timestamp "1318622958"
   :oauth-token "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
   :oauth-version "1.0"})

(def twitter-request-token
  {:method :post
   :scheme "https"
   :server-name "api.twitter.com"
   :uri "/oauth/request_token"
   :query-params {"oauth_callback" "http://localhost:3005/the_dance/process_callback?service_provider_id=11"}
   :oauth-consumer-key "GDdmIQH6jhtmLUypg82g"
   :oauth-nonce "QP70eNmVz8jvdPevU3oJD2AfF7R7odC2XJcn4XlZJqk"
   :oauth-signature-method "HMAC-SHA1"
   :oauth-timestamp "1272323042"
   :oauth-version "1.0"})

(deftest test-oauth-authentication-url
  (is (= "https://api.twitter.com/oauth/authenticate?oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
         (oauth-authentication-url "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"))))

(deftest test-oauth-authorization-url
  (is (= "https://api.twitter.com/oauth/authorize?oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
         (oauth-authorization-url "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"))))

(deftest test-oauth-client
  (is (fn? twitter-client)))

(deftest test-oauth-request-token
  (let [request-token (oauth-request-token example-consumer-key example-consumer-secret)]
    (is (map? request-token))
    (is (string? (:oauth-token request-token)))
    (is (string? (:oauth-token-secret request-token)))
    (is (= "true" (:oauth-callback-confirmed request-token)))))

(deftest test-verify-credentials
  (let [user (twitter-client {:method :get :url "https://api.twitter.com/1.1/account/verify_credentials.json"})]
    (is (map? user))
    (is (= 469240209 (:id user)))
    (let [response (meta user)]
      (is (= 200 (:status response))))))

(deftest test-update-status-query-params
  (let [status (format "Test-q %s" (java.util.Date.))
        response (twitter-client
                  {:method :post
                   :url "https://api.twitter.com/1.1/statuses/update.json"
                   :query-params {:status status}})]
    (is (string? (:id-str response)))
    (is (= status (:text response)))))

(deftest test-update-status-form-params
  (let [status (format "Test-f %s" (java.util.Date.))
        response (twitter-client
                  {:method :post
                   :url "https://api.twitter.com/1.1/statuses/update.json"
                   :form-params {:status status}})]
    (is (string? (:id-str response)))
    (is (= status (:text response)))))
