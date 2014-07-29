(ns transitive.client
  (require [clojure.core.async :as async]))

(defn run-client [in out]
  (async/go-loop
   [state :open]
   (let [val (async/<! in)]
     (cond
      (nil? val) (async/close! out)
      :else (async/>! out val)))
   (recur :open)))
