;; Network relay; used to proxy to and from a socket

(ns transitive.net
  (:import [org.jboss.netty.buffer ChannelBuffers ChannelBuffer])
  (:use [transitive.channel])
  (:require [aleph.tcp :as tcp])
  (:require [lamina.core :as lamina]))

(def ^:dynamic CONNECTION_TIMEOUT 30000)

(defn string-to-channelbuf [^String string]
  (ChannelBuffers/wrappedBuffer (.getBytes string)))

(defn serialise [value]
  (def OPEN  (string-to-channelbuf "("))
  (def CLOSE (string-to-channelbuf ")"))
  (cond (= :open value)  OPEN
        (= :close value) CLOSE
        :else (string-to-channelbuf (str (count value) ":" value))))

(defn create-client [remoteAddr]
  (let [open (tcp/tcp-client remoteAddr)
        ;; Argh we get Netty ChannelBuffers, not what I've written the lexer for
        bytes (fn [^ChannelBuffer buffer]
                (.toByteBuffer buffer))
        ;; bufs (fn [^java.nio.HeapByteBuffer bytes]
        ;;        (org.jboss.netty.buffer.ChannelBuffer/wrappedBuffer bytes))
        [near far] (lamina/channel-pair)
        readpipe (comp parse-channel lex-channel #(lamina/map* bytes %))
        writepipe (comp #(lamina/map* serialise %)  unparse-channel)]

    (lamina/run-pipeline
     open
     (fn [open]
       ;; Transform incoming packets into terms
       (lamina/siphon (readpipe open) far)
       ;; Transform outgoing terms into packets
       (lamina/siphon (writepipe far) open))
     (fn [_] near))))
