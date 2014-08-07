(ns transitive.server
  (import [io.netty.channel
           ChannelHandlerContext
           ChannelInboundHandlerAdapter])
  (require [transitive.netty :as net]
           [transitive.client :as client]
           [clojure.core.async :as async]))

(declare make-run-client)

(defn start-server [port]
  (let [[listener, shutdown] (net/make-listener make-run-client)]
    (.bind listener (int port))
    shutdown))

(defn make-run-client []
  (let [in (async/chan)
        out (async/chan)]
    (client/run-client in out)
    (proxy [ChannelInboundHandlerAdapter] []
      (channelActive
        [^ChannelHandlerContext ctx]
        (async/go-loop []
                       (let [val (async/<! out)]
                         (cond
                          (nil? val) (.close ctx)
                          :else (let [buf (-> ctx .alloc .buffer)]
                                  (.writeBytes buf (.getBytes (str val)))
                                  (.writeAndFlush ctx buf)
                                  (recur))))))
      (channelRead
        [^ChannelHandlerContext ctx buf]
        (async/go (async/>! in buf)))

      (exceptionCaught
        [^ChannelHandlerContext ctx ^Throwable cause]
        (.printStackTrace cause)
        (.close ctx)
        (async/close! in)))))
