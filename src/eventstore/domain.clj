(ns eventstore.domain)

(defrecord Event [payload commit-timestamp sequence])
(defrecord Stream [aggregation id])
(defrecord Message [stream event])