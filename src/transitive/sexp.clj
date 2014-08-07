(ns transitive.sexp)

(def ZERO (.codePointAt "0" 0))
(def NINE (.codePointAt "9" 0))
(def COLN (.codePointAt ":" 0))
(def LPAR (.codePointAt "(" 0))
(def RPAR (.codePointAt ")" 0))

(declare fresh)
(declare string)
(declare prefix)

(defn return [stack value]
  (if (empty? stack)
    [:return value]
    (let [[head & tail] stack
          newhead (cons value head)]
      [:more (fresh (cons newhead tail))])))

(defn openp [stack]
  [:more (fresh (cons '() stack))])

(defn closep [stack]
  (if (empty? stack)
    (throw "Unexpected closing paren")
    (let [[head & tail] stack]
      (if (empty? tail)
        [:return (reverse head)]
        [:more (fresh (cons (cons (reverse head) (first tail))
                            (rest tail)))]))))
(defn fresh [stack]
  (fn [byte]
    (cond
     (<= ZERO byte NINE)
     [:more (prefix stack (- byte ZERO))]

     (= byte LPAR)
     (openp stack)

     (= byte RPAR)
     (closep stack)

     :else
     (throw "Unexpected character"))))

(defn prefix [stack already]
  (fn [byte]
    (cond
     (<= ZERO byte NINE)
     [:more (prefix stack (+ (* 10 already) (- byte ZERO)))]

     (= byte COLN)
     (if (zero? already)
       (return stack "")
       [:more (string stack '() already)])

     :else
     (throw "Unexpected character"))))

(defn string [stack gathered left]
  (fn [byte]
    (if (= left 1)
      (return stack (String. (-> (cons (char byte) gathered)
                                 reverse into-array char-array)))
      [:more (string stack (cons (char byte) gathered) (- left 1))])))

(def start (fresh '()))
