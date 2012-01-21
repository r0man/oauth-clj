(ns oauth.test.twitter
  (:use clojure.test
        oauth.test.examples
        oauth.twitter))

(deftest test-oauth-authentication-url
  (is (= "https://api.twitter.com/oauth/authenticate?oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
         (oauth-authentication-url "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"))))
