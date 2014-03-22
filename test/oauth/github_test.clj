(ns oauth.github-test
  (:require [clojure.java.browse :refer [browse-url]]
            [clojure.test :refer :all]
            [oauth.github :refer :all]
            [oauth.v2 :as v2]))

(def github-access-token
  "eba083597f7a25945cf3546a4591b8829f56f536")

(def github-client-id "8e89fcdfe73e5325e16f")

(def github-client-secret "ad1de66992d850f4edd1bb0c36174c8dd1acf1a2")

(def github-redirect-uri "http://localhost/oauth/github/callback")


(deftest test-oauth-authorization-url
  (is (= (str "https://github.com/login/oauth/authorize?"
              "client_id=" github-client-id
              "&redirect_uri=http%3A%2F%2Flocalhost%2Foauth%2Fgithub%2Fcallback")
         (oauth-authorization-url github-client-id github-redirect-uri)))
  (is (= (str "https://github.com/login/oauth/authorize?"
              "client_id=" github-client-id
              "&redirect_uri=http%3A%2F%2Flocalhost%2Foauth%2Fgithub%2Fcallback&"
              "scope=user")
         (oauth-authorization-url github-client-id github-redirect-uri :scope "user"))))

(deftest test-oauth-authorize
  (let [client-id github-client-id
        redirect-uri "http://example.com"]
    (with-redefs [browse-url (fn [target] (is (= (oauth-authorization-url client-id redirect-uri) target)))]
      (oauth-authorize client-id redirect-uri))))

(deftest test-oauth-client
  (is (fn? (oauth-client github-access-token))))
