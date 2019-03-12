(ns eventstore.in-memory-event-store
  (:require [eventstore.domain :as d]
            [eventstore.core :as core]
            [eventstore.util :as util])
  (:import java.util.concurrent.ConcurrentHashMap
           java.util.Collections
           java.util.ArrayList
           java.util.function.Function))

(declare retrieve-events-for)

(defn- retrieve-events-for [store stream]
  (let [aggregate-streams (.computeIfAbsent store (:aggregation stream) (util/function-apply (fn [t] (ConcurrentHashMap.))))]
    (.computeIfAbsent aggregate-streams (:id stream) (util/function-apply (fn [t] (Collections/synchronizedList (ArrayList.)))))))

(defn- drop-and-take [coll offset & [limit]]
  (take (or limit (count coll)) (drop offset coll)))

(defn- retrieve-aggregations
  ([store] (retrieve-aggregations store 0))
  ([store offset & [limit]] 
   (drop-and-take (Collections/list (.keys store)) offset limit)))

(defn- retrieve-streams
  ([store aggregation] (retrieve-streams store aggregation 0))
  ([store aggregation offset & [limit]]
   (let [aggregations (or (.get store aggregation) (ConcurrentHashMap.))
         streams (Collections/list (.keys aggregations))]
     (drop-and-take streams offset limit))))

(def in-memory-event-store
  "A Event Store that handle all the data in memory. 
  It is a very simple implementation that should be used only for development and test purposes."
  (let [store (ConcurrentHashMap.)]
    (reify core/EventStore
      (add-event [this stream data]
        (let [current-events (retrieve-events-for store stream)
              new-event (d/->Event data (System/currentTimeMillis) (.size current-events))]
          (.add current-events new-event)))

      (get-events [this stream]
        (into () (retrieve-events-for store stream)))

      (get-events [this stream offset]
        (drop offset (retrieve-events-for store stream)))

      (get-events [this stream offset limit]
        (take limit (drop offset (retrieve-events-for store stream))))

      (get-aggregations [this]
        (retrieve-aggregations store))

      (get-aggregations [this offset]
        (retrieve-aggregations store offset))

      (get-aggregations [this offset limit]
        (retrieve-aggregations store offset limit))

      (get-streams [this aggregation]
        (retrieve-streams store aggregation))

      (get-streams [this aggregation offset]
        (retrieve-streams store aggregation offset))

      (get-streams [this aggregation offset limit]
        (retrieve-streams store aggregation offset limit))

      (clear-store [this]
        (.clear store)))))