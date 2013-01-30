(ns oauth.test.io
  (:require [cheshire.core :as json])
  (:use clojure.test
        oauth.io))

(deftest test-content-type
  (are [type expected]
       (is (= expected (content-type {:headers {"content-type" type}})))
       nil nil
       "" nil
       "appication/json" :appication/json
       "appication/clojure" :appication/clojure
       "application/json;charset=UTF-8" :application/json))

(deftest test-deserialize
  (is (nil? (deserialize nil)))
  (is (= {} (deserialize {})))
  (let [body {:a 1 :b 2}]
    (let [request (deserialize {:body (prn-str body) :headers {"content-type" "application/clojure"}})]
      (is (= body (:body request))))
    (let [request (deserialize {:body (json/encode body) :headers {"content-type" "application/json"}})]
      (is (= body (:body request))))))

(deftest test-serialize
  (is (nil? (serialize nil)))
  (is (= {} (serialize {})))
  (let [body {:a 1 :b 2}]
    (let [request (serialize {:body body :headers {"content-type" "application/clojure"}})]
      (is (= :application/clojure (content-type request)))
      (is (= (prn-str body) (:body request))))
    (let [request (serialize {:body body :headers {"content-type" "application/json"}})]
      (is (= :application/json (content-type request)))
      (is (= (json/encode body) (:body request))))))
