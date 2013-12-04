(ns oauth.github-test
  (:require [oauth.v2 :as v2])
  (:use [clojure.java.browse :only (browse-url)]
        clojure.test
        oauth.github))

(def github-access-token
  "7734591ab7093f9e5825d62b040b3fe527b57a3b")

(def github-client-id "f0040abaefab461310aa")

(def github-client-secret "1eacbf61ea266092d3e907870f7938654b6da090")

(def github-redirect-uri "http://localhost/oauth/github/callback")

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
