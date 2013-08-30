(ns oauth.facebook-test
  (:require [oauth.v2 :as v2])
  (:use [clojure.java.browse :only (browse-url)]
        clojure.test
        oauth.facebook))

(def facebook-access-token
  "AAAEFLdD911QBABjIcoGUnQCXdDLOEZAuuNxdarxLGNcZCucZC070FzHHe1D9OYILBYvHFSKfJwfq8ymD0j0MZACmnhOTbDEURzZBXBovM4gZDZD")

(def facebook-client-id "287169314674516")

(def facebook-client-secret "1dd8b4e0492f3e6113f1014e38d317f9")

(def facebook-redirect-uri "http://localhost/oauth/facebook/callback")

(def facebook-code
  (str "AQDHV_kgQvlQHMFt5uyvCpVBB9QVTLaWzReBukLF7E6sBXEFofph07uQKKAmL512Sc0xEp-5yo9BLpk"
       "8yxWvrgQJ99ycTP7We3FnM-uiQCUQJukn1ux4-4zgGtUykeaU6AxArsBsWm2pPoTL2hKV9GAAf7kVh4"
       "1EGtcXLHSKLfrPEKY6xPm7C6BPOi8daqeEfxVAvn_MiVI0bZIjAP4r1H8Z#_=_"))

;; (deftest test-me-endpoint
;;   (let [user ((oauth-client facebook-access-token) {:method :get :url "https://graph.facebook.com/me"})]
;;     (is (map? user))
;;     (is (= "100001026171775" (:id user)))
;;     (let [response (meta user)]
;;       (is (= 200 (:status response)))
;;       (is (not (contains? (set (keys response)) :body))))))

;; (deftest test-oauth-access-token
;;   (let [access-token
;;         (oauth-access-token
;;          facebook-client-id
;;          facebook-client-secret
;;          facebook-code
;;          facebook-redirect-uri)]
;;     (is (string? (:access-token access-token)))
;;     (is (string? (:expires access-token)))))

(deftest test-oauth-authorization-url
  (is (= (str "https://www.facebook.com/dialog/oauth?"
              "client_id=287169314674516&"
              "redirect_uri=http%3A%2F%2Flocalhost%2Foauth%2Ffacebook%2Fcallback")
         (oauth-authorization-url facebook-client-id facebook-redirect-uri)))
  (is (= (str "https://www.facebook.com/dialog/oauth?"
              "client_id=287169314674516&"
              "redirect_uri=http%3A%2F%2Flocalhost%2Foauth%2Ffacebook%2Fcallback&"
              "scope=email%2Cread_stream")
         (oauth-authorization-url facebook-client-id facebook-redirect-uri :scope "email,read_stream"))))

(deftest test-oauth-authorize
  (let [client-id facebook-client-id
        redirect-uri "http://example.com"]
    (with-redefs [browse-url (fn [target] (is (= (oauth-authorization-url client-id redirect-uri) target)))]
      (oauth-authorize client-id redirect-uri))))

(deftest test-oauth-client
  (is (fn? (oauth-client facebook-access-token))))
