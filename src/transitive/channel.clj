(ns transitive.channel
  (:require [transitive.sexp :as sexp])
  (:require [lamina.core :as lamina]))

;; Take a channel that spurts sequences of 'a and flatten it into a
;; channel that emits 'a
(defn flatten-channel [ch]
  (let [out (lamina/channel)]
    (lamina/receive-all ch (fn [sq] (doseq [it sq]
                                      (lamina/enqueue out it))))
    out))

(defn lex-channel [ch]
  (let* [out (lamina/channel)
         in (flatten-channel ch)
         lex (fn handle
               [[result val & rest]] ;; either [true output & rest]
                                     ;;     or [false parseFn & rest]
               (if result
                 (do (lamina/enqueue out val)
                     (handle (sexp/lex (first rest))))
                 (lamina/receive in (fn [buf]
                                      (handle (val buf))))))]
        (lamina/receive in (fn [buf]
                             (lex (sexp/lex buf))))
        out))


;; parse :: token -> parse

(defn parse-channel [lexed]
  (let [out (lamina/channel)]
    ;; no letrec in clojure, yuck
    (letfn [(readtok [parse] (lamina/receive lexed parse))

            (error [e] (lamina/error out e))

            ;; start := sexp*
            ;; sexp  := string | ( sexp* )
            ;; sexp* := nil | sexp sexp*
            (sexp* [acc k]
              (fn [token]
                (readtok (cond (= :close token)
                               (k (reverse acc))
                               
                               (= :open token)
                               ;; %% this uses stack when the
                               ;; %% continuation is called
                               (sexp* () (fn [vals] (sexp* (cons vals acc) k)))
                               
                               :else
                               (sexp* (cons token acc) k)))))
            
            (sexp [k]
              (fn [token]
                (cond (= :close token)
                      (error "Expected open paren or string")
                      
                      (= :open token)
                      (readtok (sexp* () k))

                      :else
                      (readtok (k token)))))
            
            (start [] (sexp (fn [value]
                              (lamina/enqueue out value)
                              (start))))]
      (readtok (start))
      out)))
