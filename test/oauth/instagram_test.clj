(ns oauth.instagram-test
  (:require [clj-http.client :refer [parse-url]]
            [oauth.v2 :as v2])
  (:use [clojure.java.browse :only (browse-url)]
        clojure.test
        oauth.instagram))

(def client-id "4dd9fe7ba08a424f838e96bed2db5af3")
(def client-secret "1dd8b4e0492f3e6113f1014e38d317f9")
(def redirect-uri "http://localhost/oauth/instagram")
(def access-token "789c7ec8ff434138afef9a65761c6100")

(deftest test-oauth-authorization-url
  (let [url (parse-url (oauth-authorization-url client-id redirect-uri))]
    (is (= :https (:scheme url)))
    (is (= "api.instagram.com" (:server-name url)))
    (is (nil? (:server-port url)))
    (is (= "/oauth/authorize" (:uri url)))
    (is (= "client_id=4dd9fe7ba08a424f838e96bed2db5af3&redirect_uri=http%3A%2F%2Flocalhost%2Foauth%2Finstagram&response_type=code"
           (:query-string url)))))

(deftest test-oauth-authorize
  (with-redefs [browse-url (fn [target] (is (= (oauth-authorization-url client-id redirect-uri) target)))]
    (oauth-authorize client-id redirect-uri)))

(deftest test-oauth-client
  (is (fn? (oauth-client client-id))))

(deftest test-media
  (let [client (oauth-client client-id)
        result (client {:method :get :url "https://api.instagram.com/v1/media/search"
                        :query-params {:lat -8.80027 :lng 115.160618}})]
    (is seq? result)))
