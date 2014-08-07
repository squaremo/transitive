(ns transitive.client
  (require [clojure.core.async :as async]
           [transitive.sexp :as sexp]))

(defn run-client [in out]
  (async/go-loop
   [parse sexp/start]
   (if (nil? val)
     (async/close! out)
     (let [val (async/<! in)]
       (recur ; go-loop
        (loop [parse parse]
          (if (= 0 (.readableBytes val))
            parse
            (let [[state result] (parse (.readByte val))]
              (case state
                :return (do (println "received" result)
                          (async/>! out result)
                            (recur sexp/start))
                :more (recur result))))))))))
