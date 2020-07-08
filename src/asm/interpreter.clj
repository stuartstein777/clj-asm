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

(defn cmp [registers internal-registers x y]
  (println registers ":: " x ", " y)
  (let [x-val (if (keyword? x) (get registers x) x)
        y-val (if (keyword? y) (get registers y) y)]
    (assoc internal-registers :cmp (cond-> []
                                      (= x-val y-val) (conj :eq)
                                      (> x-val y-val) (conj :gt)
                                      (< x-val y-val) (conj :lt)))))

(defn interpret [instructions]
  (let [symbol-table (build-symbol-table instructions)]
    (loop [eip          0
           registers    {}
           eip-stack    []
           internal-registers {:cmp []}]
      (if (= eip (count instructions))
        [registers internal-registers]
        (let [[instruction & opcodes] (nth instructions eip)]
          (cond (= :end instruction)
                [registers internal-registers]
                (= :mov instruction)
                (recur (inc eip) (apply (partial mov registers) opcodes) eip-stack internal-registers)
                (= :inc instruction)
                (recur (inc eip) (apply (partial unary-op registers inc) opcodes) eip-stack internal-registers)
                (= :dec instruction)
                (recur (inc eip) (apply (partial unary-op registers dec) opcodes) eip-stack internal-registers)
                (= :mul instruction)
                (recur (inc eip) (apply (partial binary-op registers *) opcodes) eip-stack internal-registers)
                (= :add instruction)
                (recur (inc eip) (apply (partial binary-op registers +) opcodes) eip-stack internal-registers)
                (= :sub instruction)
                (recur (inc eip) (apply (partial binary-op registers -) opcodes) eip-stack internal-registers)
                (= :div instruction)
                (recur (inc eip) (apply (partial binary-op registers quot) opcodes) eip-stack internal-registers)
                (= :xor instruction)
                (recur (inc eip) (apply (partial binary-op registers bit-xor) opcodes) eip-stack internal-registers)
                (= :and instruction)
                (recur (inc eip) (apply (partial binary-op registers bit-and) opcodes) eip-stack internal-registers)
                (= :or instruction)
                (recur (inc eip) (apply (partial binary-op registers bit-or) opcodes) eip-stack internal-registers)
                (= :jmp instruction)
                (recur (apply (partial jmp symbol-table) opcodes) registers eip-stack internal-registers)
                (= :jnz instruction)
                (recur (+ eip (apply (partial jnz registers) opcodes)) registers eip-stack internal-registers)
                (= :cmp instruction)
                (recur (inc eip) registers eip-stack (apply (partial cmp registers internal-registers) opcodes))
                (or (= :nop instruction) (= :label instruction))
                (recur (inc eip) registers eip-stack internal-registers)))))))

(interpret [[:mov :a 6]
            [:xor :a :a]])

