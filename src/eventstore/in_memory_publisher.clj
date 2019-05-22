(ns eventstore.in-memory-publisher
  (:require [eventstore.domain :as d]
            [eventstore.core :as core]
            [eventstore.util :as util])
  (:import java.util.concurrent.ConcurrentHashMap
           java.util.Collections
           java.util.ArrayList
           java.util.function.Function))

(declare retrieve-listeners-for)

(defn- retrieve-listeners-for [listeners aggregation]
  (.computeIfAbsent listeners aggregation (util/function-apply (fn [t] (Collections/synchronizedList (ArrayList.))))))

(defn in-memory-publisher []
  (let [listeners (ConcurrentHashMap.)]
    (reify core/Publisher
      (publish [this message]
        (let [aggregation-listeners (retrieve-listeners-for listeners (:aggregation (:stream message)))]
          (if-not (empty? aggregation-listeners)
            (doseq [subscriber aggregation-listeners]
              (subscriber message)))))
      
      (subscribe [this aggregation subscriber]
        (let [aggregation-listeners (retrieve-listeners-for listeners aggregation)]
          (.add aggregation-listeners subscriber)
          (fn [] (.remove aggregation-listeners subscriber)))))))