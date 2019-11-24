(ns eventstore.event-store
  (:require
   [clojure.tools.logging :as log]
   [eventstore.domain :as d]
   [eventstore.core :as core]))

(defn event-store
  ([provider] (event-store provider nil))
  ([provider publisher]
   (reify core/EventStore
     (add-event [this stream data]
       (let [new-event (core/add provider stream data)]
         (if publisher
           (core/publish publisher (d/->Message stream new-event))
           (log/info "No publisher available"))))

     (get-events [this stream]
       (core/retrieve-events provider stream))

     (get-events [this stream offset]
       (core/retrieve-events provider stream offset))

     (get-events [this stream offset limit]
       (core/retrieve-events provider stream offset limit))

     (get-aggregations [this]
       (core/retrieve-aggregations provider))

     (get-aggregations [this offset]
       (core/retrieve-aggregations provider offset))

     (get-aggregations [this offset limit]
       (core/retrieve-aggregations provider offset limit))

     (get-streams [this aggregation]
       (core/retrieve-streams provider aggregation))

     (get-streams [this aggregation offset]
       (core/retrieve-streams provider aggregation offset))

     (get-streams [this aggregation offset limit]
       (core/retrieve-streams provider aggregation offset limit))

     (subscribe [this aggregation subscriber]
       (if publisher
         (core/add-subscriber publisher aggregation subscriber)
         (log/info "No publisher available"))))))