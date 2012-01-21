(ns oauth.test.twitter
  (:use clojure.test
        oauth.test.examples
        oauth.twitter))

(deftest test-oauth-authentication-url
  (is (= "https://api.twitter.com/oauth/authenticate?oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
         (oauth-authentication-url "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"))))

(deftest test-oauth-client
  (let [client
        (oauth-client
         example-consumer-key
         example-consumer-secret
         example-access-token
         example-access-token-secret)]
    (is (fn? client))))

(deftest test-oauth-request-token
  (let [request-token (oauth-request-token example-consumer-key example-consumer-secret)]
    (is (map? request-token))
    (is (string? (:oauth-token request-token)))
    (is (string? (:oauth-token-secret request-token)))
    (is (= "true" (:oauth-callback-confirmed request-token)))))
