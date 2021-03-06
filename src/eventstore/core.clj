(ns ^{:doc "A Persistence provider for the Event Store. It is responsible for write and read events in the event stream"
      :author "Leandro Rodrigues"}
 eventstore.core 
  (:require [eventstore.domain :as d]))

(defprotocol Publisher
  (publish [this message])
  (add-subscriber [this aggregation subscriber]))

(defprotocol Provider
  (add [this stream data])
  (retrieve-events
    [this stream]
    [this stream offset]
    [this stream offset limit])
  (retrieve-aggregations
    [this]
    [this offset]
    [this offset limit])
  (retrieve-streams
    [this aggregation]
    [this aggregation offset]
    [this aggregation offset limit]))

(defprotocol EventStore
  (add-event
    ^{:arglists '([stream data])
      :doc "Add a new event in the event stream."}
    [this stream data])
  (get-events
    ^{:arglists '([stream & [offset limit]])
      :doc "Retrieves a list of events in the event stream"}
    [this stream]
    [this stream offset]
    [this stream offset limit])
  (get-aggregations
    ^{:arglists '([& [offset limit]])
      :doc "Retrieves the aggregation list"}
    [this]
    [this offset]
    [this offset limit])
  (get-streams
    ^{:arglists '([stream & [offset limit]])
      :doc "Retrieves the stream list"}
    [this aggregation]
    [this aggregation offset]
    [this aggregation offset limit])
  (subscribe 
   ^{:arglists '([aggregation subscriber])
      :doc "Subscribe the events from the store"}
   [this aggregation subscriber]))

(defn event-stream-constructor 
  "Creates an event store function"
  [event-store aggregation id]
  (fn [f & args]
    (let [stream (d/->Stream aggregation id)]
      (apply f event-store stream args))))