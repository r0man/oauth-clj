(ns oauth.io
  (:refer-clojure :exclude [replace])
  (:require [clj-http.client :refer [wrap-request]]
            [clj-http.core :as core]
            [cheshire.core :as json]
            [clojure.string :refer [blank? replace]]
            [inflections.core :refer [hyphenize-keys]]
            [oauth.util :refer [parse-body]]))

(defn content-type
  "Returns the value of the Content-Type header of `request`."
  [request]
  (let [content-type (get (:headers request) "content-type")]
    (if-not (blank?  content-type)
      (keyword (replace content-type #";.*" "")))))

(defn deserialize-body
  "Update the :body of `response` by applying `update-fn` to it, if
  it's a string."
  [{:keys [body] :as response} update-fn]
  (if (string? body)
    (update-in response [:body] update-fn)
    response))

(defn serialize-body [request content-type update-fn]
  (if (:body request)
    (-> (update-in request [:body] update-fn)
        (assoc-in [:headers "content-type"] content-type))
    request))

(defmulti deserialize
  "Deserialize the body of `response` according to the Content-Type header."
  (fn [response] (content-type response)))

(defmethod deserialize :default
  [response] response)

(defmethod deserialize :application/clojure
  [response]
  (binding [*read-eval* false]
    (deserialize-body response read-string)))

(defmethod deserialize :application/json
  [response] (deserialize-body response #(json/decode %1 true)))

(defmethod deserialize :application/x-www-form-urlencoded
  [response] (deserialize-body response parse-body))

(defmethod deserialize :text/html
  [response] (deserialize-body response parse-body))

(defmethod deserialize :text/javascript
  [response] (deserialize-body response #(json/decode %1 true)))

(defmethod deserialize :text/plain
  [response] (deserialize-body response parse-body))

(defmulti serialize
  "Serialize the body of `response` according to the Content-Type header."
  (fn [request] (content-type request)))

(defmethod serialize :default
  [request] request)

(defmethod serialize :application/clojure
  [request] (serialize-body request "application/clojure" prn-str))

(defmethod serialize :application/json
  [request] (serialize-body request "application/json" json/encode))

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
    (if (= :stream (:as request))
      (handler request)
      (-> (handler request)
          (deserialize)))))

(defn wrap-output-hyphenize
  "Returns a HTTP client that recursively replaces all underscores in
  the keys of the response map to dashes."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (:skip-hyphenize request)
        response (hyphenize-keys response)))))

(def request
  (-> #'core/request
      (wrap-request)
      (wrap-input-coercion)
      (wrap-output-coercion)
      (wrap-output-hyphenize)
      (wrap-meta-response)))
