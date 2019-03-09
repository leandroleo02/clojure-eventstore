(ns eventstore.core-test
  (:require [clojure.test :refer :all]
            [eventstore.domain :as d]
            [eventstore.core :refer :all :as core]
            [eventstore.in-memory-event-store :refer :all]))

(defn clear-store-fixture [f]
  (f)
  (core/clear-store in-memory-event-store))

(use-fixtures :each clear-store-fixture)

(deftest create-event-stream-and-use-its-functions
  (testing "Add an event with event-stream"
    (let [orders-stream (core/event-stream-constructor in-memory-event-store "orders" "123456")]
      (orders-stream core/add-event "json 1")
      (is (= 1 (count (orders-stream core/get-events))))))
  
  (testing "Retrieves events with event-stream with offset"
    (let [orders-stream (core/event-stream-constructor in-memory-event-store "orders" "123457")]
      (orders-stream core/add-event "json 1")
      (orders-stream core/add-event "json 2")
      (orders-stream core/add-event "json 3")
      (is (= 2 (count (orders-stream core/get-events 1))))))
  
  (testing "Retrieves events with event-stream with offsetn and limit"
    (let [orders-stream (core/event-stream-constructor in-memory-event-store "orders" "123458")]
      (orders-stream core/add-event "json 1")
      (orders-stream core/add-event "json 2")
      (orders-stream core/add-event "json 3")
      (orders-stream core/add-event "json 4")
      (orders-stream core/add-event "json 5")
      (orders-stream core/add-event "json 6")
      (is (= 3 (count (orders-stream core/get-events 2 3)))))))

  