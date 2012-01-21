(ns oauth.test.v2
  (:require [clj-http.client :as http])
  (:use [clojure.java.browse :only (browse-url)]
        clojure.test
        oauth.v2))

(deftest test-make-consumer
  (is (fn? (make-consumer "ACCESS-TOKEN"))))

(deftest test-oauth-authorization-url
  (is (= "https://www.facebook.com/dialog/oauth?client_id=287169314674516&redirect_uri=http%3A%2F%2Fexample.com"
         (oauth-authorization-url "https://www.facebook.com/dialog/oauth" "287169314674516" "http://example.com")))
  (is (= "https://www.facebook.com/dialog/oauth?client_id=287169314674516&redirect_uri=http%3A%2F%2Fexample.com&scope=email%2Cread_stream"
         (oauth-authorization-url "https://www.facebook.com/dialog/oauth" "287169314674516" "http://example.com" :scope "email,read_stream"))))

(deftest test-oauth-authorize
  (let [url "https://www.facebook.com/dialog/oauth"
        client-id "287169314674516"
        redirect-uri "http://example.com"]
    (with-redefs [browse-url (fn [target] (is (= (oauth-authorization-url url client-id redirect-uri) target)))]
      (oauth-authorize url client-id redirect-uri))))
