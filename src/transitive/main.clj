(ns transitive.main
  (require [transitive.server :as server]))

(defn -main
  ([] (println "transitive <port>"))
  ([port]
     (println "Starting server on" port)
     (server/start-server (Integer/parseInt port))))
