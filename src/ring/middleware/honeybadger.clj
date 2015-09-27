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

(defn- request->metadata
  "Given a Ring request, extract and format the key details as
  honeybadger metadata."
  [request]
  {:request {:method  (:request-method request)
             :url     (req/request-url request)
             :params  (request-params request)
             :session (:session request)}})

(defn wrap-honeybadger
  "Ring middleware to report handler exceptions to
  honeybadger.io. :api-key is the only required option."
  [handler options]
  (fn [request]
    (try
      (handler request)
      (catch Throwable t
        (let [{:keys [callback]} options
              hb-options (select-keys options [:api-key :env])
              hb-id @(hb/notify hb-options t (request->metadata request))]
          (when callback
            (callback t hb-id)))
        (throw t)))))
