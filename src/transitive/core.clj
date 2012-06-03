(ns transitive.core
  (:use transitive.sexp)
  (:use lamina.core))

;; Take a channel that spurts sequences of 'a and flatten it into a
;; channel that emits 'a
(defn flatten-channel [ch]
  (let [out (channel)]
    (receive-all ch (fn [sq] (doseq [it sq] (enqueue out it))))
    out))

(defn lex-channel [ch]
  (let* [out (channel)
         in (flatten-channel ch)
         lex (fn handle
               [[result val & rest]]
               (if result
                 (do (enqueue out val)
                     (handle (start (first rest))))
                 (receive in (fn [buf] (handle (val buf))))))]
        (receive in (fn [buf] (lex (start buf))))
        out))

(defn lex-printer [val]
  (cond
   (= val :open) (print "(")
   (= val :close) (println ")")
   :else (println (.toString val))))
