(ns eventstore.util
  (:import java.util.function.Function))

(defn function-apply [lambda]
  (reify Function
    (apply [this t]
      (lambda t))))