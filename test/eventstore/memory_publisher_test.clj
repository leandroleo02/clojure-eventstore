(ns eventstore.memory-publisher-test
  (:require [clojure.test :refer :all]
            [eventstore.domain :as d]
            [eventstore.core :refer [Publisher] :as core]
            [eventstore.in-memory-publisher :refer :all :as publisher]))

(def event (d/->Event "json 1" (System/currentTimeMillis) 1))

(def aggregation-orders "orders")
(def stream-orders (d/->Stream aggregation-orders 123456))
(def message-orders (d/->Message stream-orders event))

(def aggregation-customer "customer")
(def stream-customer (d/->Stream aggregation-customer 654321))
(def message-customer (d/->Message stream-customer event))

(deftest publish-and-subscribe-messages
  (testing "One subscriber by aggregation"
    (let [in-memory (publisher/in-memory-publisher)]
      (core/add-subscriber in-memory aggregation-orders
                      (fn [m]
                        (is (= "orders" (:aggregation (:stream m))))))
      (core/publish in-memory message-orders)))

  (testing "Two subscribers by aggregation"
    (let [in-memory (publisher/in-memory-publisher)]
      (core/add-subscriber in-memory aggregation-orders
                      (fn [m]
                        (is (= "orders" (:aggregation (:stream m))))))
      (core/add-subscriber in-memory aggregation-orders
                      (fn [m]
                        (is (= "orders" (:aggregation (:stream m))))))
      (core/publish in-memory message-orders)))

  (testing "Two subscribers and two aggregations"
    (let [in-memory (publisher/in-memory-publisher)]
      (core/add-subscriber in-memory aggregation-customer
                      (fn [m]
                        (is (= "customer" (:aggregation (:stream m))))))
      (core/add-subscriber in-memory aggregation-orders
                      (fn [m]
                        (is (= "orders" (:aggregation (:stream m))))))
      (core/publish in-memory message-customer)
      (core/publish in-memory message-orders)))
  
  (testing "Remove subscriber"
    (let [in-memory (publisher/in-memory-publisher) 
          subscription (core/add-subscriber in-memory aggregation-customer
                          (fn [m]
                            (is (= "customer" (:aggregation (:stream m))))))]
      (is (subscription)))))