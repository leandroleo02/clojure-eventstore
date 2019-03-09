(defproject eventstore.net/eventstore "0.1.0-SNAPSHOT"
  :description "A simple and fast EventStore that supports multiple persistence and notification providers"
  :url "https://github.com/leandroleo02/clojure-eventstore"
  :license {:name         "MIT License"
            :url          "http://opensource.org/licenses/MIT"
            :distribution :repo}
  :min-lein-version "2.8.1"
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :profiles {:dev {:plugins [[lein-cloverage "1.1.0"]]}})
