(ns eventstore.in-memory-provider
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

(defn- retrieve-aggregations-for
  ([store] (retrieve-aggregations-for store 0))
  ([store offset & [limit]]
   (drop-and-take (Collections/list (.keys store)) offset limit)))

(defn- retrieve-streams-for
  ([store aggregation] (retrieve-streams-for store aggregation 0))
  ([store aggregation offset & [limit]]
   (let [aggregations (or (.get store aggregation) (ConcurrentHashMap.))
         streams (Collections/list (.keys aggregations))]
     (drop-and-take streams offset limit))))

(defn in-memory-provider []
  (let [store (ConcurrentHashMap.)]
    (reify core/Provider
      (add [this stream data]
        (let [current-events (retrieve-events-for store stream)
              new-event (d/->Event data (System/currentTimeMillis) (.size current-events))]
          (.add current-events new-event)
          new-event))

      (retrieve-events [this stream]
        (into () (retrieve-events-for store stream)))

      (retrieve-events [this stream offset]
        (drop offset (retrieve-events-for store stream)))

      (retrieve-events [this stream offset limit]
        (take limit (drop offset (retrieve-events-for store stream))))

      (retrieve-aggregations [this]
        (retrieve-aggregations-for store))

      (retrieve-aggregations [this offset]
        (retrieve-aggregations-for store offset))

      (retrieve-aggregations [this offset limit]
        (retrieve-aggregations-for store offset limit))

      (retrieve-streams [this aggregation]
        (retrieve-streams-for store aggregation))

      (retrieve-streams [this aggregation offset]
        (retrieve-streams-for store aggregation offset))

      (retrieve-streams [this aggregation offset limit]
        (retrieve-streams-for store aggregation offset limit)))))