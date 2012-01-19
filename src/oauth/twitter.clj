(ns oauth.twitter
  (:use oauth.v1))

(def ^:dynamic *oauth-request-token-url*
  "https://api.twitter.com/oauth/request_token")

(defn request-token
  [consumer-key consumer-secret callback]
  (let [client (make-consumer consumer-key consumer-secret)]
    {:method :post
     :url *oauth-request-token-url*
     :query-params {"oauth_callback" callback}}))