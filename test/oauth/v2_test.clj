(ns oauth.v2-test
  (:require [clj-http.client :as http]
            [clojure.java.browse :refer [browse-url]]
            [oauth.facebook :refer [*oauth-access-token-url* *oauth-authorization-url*]]
            [oauth.facebook-test :refer [facebook-access-token facebook-access-token facebook-client-id facebook-code facebook-redirect-uri facebook-client-secret]]
            [clojure.test :refer :all]
            [oauth.v2 :refer :all]))

;; (deftest test-oauth-access-token
;;   (let [access-token
;;         (oauth-access-token
;;          *oauth-access-token-url*
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
         (oauth-authorization-url *oauth-authorization-url* facebook-client-id facebook-redirect-uri)))
  (is (= (str "https://www.facebook.com/dialog/oauth?"
              "client_id=287169314674516&"
              "redirect_uri=http%3A%2F%2Flocalhost%2Foauth%2Ffacebook%2Fcallback&"
              "scope=email%2Cread_stream")
         (oauth-authorization-url *oauth-authorization-url* facebook-client-id facebook-redirect-uri :scope "email,read_stream"))))

(deftest test-oauth-authorize
  (with-redefs [browse-url #(is (= (oauth-authorization-url *oauth-authorization-url* facebook-client-id facebook-redirect-uri) %1))]
    (oauth-authorize *oauth-authorization-url* facebook-client-id facebook-redirect-uri)))

(deftest test-oauth-client
  (is (fn? (oauth-client facebook-access-token))))

(deftest test-wrap-client-id
  (let [client-id "CLIENT-ID"]
    ((wrap-client-id
      #(is (= client-id (get (:query-params %1) "client_id")))
      client-id)
     {:request-method :get})
    ((wrap-client-id
      #(is (= client-id (get (:form-params %1) "client_id")))
      client-id)
     {:request-method :put})))

(deftest test-wrap-oauth-access-token
  ((wrap-oauth-access-token
    #(is (= facebook-access-token (get (:query-params %1) "access_token")))
    facebook-access-token)
   {})
  ((wrap-oauth-access-token
    #(is (= "ACCESS-TOKEN" (get (:query-params %1) "access_token")))
    facebook-access-token)
   {:oauth-access-token "ACCESS-TOKEN"}))
