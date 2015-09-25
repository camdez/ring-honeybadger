# camdez/ring-honeybadger

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

## License

Copyright © 2015 Cameron Desautels

Distributed under the MIT License.
