# camdez/ring-honeybadger

[![Clojars Project][clojars-badge]][clojars-ring-honeybadger]

Ring middleware for sending errors to honeybadger.io using the
standalone [honeybadger][1] library.

[1]: https://github.com/camdez/honeybadger

## Usage

```clj
(require '[ring.middleware.honeybadger :refer [wrap-honeybadger]])

(def hb-config
  {:api-key "d34db33f"
   :env     "development"})

(def app
  (wrap-honeybadger handler hb-config))
```

If you'd like to run code when exceptions are reported, you can
provide a callback function which will be invoked with the reported
exception and the resultant Honeybadger ID. For example:

```clj
(def hb-config
  {:api-key  "d34db33f"
   :env      "development"
   :callback (fn [_ex id]
               (info (str "Reported exception to Honeybadger with ID" id)))})
```

## License

Copyright © 2015 Cameron Desautels

Distributed under the MIT License.

[clojars-badge]: http://clojars.org/camdez/ring-honeybadger/latest-version.svg
[clojars-ring-honeybadger]: http://clojars.org/camdez/ring-honeybadger
