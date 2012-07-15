(ns oauth.test.google
  (:require [oauth.v2 :as v2])
  (:use [clojure.java.browse :only (browse-url)]
        clojure.test
        oauth.google))

(def google-access-token
  "")

(def google-client-id
  "235540178849.apps.googleusercontent.com")

(def google-client-secret
  "WmmnpwWv7NEptBuKymPzPPLF")

(def google-redirect-uri
  "https://localhost/oauth2callback")

(def google-code
  "kACAH-1Ng0uD2HWBWbVKhqSXoXNda8_geLaOhmm8S32pG_-isiyg2E_XMKwthn2f4oqvkl9mDaK-IvVfG85KTb4mlTlEDI_ccJ3JTVn5JKC0rjHjXkXUmN1OyA")

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
  (is (= (str "https://accounts.google.com/o/oauth2/auth?"
              "access_type=offline&"
              "client_id=235540178849.apps.googleusercontent.com&"
              "redirect_uri=https%3A%2F%2Flocalhost%2Foauth2callback&"
              "response_type=code&"
              "scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email")
         (oauth-authorization-url google-client-id google-redirect-uri)))
  (is (= (str "https://accounts.google.com/o/oauth2/auth?"
              "access_type=offline&"
              "client_id=235540178849.apps.googleusercontent.com&"
              "redirect_uri=https%3A%2F%2Flocalhost%2Foauth2callback&"
              "response_type=code&"
              "scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email")
         (oauth-authorization-url google-client-id google-redirect-uri :scope (:email scopes)))))

(deftest test-oauth-authorize
  (with-redefs [browse-url (fn [target] (is (= (oauth-authorization-url google-client-id "http://example.com") target)))]
    (oauth-authorize google-client-id "http://example.com")))

(comment
  (println (oauth-authorization-url google-client-id google-redirect-uri :scope (:email scopes)))
  (def google-access-token nil)
  (alter-var-root
   #'google-access-token
   (constantly
    (oauth-access-token
     google-client-id
     google-client-secret
     "kACAH-1Ng1B56xUzDAuVeEqVNe51EEqaQk7r9OfXrHIkivA9NPkGmohr6UQsDxHUI1ZEewjRx6I3h-yVZlY7qh0vxSoO-oAjPr7cf0nCap8ghcjCjyiQCVUijw"
     google-redirect-uri)))
  (user-info (oauth-client (:access-token google-access-token)))
  )
