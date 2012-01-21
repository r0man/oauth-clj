(ns oauth.test.v2
  (:require [clj-http.client :as http])
  (:use [clojure.java.browse :only (browse-url)]
        clojure.test
        oauth.test.examples
        oauth.v2))

(deftest test-oauth-authorization-url
  (is (= "https://www.facebook.com/dialog/oauth?client_id=287169314674516&redirect_uri=http%3A%2F%2Fexample.com"
         (oauth-authorization-url "https://www.facebook.com/dialog/oauth" "287169314674516" "http://example.com")))
  (is (= "https://www.facebook.com/dialog/oauth?redirect_uri=http%3A%2F%2Fexample.com&client_id=287169314674516&scope=email%2Cread_stream"
         (oauth-authorization-url "https://www.facebook.com/dialog/oauth" "287169314674516" "http://example.com" :scope "email,read_stream"))))

(deftest test-oauth-authorize
  (with-redefs [browse-url (fn [url] (is (= "https://www.facebook.com/dialog/oauth?client_id=287169314674516&redirect_uri=http%3A%2F%2Fexample.com" url)))]
    (oauth-authorize "https://www.facebook.com/dialog/oauth" "287169314674516" "http://example.com")))
