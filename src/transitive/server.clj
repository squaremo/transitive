(ns transitive.server
  (require [transitive.netty :as net]))

(defn start-server [port]
  (let [[listener, shutdown] (net/make-listener)]
    (.bind listener (int port))
    shutdown))
