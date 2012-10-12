(ns oauth.test.google
  (:require [oauth.v2 :as v2])
  (:use [clojure.java.browse :only (browse-url)]
        [clj-http.client :only [parse-url]]
        clojure.test
        oauth.google))

(def google-access-token
  "")

(def google-client-id
  "173176451919.apps.googleusercontent.com")

(def google-client-secret
  "rd6_3jrt7dXgF8Ww_eJdxdrp")

(def google-redirect-uri
  "https://localhost/oauth2callback")

(def google-code
  "4/ZgIhztcK_dX3ULsXXBzlGl-f-RX0.MlILSu3vraUdOl05ti8ZT3a5grtycgI")

(deftest test-user-info
  (user-info
   (fn [request]
     (is (= "https://www.googleapis.com/oauth2/v1/userinfo" (:url request)))
     (is (= "https://www.googleapis.com/oauth2/v1/userinfo" (:url request))))))

(deftest test-oauth-access-token
  (with-redefs
    [v2/oauth-access-token
     (fn [url client-id client-secret code redirect-uri]
       (is (= *oauth-access-token-url* url))
       (is (= google-client-id client-id))
       (is (= google-client-secret client-secret))
       (is (= google-code code))
       (is (= google-redirect-uri redirect-uri)))]
    (oauth-access-token
     google-client-id
     google-client-secret
     google-code
     google-redirect-uri)))

(deftest test-oauth-authorization-url
  (let [url (parse-url (oauth-authorization-url google-client-id google-redirect-uri))]
    (is (= :https (:scheme url)))
    (is (= "accounts.google.com" (:server-name url)))
    (is (= "/o/oauth2/auth" (:uri url)))
    (is (= (str "access_type=offline&client_id=173176451919.apps.googleusercontent.com&"
                "redirect_uri=https%3A%2F%2Flocalhost%2Foauth2callback&response_type=code&"
                "scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email")
           (:query-string url))))
  (let [url (parse-url (oauth-authorization-url google-client-id google-redirect-uri :scope (scopes :email)))]
    (is (= :https (:scheme url)))
    (is (= "accounts.google.com" (:server-name url)))
    (is (= "/o/oauth2/auth" (:uri url)))
    (is (= (str "access_type=offline&client_id=173176451919.apps.googleusercontent.com&"
                "redirect_uri=https%3A%2F%2Flocalhost%2Foauth2callback&response_type=code&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email")
           (:query-string url)))))

(deftest test-oauth-authorize
  (with-redefs [browse-url (fn [target] (is (= (oauth-authorization-url google-client-id "http://example.com") target)))]
    (oauth-authorize google-client-id "http://example.com")))

(deftest test-scopes
  (is (= "https://www.googleapis.com/auth/userinfo.email" (scopes :email)))
  (is (= (str "https://www.googleapis.com/auth/userinfo.email "
              "https://www.googleapis.com/auth/userinfo.profile")
         (scopes :email :profile))))

(comment
  (println (oauth-authorization-url google-client-id google-redirect-uri :scope (scopes :email)))
  (def google-access-token nil)
  (alter-var-root
   #'google-access-token
   (constantly
    (oauth-access-token
     google-client-id
     google-client-secret
     google-code
     google-redirect-uri)))
  (user-info (oauth-client (:access-token google-access-token))))
