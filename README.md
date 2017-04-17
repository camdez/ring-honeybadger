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

## Bouncing

The middleware-based approach to exception handling has one major
caveat---we want to install the middleware as early as possible in the
middleware chain so that we don't miss any exceptions, but doing so
means we don't get any of the benefits of other middleware in our
exception reports.  Since the call stack is unwound to the `try` form,
the request as seen by the handling code is the request when it
*entered* the exception handler, meaning we may not even have basic
functionality like parameter parsing or session support, let alone
user authentication, which could be valuable in diagnosing issues.

[ring-defaults][] only exacerbates this problem by applying a number
of middleware together which we can only precede or follow (but not
insert into).

To help with this problem, this library provides a second middleware,
`wrap-honeybadger-bouncer`; this middleware can be installed later in
the middleware chain to capture the more fully-processed request.  It
will then rethrow the exception to the `wrap-honeybadger` middleware
to be reported with this processed request.  You can apply this
middleware as often as you like without ill effect, but as a general
rule you'll want to install it last in the middleware chain so it
directly wraps your application code (the most likely source of
exceptions).

## License

Copyright © 2015 Cameron Desautels

Distributed under the MIT License.

[hb]: https://github.com/camdez/honeybadger
[clojars-badge]: http://clojars.org/camdez/ring-honeybadger/latest-version.svg
[clojars-ring-honeybadger]: http://clojars.org/camdez/ring-honeybadger
[ring-defaults]: https://github.com/ring-clojure/ring-defaults
