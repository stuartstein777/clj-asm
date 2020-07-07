(ns asm.interpreter
  (:require [asm.parser :refer [build-symbol-table]]
            [asm.macros :refer :all]))

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

(defn jmp [symbol-table opcode]
  (inc (get symbol-table opcode)))

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
                (apply-unary-function registers eip eip-stack inc opcodes)
                (= :dec instruction)
                (apply-unary-function registers eip eip-stack dec opcodes)
                (= :mul instruction)
                (apply-binary-function registers eip eip-stack * opcodes)
                (= :add instruction)
                (apply-binary-function registers eip eip-stack + opcodes)
                (= :sub instruction)
                (apply-binary-function registers eip eip-stack - opcodes)
                (= :div instruction)
                (apply-binary-function registers eip eip-stack quot opcodes)
                (= :jmp instruction)
                (recur (apply (partial jmp symbol-table) opcodes) registers eip-stack)
                (= :jnz instruction)
                (recur (+ eip (apply (partial jnz registers) opcodes)) registers eip-stack)
                (or (= :nop instruction) (= :label instruction))
                (recur (inc eip) registers eip-stack)))))))

(interpret [[:mov :a 5]
            [:inc :a]
            [:nop]
            [:mov :b :a]
            [:dec :b]
            [:jmp :foo]
            [:mul :a :b]
            [:mov :c :b]
            [:add :a :c]
            [:label :foo]
            [:end]
            [:inc :a]])