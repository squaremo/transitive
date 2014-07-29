(defproject transitive "0.0.2"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.typed "0.2.53"]
                 [org.clojure/tools.trace "0.7.8"]
                 #_[co.paralleluniverse/pulsar "0.6.0"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [io.netty/netty-all "4.0.21.Final"]]
  :main transitive.main

;;  :java-agents [[co.paralleluniverse/quasar-core "0.6.0"]]
)
