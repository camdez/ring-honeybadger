(ns ring.middleware.honeybadger-test
  (:require [ring.middleware.honeybadger :as sut]
            [clojure.test :refer :all]))

(def req {:scheme :http, :url "/", :request-method :get})

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
        (is (= orig-ex ex))))))
