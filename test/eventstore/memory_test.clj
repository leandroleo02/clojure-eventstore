(ns eventstore.memory-test
  (:require [clojure.test :refer :all]
            [eventstore.domain :as d]
            [eventstore.core :refer [EventStore] :as eventstore]
            [eventstore.in-memory-event-store :refer :all]))

(defn clear-store-fixture [f]
  (f)
  (eventstore/clear-store in-memory-event-store))

(use-fixtures :each clear-store-fixture)

(defn add-n-events
  ([n aggregation id] (add-n-events n aggregation id "any json data"))
  ([n aggregation id data]
   (dotimes [v n]
     (eventstore/add-event in-memory-event-store (d/->Stream aggregation id) (str data v)))))

(deftest adding-events-to-the-eventstore
  (testing "Add event to an aggregation"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 123456) "json 1")
    (let [events (eventstore/get-events in-memory-event-store (d/->Stream "orders" 123456))]
      (is (= 1 (count events)))))

  (testing "Add another event to the aggregation"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 123456) "json 2")
    (let [events (eventstore/get-events in-memory-event-store (d/->Stream "orders" 123456))]
      (is (= 2 (count events)))))

  (testing "Add an event to a different id in the same aggregation"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 654321) "json 3")
    (let [events (eventstore/get-events in-memory-event-store (d/->Stream "orders" 654321))]
      (is (= 1 (count events)))))

  (testing "Add an event to a different aggregation"
    (eventstore/add-event in-memory-event-store (d/->Stream "account" 123456) "json 4")
    (let [events (eventstore/get-events in-memory-event-store (d/->Stream "account" 123456))]
      (is (= 1 (count events))))))

(deftest retrieving-eventos-from-eventstore
  (testing "No events added"
    (is (= 0 (count (eventstore/get-events in-memory-event-store (d/->Stream "orders" 6543210))))))

  (testing "Two events added"
    (add-n-events 2 "orders" 1010101)
    (is (= 2 (count (eventstore/get-events in-memory-event-store (d/->Stream "orders" 1010101))))))

  (testing "Filter eventos with offset and return the last event"
    (add-n-events 5 "orders" 3131313)
    (let [events (eventstore/get-events in-memory-event-store (d/->Stream "orders" 3131313) 4)]
      (is (= 1 (count events)))))

  (testing "Filter eventos with offset and limit and return the last two events"
    (add-n-events 5 "orders" 2121212)
    (let [events (eventstore/get-events in-memory-event-store (d/->Stream "orders" 2121212) 3 2)]
      (is (= 2 (count events))))))

(deftest retrieving-aggregations-from-eventstore
  (testing "No aggregations in the store"
    (let [aggregations (eventstore/get-aggregations in-memory-event-store)]
      (is (empty? aggregations))))

  (testing "Two aggregations in the store"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 123456) "json 1")
    (eventstore/add-event in-memory-event-store (d/->Stream "account" 654321) "json 2")
    (let [aggregations (eventstore/get-aggregations in-memory-event-store)]
      (is (= 2 (count aggregations)))))

  (testing "Filtering aggregations result by offset"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 123456) "json 1")
    (eventstore/add-event in-memory-event-store (d/->Stream "account" 654321) "json 2")
    (eventstore/add-event in-memory-event-store (d/->Stream "offers" 000001) "json 3")
    (let [aggregations (eventstore/get-aggregations in-memory-event-store 2)]
      (is (= 1 (count aggregations)))))

  (testing "Filtering aggregations result by offset and limit"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 123456) "json 1")
    (eventstore/add-event in-memory-event-store (d/->Stream "account" 654321) "json 2")
    (eventstore/add-event in-memory-event-store (d/->Stream "offers" 000001) "json 3")
    (eventstore/add-event in-memory-event-store (d/->Stream "users" 100000) "json 4")
    (let [aggregations (eventstore/get-aggregations in-memory-event-store 1 2)]
      (is (= 2 (count aggregations))))))

(deftest retrieving-streams-from-eventstore
  (testing "No streams in the store"
    (let [streams (eventstore/get-streams in-memory-event-store "orders")]
      (is (empty? streams))))
  
  (testing "Two streams in the store"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 123456) "json 1")
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 654321) "json 2")
    (let [streams (eventstore/get-streams in-memory-event-store "orders")]
      (is (= 2 (count streams)))))
  
  (testing "Filtering streams result by offset"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 123456) "json 1")
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 654321) "json 2")
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 100001) "json 3")
    (let [streams (eventstore/get-streams in-memory-event-store "orders" 2)]
      (is (= 1 (count streams)))))
  
  (testing "Filtering streams result by offset and limit"
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 123456) "json 1")
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 654321) "json 2")
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 100001) "json 3")
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 100002) "json 4")
    (eventstore/add-event in-memory-event-store (d/->Stream "orders" 100003) "json 6")
    (let [streams (eventstore/get-streams in-memory-event-store "orders" 2 2)]
      (is (= 2 (count streams))))))