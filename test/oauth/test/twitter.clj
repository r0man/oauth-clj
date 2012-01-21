(ns oauth.test.twitter
  (:use clojure.test
        oauth.test.examples
        oauth.twitter))

(def twitter-client
  (oauth-client
   example-consumer-key
   example-consumer-secret
   example-access-token
   example-access-token-secret))

(deftest test-oauth-authentication-url
  (is (= "https://api.twitter.com/oauth/authenticate?oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
         (oauth-authentication-url "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"))))

(deftest test-oauth-client
  (is (fn? twitter-client)))

(deftest test-oauth-request-token
  (let [request-token (oauth-request-token example-consumer-key example-consumer-secret)]
    (is (map? request-token))
    (is (string? (:oauth-token request-token)))
    (is (string? (:oauth-token-secret request-token)))
    (is (= "true" (:oauth-callback-confirmed request-token)))))

(deftest test-verify-credentials
  (let [response (twitter-client {:method :get :url "https://api.twitter.com/1/account/verify_credentials.json"})]
    (is (= 200 (:status response)))))
