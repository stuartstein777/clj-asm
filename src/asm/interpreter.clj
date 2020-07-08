(ns asm.interpreter
  (:require [asm.parser :refer [build-symbol-table]]))

(defn get-value [registers op]
  (if (keyword? op)
    (get registers op)
    op))

(defn mov [registers x y]
  (assoc registers x (get-value registers y)))

(defn unary-op [registers op x]
  (update registers x op))

(defn binary-op [registers op x y]
  (assoc registers x (op (get registers x) (get-value registers y))))

(defn jnz [registers x y]
  (if (zero? (get-value registers x))
    1
    (get-value registers y)))

(defn jmp [symbol-table label]
  (inc (get symbol-table label)))

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
                (recur (inc eip) (apply (partial mov registers) opcodes) eip-stack)
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
                (recur (inc eip) (apply (partial binary-op registers quot) opcodes) eip-stack)
                (= :jmp instruction)
                (recur (apply (partial jmp symbol-table) opcodes) registers eip-stack)
                (= :jnz instruction)
                (recur (+ eip (apply (partial jnz registers) opcodes)) registers eip-stack)
                (or (= :nop instruction) (= :label instruction))
                (recur (inc eip) registers eip-stack)))))))