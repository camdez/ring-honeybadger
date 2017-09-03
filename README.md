# camdez/ring-honeybadger

[![Clojars Project][clojars-badge]][clojars-ring-honeybadger]

Ring middleware for sending errors to honeybadger.io using the
standalone [honeybadger][hb] library.

## Usage

### Basics

```clj
(require '[ring.middleware.honeybadger :refer [wrap-honeybadger]])

(def hb-config
  {:api-key "d34db33f"
   :env     "development"})

(def app
  (wrap-honeybadger handler hb-config))
```

### Filters

The underlying [honeybadger][hb] library supports a flexible mechanism
called *filters* for controlling exactly what gets sent to
Honeybadger, and under what conditions. There is also a collection of
filters for common operations in the `honeybadger.filter` namespace:

```clj
(require '[honeybadger.filter :as hbf])

(def hb-config
  {:api-key "d34db33f"
   :env     "development"
   :filters [(hbf/only   (hbf/env? :production))              ; only report exceptions in prod
             (hbf/except (hbf/instance? ArithmeticException)) ; never report ArithmeticExceptions
             (hbf/obscure-params [[:config :password]         ; replace these params with
                                  [:secret-id])]})            ;   "[FILTERED]" (if present)
```

For full details, see that library's documentation.

### Context

If you'd like to add contextual metadata to the error report you can
provide a context-generating function in the config parameter under
the `:context-fn` key.  The function will be called with the current
request, and it should return a map of metadata which can be
serialized to JSON.

This map can contain any information which will help you to make sense
of the reported errors, but there are two keys which have special
significance to Honeybadger: `:user-id` and `:user-email`; these keys
are used to show which users are experiencing the reported issue.

```clj
(def hb-config
  {:api-key    "d34db33f"
   :env        "development"
   :context-fn (fn [req]
                 {:user-id (auth/current-user-id req)
                  ,,,}))
```

### Callbacks

If you'd like to run code when errors are reported, you can provide a
callback function which will be invoked with the reported error and
the resultant Honeybadger ID (or `nil` if reporting was suppressed by
a filter). For example:

```clj
(defn hb-callback [_err id]
  (println
   (if id
     (str "Reported error to Honeybadger with ID " id)
     "Error reporting suppressed by filter")))

(def hb-config
  {:api-key  "d34db33f"
   :env      "development"
   :callback hb-callback})
```

## License

Copyright © 2015 Cameron Desautels

Distributed under the MIT License.

[hb]: https://github.com/camdez/honeybadger
[clojars-badge]: http://clojars.org/camdez/ring-honeybadger/latest-version.svg
[clojars-ring-honeybadger]: http://clojars.org/camdez/ring-honeybadger
