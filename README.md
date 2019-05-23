# clojure-eventstore [![Build Status](https://travis-ci.com/leandroleo02/clojure-eventstore.svg?branch=master)](https://travis-ci.com/leandroleo02/clojure-eventstore)
A simple and fast EventStore that supports multiple persistence and notification providers

# Usage Guide

## Create EventStore

A notification Publisher (Optional): Responsible for notify any process interested in modifications on the store streams.
If there is no publisher provided, the event store will not send any notification.

```clojure
(def event-store (in-memory-event-store (in-memory-publisher)))
```

## Adding Events

To add Events you need to create an EventStream. You can add Events passing anything you want as a payload.

```clojure
(def orders-stream (event-stream-constructor event-store "orders" "123456"))
(orders-stream add-event "My Event Payload")
```

## Reading to Events

To read Events you need to create an EventStream. You can read a stream to receive an ordered list containing all the events in the store.

```clojure
(def orders-stream (event-stream-constructor event-store "orders" "123456"))
(def events (orders-stream get-events))
```

## Reacting to Events

You can add subscribers to be notified every time a new event is added in any stream contained in the given aggregation. The following example will be notified for every new event in any stream in the "orders" aggregation.

```clojure
(orders-stream subscribe "orders" (fn [message] 
                                      (println (:stream message)) 
                                      (println (:event message))))
```

## Removing subscriptions

It is possible to cancel subscriptions to event stream channels.

```clojure
(def remove-subscription (orders-stream subscribe "orders" (fn [message] 
                                      (println (:stream message)) 
                                      (println (:event message)))))
                                      
(remove-subscription)
```
