(ns eventstore.in-memory-event-store
  (:require [eventstore.domain :as d]
            [eventstore.core :refer [EventStore] :as store]
            [eventstore.core :refer [Publisher] :as publisher]
            [eventstore.core :refer [Provider] :as provider]
            [eventstore.util :as util])
  (:import java.util.concurrent.ConcurrentHashMap
           java.util.Collections
           java.util.ArrayList
           java.util.function.Function))

(defn in-memory-event-store 
  ([provider] (in-memory-event-store provider nil))
  ([provider publisher]
    (reify store/EventStore
      (add-event [this stream data]
        (let [new-event (provider/add provider stream data)]
          (if publisher 
            (publisher/publish publisher (d/->Message stream new-event)))))

      (get-events [this stream]
        (provider/retrieve-events provider stream))

      (get-events [this stream offset]
        (provider/retrieve-events provider stream offset))

      (get-events [this stream offset limit]
        (provider/retrieve-events provider stream offset limit))

      (get-aggregations [this]
        (provider/retrieve-aggregations provider))

      (get-aggregations [this offset]
        (provider/retrieve-aggregations provider offset))

      (get-aggregations [this offset limit]
        (provider/retrieve-aggregations provider offset limit))

      (get-streams [this aggregation]
        (provider/retrieve-streams provider aggregation))

      (get-streams [this aggregation offset]
        (provider/retrieve-streams provider aggregation offset))

      (get-streams [this aggregation offset limit]
        (provider/retrieve-streams provider aggregation offset limit))
      
      (subscribe [this aggregation subscriber] 
        (if publisher
          (publisher/add-subscriber publisher aggregation subscriber))))))