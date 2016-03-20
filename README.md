# OAUTH-CLJ
  [![Build Status](https://travis-ci.org/r0man/oauth-clj.svg)](https://travis-ci.org/r0man/oauth-clj)
  [![Dependencies Status](http://jarkeeper.com/r0man/oauth-clj/status.svg)](http://jarkeeper.com/r0man/oauth-clj)

A clj-http compatible OAuth library for Clojure.

## Installation

Via Clojars: http://clojars.org/oauth-clj.

[![Current Version](https://clojars.org/oauth-clj/latest-version.svg)](https://clojars.org/oauth-clj)

## Usage

``` clj
(use 'oauth.twitter)
```

Define your consumer key and secret.

``` clj
(def consumer-key "qcz2O57srPsb5eZA2Jyw")
(def consumer-secret "lfs5WjmIzPc3OlDNoHSfbxVBmPNmduTDq4rQHhNN7Q")
```

Obtain a OAuth request token from Twitter to request user authorization.

``` clj
(def request-token (oauth-request-token consumer-key consumer-secret))
;;=> {:oauth-callback-confirmed "true",
;;=>  :oauth-token-secret "1TPRuaqWZ9Y9viEdKbU4SQ2QsF5auLcMZaHOwYLK2ao",
;;=>  :oauth-token "C6FCXGYUIutgTZZP1EAAx2nT0cv8QO15K4EbjbzOmBs"}
```

Send the user to Twitter's authorization endpoint.

``` clj
(oauth-authorize (:oauth-token request-token))
```

Parse the parameters in your oauth callback endpoint.

``` clj
(def authorization
  {:oauth-verifier "ZCpKl8mgIUJmTkO8rfBeFotrKKd84igvytvLqlzo"
   :oauth-token "a5wQRcMsl5BMSPTmxZG5ER8OzMH6jdG4kX4uPtbC4Rw"})
```

Obtain the OAuth access token from Twitter.

``` clj
(def access-token
  (oauth-access-token
   consumer-key
   (:oauth-token authorization)
   (:oauth-verifier authorization)))
```

Make a clj-http OAuth client.

``` clj
(def client
  (oauth-client
   consumer-key
   consumer-secret
   (:oauth-token access-token)
   (:oauth-token-secret access-token)))
```

Post a Tweet ...

``` clj
(client
 {:method :post
  :url "http://api.twitter.com/1/statuses/update.json"
  :body (str "status=setting%20up%20my%20twitter%20私のさえずりを設定する")})
```

## License

Copyright (C) 2012-2016 r0man

Distributed under the Eclipse Public License, the same as Clojure.
