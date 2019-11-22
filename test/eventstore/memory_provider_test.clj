(ns eventstore.memory-provider-test
  (:require [clojure.test :refer :all]
            [eventstore.domain :as d]
            [eventstore.core :as c]
            [eventstore.in-memory-provider :as p]))

(defn add-n-events
  ([provider n aggregation id] (add-n-events provider n aggregation id "any json data"))
  ([provider n aggregation id data]
   (dotimes [v n]
     (c/add provider (d/->Stream aggregation id) (str data v)))))

(deftest adding-events-to-provider
  (let [provider (p/in-memory-provider)]
    (testing "Add event to an aggregation"
      (c/add provider (d/->Stream "orders" 123456) "json 1")
      (let [events (c/retrieve-events provider (d/->Stream "orders" 123456))]
        (is (= 1 (count events)))))

    (testing "Add another event to the aggregation"
      (c/add provider (d/->Stream "orders" 123456) "json 2")
      (let [events (c/retrieve-events provider (d/->Stream "orders" 123456))]
        (is (= 2 (count events)))))

    (testing "Add an event to a different id in the same aggregation"
      (c/add provider (d/->Stream "orders" 654321) "json 3")
      (let [events (c/retrieve-events provider (d/->Stream "orders" 654321))]
        (is (= 1 (count events)))))

    (testing "Add an event to a different aggregation"
      (c/add provider (d/->Stream "account" 123456) "json 4")
      (let [events (c/retrieve-events provider (d/->Stream "account" 123456))]
        (is (= 1 (count events)))))))

(deftest retrieving-eventos-from-provider
  (let [provider (p/in-memory-provider)]
    (testing "No events added"
      (is (= 0 (count (c/retrieve-events provider (d/->Stream "orders" 6543210))))))

    (testing "Two events added"
      (add-n-events provider 2 "orders" 1010101)
      (is (= 2 (count (c/retrieve-events provider (d/->Stream "orders" 1010101))))))

    (testing "Filter eventos with offset and return the last event"
      (add-n-events provider 5 "orders" 3131313)
      (let [events (c/retrieve-events provider (d/->Stream "orders" 3131313) 4)]
        (is (= 1 (count events)))))

    (testing "Filter eventos with offset and limit and return the last two events"
      (add-n-events provider 5 "orders" 2121212)
      (let [events (c/retrieve-events provider (d/->Stream "orders" 2121212) 3 2)]
        (is (= 2 (count events)))))))

(deftest retrieving-aggregations-from-provider
  (let [provider (p/in-memory-provider)]
    (testing "No aggregations in the store"
      (let [aggregations (c/retrieve-aggregations provider)]
        (is (empty? aggregations))))

    (testing "Two aggregations in the store"
      (c/add provider (d/->Stream "orders" 123456) "json 1")
      (c/add provider (d/->Stream "account" 654321) "json 2")
      (let [aggregations (c/retrieve-aggregations provider)]
        (is (= 2 (count aggregations)))))

    (testing "Filtering aggregations result by offset"
      (c/add provider (d/->Stream "orders" 123456) "json 1")
      (c/add provider (d/->Stream "account" 654321) "json 2")
      (c/add provider (d/->Stream "offers" 000001) "json 3")
      (let [aggregations (c/retrieve-aggregations provider 2)]
        (is (= 1 (count aggregations)))))

    (testing "Filtering aggregations result by offset and limit"
      (c/add provider (d/->Stream "orders" 123456) "json 1")
      (c/add provider (d/->Stream "account" 654321) "json 2")
      (c/add provider (d/->Stream "offers" 000001) "json 3")
      (c/add provider (d/->Stream "users" 100000) "json 4")
      (let [aggregations (c/retrieve-aggregations provider 1 2)]
        (is (= 2 (count aggregations)))))))

(deftest retrieving-streams-from-provider
  (let [provider (p/in-memory-provider)]
    (testing "No streams in the store"
      (let [streams (c/retrieve-streams provider "orders")]
        (is (empty? streams))))

    (testing "Two streams in the store"
      (c/add provider (d/->Stream "orders" 123456) "json 1")
      (c/add provider (d/->Stream "orders" 654321) "json 2")
      (let [streams (c/retrieve-streams provider "orders")]
        (is (= 2 (count streams)))))

    (testing "Filtering streams result by offset"
      (c/add provider (d/->Stream "orders" 123456) "json 1")
      (c/add provider (d/->Stream "orders" 654321) "json 2")
      (c/add provider (d/->Stream "orders" 100001) "json 3")
      (let [streams (c/retrieve-streams provider "orders" 2)]
        (is (= 1 (count streams)))))

    (testing "Filtering streams result by offset and limit"
      (c/add provider (d/->Stream "orders" 123456) "json 1")
      (c/add provider (d/->Stream "orders" 654321) "json 2")
      (c/add provider (d/->Stream "orders" 100001) "json 3")
      (c/add provider (d/->Stream "orders" 100002) "json 4")
      (c/add provider (d/->Stream "orders" 100003) "json 6")
      (let [streams (c/retrieve-streams provider "orders" 2 2)]
        (is (= 2 (count streams)))))))