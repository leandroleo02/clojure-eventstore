(ns eventstore.in-memory-publisher
  (:require [eventstore.core :as core]
            [eventstore.util :as util])
  (:import java.util.concurrent.ConcurrentHashMap
           java.util.Collections
           java.util.ArrayList))

(declare retrieve-listeners-for)

(defn- retrieve-listeners-for [listeners aggregation]
  (.computeIfAbsent listeners aggregation (util/function-apply (fn [t] (Collections/synchronizedList (ArrayList.))))))

(defn in-memory-publisher []
  (let [listeners (ConcurrentHashMap.)]
    (reify core/Publisher
      (publish [this message]
        (let [aggregation-listeners (retrieve-listeners-for listeners (:aggregation (:stream message)))]
          (doseq [subscriber aggregation-listeners]
            (subscriber message))))
      
      (add-subscriber [this aggregation subscriber]
        (let [aggregation-listeners (retrieve-listeners-for listeners aggregation)]
          (.add aggregation-listeners subscriber)
          (fn [] (.remove aggregation-listeners subscriber)))))))