(ns oauth.test.examples)

(def oauth-consumer-secret
  "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw")

(def oauth-token-secret
  "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE")

(def twitter-update-status
  {:method :post
   :scheme "https"
   :server-name "api.twitter.com"
   :uri "/1/statuses/update.json"
   :query-params {:include_entities true}
   :body "status=Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21"
   :oauth-consumer-key "xvz1evFS4wEEPTGEFPHBog"
   :oauth-nonce "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"
   :oauth-signature-method "HMAC-SHA1"
   :oauth-timestamp "1318622958"
   :oauth-token "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
   :oauth-version "1.0"})

(def twitter-request-token
  {:method :post
   :scheme "https"
   :server-name "api.twitter.com"
   :uri "/oauth/request_token"
   :query-params {"oauth_callback" "http://localhost:3005/the_dance/process_callback?service_provider_id=11"}
   :oauth-consumer-key "GDdmIQH6jhtmLUypg82g"
   :oauth-nonce "QP70eNmVz8jvdPevU3oJD2AfF7R7odC2XJcn4XlZJqk"
   :oauth-signature-method "HMAC-SHA1"
   :oauth-timestamp "1272323042"
   :oauth-version "1.0"})