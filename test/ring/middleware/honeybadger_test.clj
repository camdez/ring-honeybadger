(ns ring.middleware.honeybadger-test
  (:require [ring.middleware.honeybadger :as sut]
            [clojure.test :refer :all]))

(def req {:scheme :http, :url "/", :request-method :get})

(deftest wrap-honeybadger-bouncer
  (testing "re-throws exceptions"
    (let [orig-ex (Exception. "Test exception")
          handler (-> (fn [_] (throw orig-ex))
                      sut/wrap-honeybadger-bouncer)
          ex      (try
                    (handler req)
                    (catch Throwable t
                      t))]
      (testing "as wrapped errors"
        (is (#'sut/bounced-error? ex)))
      (testing "with request attached"
        (is (= req (::sut/bounced-request (ex-data ex)))))
      (testing "with original error attached"
        (is (= orig-ex (.getCause ex))))))

  (testing "when applied repeatedly only wraps once"
    (let [orig-ex (Exception. "Test exception")
          handler (-> (fn [_] (throw orig-ex))
                      sut/wrap-honeybadger-bouncer
                      sut/wrap-honeybadger-bouncer)
          ex      (try
                    (handler req)
                    (catch Throwable t
                      t))]
      (is (= orig-ex (.getCause ex))))))

(deftest wrap-honeybadger
  (let [notify-args (atom nil)
        orig-ex     (Exception. "Test exception")
        handler     (-> (fn [_] (throw orig-ex))
                        (sut/wrap-honeybadger {}))]
    (testing "re-throws exceptions"
      (with-redefs [sut/notify (fn [& rest] (reset! notify-args rest))]
        (is (thrown? Exception (handler req)))))
    (testing "reports exceptions to Honeybadger"
      (let [[_ ex _] @notify-args]
        (is (= orig-ex ex)))))
  (testing "reports bounced errors with attached request instead of local request"
    (let [notify-args (atom nil)
          orig-ex     (Exception. "Test exception")
          req'        (assoc-in req [:params :foo] :bar)
          handler     (-> (fn [_] (throw orig-ex))
                          (sut/wrap-honeybadger-bouncer)
                          ((fn [handler]
                             (fn [_req]
                               (handler req'))))
                          (sut/wrap-honeybadger {}))]
      (with-redefs [sut/notify (fn [& rest] (reset! notify-args rest))]
        (is (thrown? Exception (handler req))))
      (let [[_ _ metadata] @notify-args]
        (is (= metadata (#'sut/request->metadata req')))))))
