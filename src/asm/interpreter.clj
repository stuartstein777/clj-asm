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

(defn cmp [registers x y]
  (let [x-val (if (keyword? x) (get registers x) x)
        y-val (if (keyword? y) (get registers y) y)]
    (assoc-in registers [:internal-registers :cmp] (cond (= x-val y-val) (conj :eq)
                                                         (> x-val y-val) (conj :gt)
                                                         (< x-val y-val) (conj :lt)))))

(defn cmp-jmp [registers symbol-table eip comparer x lbl]
  (if (comparer x (:cmp (:internal-registers registers)))
    (lbl symbol-table)
    (inc eip)))

(defn interpret [instructions]
  (let [symbol-table (build-symbol-table instructions)]
    (loop [eip 0
           registers {}
           eip-stack []]
      (if (= eip (count instructions))
        registers
        (let [[instruction & opcodes] (nth instructions eip)]
          (println instruction " :: " registers)
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
                (= :xor instruction)
                (recur (inc eip) (apply (partial binary-op registers bit-xor) opcodes) eip-stack)
                (= :and instruction)
                (recur (inc eip) (apply (partial binary-op registers bit-and) opcodes) eip-stack)
                (= :or instruction)
                (recur (inc eip) (apply (partial binary-op registers bit-or) opcodes) eip-stack)
                (= :jmp instruction)
                (recur (apply (partial jmp symbol-table) opcodes) registers eip-stack)
                (= :jnz instruction)
                (recur (+ eip (apply (partial jnz registers) opcodes)) registers eip-stack)
                (= :cmp instruction)
                (recur (inc eip)  (apply (partial cmp registers) opcodes) eip-stack)
                (= :jne instruction)
                (recur (apply (partial cmp-jmp registers symbol-table eip not= :eq) opcodes) registers eip-stack)
                (= :je instruction)
                (recur (apply (partial cmp-jmp registers symbol-table eip = :eq) opcodes) registers eip-stack)
                (or (= :nop instruction) (= :label instruction))
                (recur (inc eip) registers eip-stack)))))))

(interpret [[:mov :a 7]
            [:mov :b 7]
            [:cmp :a :b]
            [:je :foo]
            [:mul :a :b]
            [:end]
            [:cmp :a :b]
            [:label :foo]
            [:inc :a]])

