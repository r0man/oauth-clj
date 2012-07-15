(ns oauth.io
  (:refer-clojure :exclude [replace])
  (:require [clj-http.client :refer [wrap-request]]
            [clj-http.core :as core]
            [clojure.data.json :refer [json-str read-json]]
            [clojure.string :refer [blank? replace]]
            [inflections.core :refer [hyphenize]]
            [oauth.util :refer [parse-body]]))

(defn content-type
  "Returns the value of the Content-Type header of `request`."
  [request]
  (let [content-type (get (:headers request) "content-type")]
    ;; (println content-type)
    (if-not (blank?  content-type)
      (keyword (replace content-type #";.*" "")))))

(defn update-body
  "Update the :body of `response` by applying `update-fn` to it, if
  it's a string."
  [{:keys [body] :as response} update-fn]
  (if (string? body)
    (update-in response [:body] update-fn)
    response))

(defmulti deserialize
  "Deserialize the body of `response` according to the Content-Type header."
  (fn [response] (content-type response)))

(defmethod deserialize :default
  [response] response)

(defmethod deserialize :application/clojure
  [response] (update-body response read-string))

(defmethod deserialize :application/json
  [response] (update-body response read-json))

(defmethod deserialize :text/html
  [response] (update-body response parse-body))

(defmethod deserialize :text/plain
  [response] (update-body response parse-body))

(defmethod deserialize :application/x-www-form-urlencoded
  [response] (update-body response parse-body))

(defmethod deserialize :text/javascript
  [response] (update-body response read-json))

(defmulti serialize
  "Serialize the body of `response` according to the Content-Type header."
  (fn [request] (content-type request)))

(defmethod serialize :default
  [request] request)

(defmethod serialize :application/clojure
  [{:keys [body] :as request}]
  (if body
    (-> (update-in request [:body] prn-str)
        (assoc-in [:headers "content-type"] "application/clojure"))
    request))

(defmethod serialize :application/json
  [{:keys [body] :as request}]
  (if body
    (-> (update-in request [:body] json-str)
        (assoc-in [:headers "content-type"] "application/json"))
    request))

(defn wrap-meta-response [handler]
  (fn [request]
    (let [{:keys [body] :as response} (handler request)]
      (if (instance? clojure.lang.IMeta body)
        (with-meta body (dissoc response :body))
        body))))

(defn wrap-input-coercion [handler]
  (fn [request]
    (handler (serialize request))))

(defn wrap-output-coercion [handler]
  (fn [request]
    (-> (handler request)
        (deserialize)
        (update-in [:body] hyphenize))))

(def request
  (-> #'core/request
      (wrap-request)
      (wrap-output-coercion)
      (wrap-input-coercion)
      (wrap-meta-response)))
