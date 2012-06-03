;; SExp codec

(ns transitive.sexp)

(def OPAREN 0x28)
(def CPAREN 0x29)
(def ZERO   0x30)
(def COLON  0x3a)

;; For insulation

(defn- remaining [buf]
  (.remaining buf))

(defn- get-byte [buf pos]
  (.get buf pos))

(defn- slice
  ([buf at len]
     (.position buf at) ;; reset it?
     (let [newbuf (.slice buf)]
       (.limit newbuf len)))
  ([buf at]
     (.position buf at)
     (.slice buf)))

(defn- copyInto [source target]
  (.put target (.array source) (.arrayOffset source) (.remaining source)))

(defn- buf-concat [buf1 buf2]
  (let [newbuf (java.nio.ByteBuffer/allocate
                (+ (remaining buf1) (remaining buf2)))]
    (copyInto buf1 newbuf)
    (copyInto buf2 newbuf)
    (.rewind newbuf)))

(defn- stroflen [buf len pos]
  (let [needed (+ len pos)]
    (if (> needed (remaining buf))
      [false (fn [newbuf]
               (stroflen (buf-concat buf newbuf) len pos))]
      [true (slice buf pos len) (slice buf needed)])))

(defn- strprefix [buf pos sofar]
  (if (>= pos (remaining buf))
    [false (fn [newbuf]
             (strprefix newbuf 0 sofar))]
    (let [char (get-byte buf pos)]
      (cond (= char COLON)
            (stroflen buf sofar (inc pos))
            :else
            (recur buf (inc pos) (+ (- char ZERO) ( * 10 sofar)))))))

(defn start [buf pos]
  (if (> (remaining buf) 0)
    (let [char (get-byte buf pos)]
      (cond (= char OPAREN)
            [true :open (slice buf (inc pos))]
            (= char CPAREN)
            [true :close (slice buf (inc pos))]
            :else
            (strprefix buf (inc pos) (- char ZERO))))
    [false start]))
