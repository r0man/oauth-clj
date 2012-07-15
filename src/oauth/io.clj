(ns oauth.io
  (:refer-clojure :exclude [replace])
  (:require [clojure.data.json :refer [json-str read-json]]
            [clojure.string :refer [blank? replace]]
            [inflections.core :refer [hyphenize]]
            [oauth.util :refer [parse-body]]))

(defn content-type
  "Returns the value of the Content-Type header of `request`."
  [request]
  (let [content-type (get (:headers request) "content-type")]
    (if-not (blank?  content-type)
      (keyword (replace content-type #";.*" "")))))

(defn deserialize-json [{:keys [body] :as response}]
  (if (string? body)
    (update-in response [:body] read-json)
    response))

(defmulti deserialize
  "Deserialize the body of `response` according to the Content-Type header."
  (fn [response] (content-type response)))

(defmethod deserialize :default
  [response] response)

(defmethod deserialize :application/clojure
  [{:keys [body] :as response}]
  (if (string? body)
    (update-in response [:body] read-string)
    response))

(defmethod deserialize :application/json
  [response] (deserialize-json response))

(defmethod deserialize :application/x-www-form-urlencoded
  [{:keys [body] :as response}]
  (if (string? body)
    (update-in response [:body] parse-body)
    response))

(defmethod deserialize :text/javascript
  [response] (deserialize-json response))

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

(defn wrap-meta-body [handler]
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
