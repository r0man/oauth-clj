(ns oauth.v2
  (:require [clj-http.client :as http])
  (:use [clojure.java.browse :only (browse-url)]
        oauth.util))

(defn- update-access-token [request access-token]
  (assoc-in request [:query-params "access_token"] access-token))

(defn oauth-access-token
  "Obtain the OAuth v2 access token."
  [url client-id client-secret code redirect-uri]
  (-> {:method :get
       :url url
       :query-params
       {"client_id" client-id
        "client_secret" client-secret
        "code" (percent-encode code)
        "redirect_uri" redirect-uri}}
      http/request :body parse-body))

(defn oauth-authorization-url
  "Returns the OAuth v2 authorization url."
  [url client-id redirect-uri & {:as options}]
  (->> (assoc options :client-id client-id :redirect-uri redirect-uri)
       (format-query-params)
       (str url "?")))

(defn oauth-authorize
  "Send the user to the authorization url via `browse-url`."
  [url client-id redirect-uri & options]
  (browse-url (apply oauth-authorization-url url client-id redirect-uri options)))

(defn wrap-oauth-access-token
  "Returns a HTTP client that adds the OAuth v2 `access-token` to `request`."
  [client & [access-token]]
  (fn [{:keys [oauth-access-token] :as request}]
    (client (update-access-token request (or oauth-access-token access-token)))))

(defn make-consumer
  "Returns an OAuth v2 consumer HTTP client."
  [access-token]
  (-> clj-http.core/request
      (http/wrap-request)
      (wrap-oauth-access-token access-token)))

;; (oauth-authorize
;;  "https://www.facebook.com/dialog/oauth"
;;  "287169314674516"
;;  "http://localhost/oauth/facebook/callback"
;;  :scope "offline_access")

;; (def access-token
;;   (oauth-access-token
;;    "https://graph.facebook.com/oauth/access_token"
;;    "287169314674516"
;;    "a91bac323651f4f1633d0a6913529878"
;;    "AQDkFVB9TRIhhcZPfFkQ-cil4LEQrfjxotOBDA8Vd8aNUVDcLp9-D9aT-HJFIT3CbDi1BpXkynurKEynYB-ERz_QImN8vhAkR1Sz5kfLy9vfbMi_-eaJA_SfCBDw4cuq-GkmYg6GjyyAtJDhplVSTd6u-Fy7WlNv8i95qODKfo5C5XMXOIkhcu0yAJ4KtRZfCVI#_=_"
;;    "http://localhost/oauth/facebook/callback"))

;; (prn access-token)

;; (prn access-token)
;; (println
;;  ((make-consumer (:access-token access-token))
;;   {:method :get :url "https://graph.facebook.com/me"}))
