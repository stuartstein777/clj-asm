(ns asm.interpreter
  (:require [asm.parser :refer [build-symbol-table]]))

(defn get-value [registers op]
  (if (keyword? op)
    (get registers op)
    op))

(defn asm-mov [registers x y]
  (assoc registers x (get-value registers y)))

(defn unary-op [registers op x]
  (update registers x op))

(defn binary-op [registers op x y]
  (assoc registers x (op (get registers x) (get-value registers y))))

(defn interpret [instructions]
  (let [symbol-table (build-symbol-table instructions)]
    (loop [eip          0
           registers    {}
           eip-stack    []]
      (if (= eip (count instructions))
        registers
        (let [[instruction & opcodes] (nth instructions eip)]
          (cond (= :end instruction)
                registers
                (= :mov instruction)
                (recur (inc eip) (apply (partial asm-mov registers) opcodes) eip-stack)
                (= :inc instruction)
                (recur (inc eip) (apply (partial unary-op registers inc) opcodes) eip-stack)
                (= :dec instruction)
                (recur (inc eip) (apply (partial unary-op registers dec) opcodes) eip-stack)
                (= :mul instruction)
                (recur (inc eip) (apply (partial binary-op registers *) opcodes) eip-stack)
                (= :add instruction)
                (recur (inc eip) (apply (partial binary-op registers +) opcodes) eip-stack)
                (= :sub instruction)
                (recur (inc eip) (apply (partial binary-op registers -) opcodes) eip-stack)
                (= :div instruction)
                (recur (inc eip) (apply (partial binary-op registers quot) opcodes) eip-stack)))))))

(interpret [[:mov :a 5]
            [:inc :a]
            [:mov :b :a]
            [:dec :b]
            [:mul :a :b]
            [:mov :c :b]
            [:add :a :c]
            [:end]
            [:inc :a]])