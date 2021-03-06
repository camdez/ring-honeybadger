(defproject camdez/ring-honeybadger "0.3.2-SNAPSHOT"
  :description "Ring middleware for reporting errors to honeybadger.io"
  :url "https://github.com/camdez/ring-honeybadger"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :dependencies [[camdez/honeybadger "0.4.1"]
                 [org.clojure/clojure "1.9.0"]
                 [ring/ring-core "1.6.3"]]
  :deploy-repositories [["releases" :clojars]])
