(ns ring.middleware.honeybadger
  (:require [honeybadger.core :as hb]
            [ring.util.request :as req]))

(defn- request-params
  "Determine params map to send to Honeybadger. Uses value of :params
  if present, else merges :query-params and :form-params.

  Rationale is: (1) w/o ring.middleware.params, :params may not exist
  at all, and (2) always merging :query-params and :form-params is not
  reliable as (e.g.) JSON params may only be merged into :params (a la
  ring.middleware.json)."
  [request]
  (if (contains? request :params)
    (:params request)
    (merge (:query-params request) (:form-params request))))

(defn- request->metadata [request]
  {:request {:method  (:request-method request)
             :url     (req/request-url request)
             :params  (request-params request)
             :session (:session request)}})

(defn wrap-honeybadger [handler options]
  (fn [request]
    (try
      (handler request)
      (catch Throwable t
        @(hb/notify options t (request->metadata request))
        (throw t)))))
