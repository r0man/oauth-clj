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

;; (deftest test-oauth-access-token
;;   (let [access-token
;;         (oauth-access-token
;;          google-client-id
;;          google-client-secret
;;          google-code
;;          google-redirect-uri)]
;;     (is (string? (:access-token access-token)))
;;     (is (string? (:expires access-token)))))

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
  (println (oauth-authorization-url google-client-id google-redirect-uri))
  (oauth-access-token
   google-client-id
   google-client-secret
   "kACAH-1Ng1V86WD8FyM4A34YtS7GgDXIOUQRPoI1TTZVr36Y0MFkOIpWhpCvf9tiAUwSRcVRWBpaAhekzMXm_xJYJeO-bOixaAXN7Mlfuzom1VV4hGc2c2_upE"
   google-redirect-uri))
