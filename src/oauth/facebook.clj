(ns oauth.facebook
  (:require [oauth.v2 :as v2]))

(def ^:dynamic *oauth-authorization-url*
  "https://www.facebook.com/dialog/oauth")

(defn oauth-authorization-url [client-id redirect-uri & options]
  (v2/oauth-authorization-url *oauth-authorization-url* client-id redirect-uri options))
