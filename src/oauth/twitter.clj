(ns oauth.twitter
  (:use oauth.v1
        oauth.util))

(def ^:dynamic *oauth-request-token-url*
  "https://api.twitter.com/oauth/request_token")

(defn oauth-request-token
  "Obtain a OAuth request token to request user authorization."
  [consumer-key consumer-secret]
  (-> ((make-consumer
        {:oauth-consumer-key consumer-key
         :oauth-consumer-secret consumer-secret})
       {:method :post :url *oauth-request-token-url*})
      :body parse-body))

;; (oauth-request-token "6dQjyEZHxjLCPKUbZqXw" "p16UiXhDd5GVX5pPz0wFrDO7QitffnFp7gWyriO5hg")
