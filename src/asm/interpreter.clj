(ns asm.interpreter
  (:require [asm.parser :refer [build-symbol-table]]))

;;=======================================================================================================
;; if op is  register, returns the value from the registers.
;; Otherwise return the op (as it's a value not a register).
;;=======================================================================================================
(defn get-value [registers op]
  (if (keyword? op)
    (get registers op)
    op))

;;=======================================================================================================
;; Handle the mov instruction.
;;=======================================================================================================
(defn mov [registers x y]
  (assoc registers x (get-value registers y)))

;;=======================================================================================================
;; Handle unary operations
;;=======================================================================================================
(defn unary-op [registers op x]
  (update registers x op))

;;=======================================================================================================
;; Handle binary operations
;;=======================================================================================================
(defn binary-op [registers op x y]
  (println "in binary op")
  (assoc registers x (op (get registers x) (get-value registers y))))

;;=======================================================================================================
;; Return the predicate for cmp jumps that we want the jump check to satisfy.
;;=======================================================================================================
(defn cmp-jump-predicates [jump-instruction]
  (cond (= :jge jump-instruction) #{:eq :gt}
        (= :jg jump-instruction) #{:gt}
        (= :jne jump-instruction) #{:lt :gt}
        (= :je jump-instruction) #{:eq}
        (= :jle jump-instruction) #{:eq :lt}
        (= :jl jump-instruction) #{:lt}))

;;=======================================================================================================
;; Return the appropriate binary operation for the given binary instruction.
;;=======================================================================================================
(defn get-binary-operations [instruction]
  (cond (= :add instruction) +
        (= :sub instruction) -
        (= :mul instruction) *
        (= :div instruction) quot
        (= :xor instruction) bit-xor
        (= :or instruction) bit-or
        (= :and instruction) bit-and))

;;=======================================================================================================
;; Return the appropriate unary operation for the given unary instruction.
;;=======================================================================================================
(defn get-unary-operation [instruction]
  (cond (= :inc instruction) inc
        (= :dec instruction) dec))

;;=======================================================================================================
;; jump forward or backwards y steps if x is not zero.
;; x and y can both be registers so we get their value via (get-value).
;;=======================================================================================================
(defn jnz [registers x y]
  (if (zero? (get-value registers x))
    1
    (get-value registers y)))

;;=======================================================================================================
;; jump to a label location. We find the label location from the symbol table.
;;=======================================================================================================
(defn jmp [symbol-table label]
  (inc (get symbol-table label)))

;;=======================================================================================================
;; compare x and y and store if x > y, x = y or x < y
;; the result is stored in internal-registers :cmp register.
;;=======================================================================================================
(defn cmp [registers x y]
  (let [x-val (if (keyword? x) (get registers x) x)
        y-val (if (keyword? y) (get registers y) y)]
    (assoc-in registers [:internal-registers :cmp] (cond (= x-val y-val) (conj :eq)
                                                         (> x-val y-val) (conj :gt)
                                                         (< x-val y-val) (conj :lt)))))

;;=======================================================================================================
;; Returns true if the specified instruction is a cmp jmp instruction, otherwise false.
;;=======================================================================================================
(defn is-cmp-jmp? [instruction]
  (not (nil? (#{:jne :je :jge :jg :jle :jl} instruction))))

;;=======================================================================================================
;; Returns true if the specified instruction is a binary instruction, otherwise false.
;;=======================================================================================================
(defn is-binary-operation? [instruction]
  (not (nil? (#{:mul :add :sub :div :xor :and :or} instruction))))

;;=======================================================================================================
;; Returns true if the specified instruction is a unary instruction, otherwise false.
;;=======================================================================================================
(defn is-unary-operation? [instruction]
  (not (nil? (#{:inc :dec} instruction))))

;;=======================================================================================================
;; After a cmp either the comparison will be in one of three states, :eq, :gt, :lt
;;
;; Therefore, to check if we need to jump or not, we simple have to pass in a set of allowed states.
;; e.g,
;; jge :: we would pass in #{:eq :gt}, then we can check if the cmp register is either :eq or :gt.
;;
;; If it is in the set then we can return the location for the label (lbl) in the symbol table.
;; Otherwise we just return the eip incremented so we advance to the next instruction.
;;=======================================================================================================
(defn cmp-jmp [registers symbol-table eip valid-comps lbl]
  (if (nil? (valid-comps (:cmp (:internal-registers registers))))
    (inc eip)
    (lbl symbol-table)))

;;=======================================================================================================
;; Handle call instructions.
;; We return the eip we want to jump to from the symbol table for the given label.
;;=======================================================================================================
(defn call [symbol-table label]
  (label symbol-table))

;;=======================================================================================================
;; The interpreter.
;;
;; Recursively handle each instruction in our set of instructions.
;; Keep track of the eip.
;; eip-stack is a vector containing the return instruction pointers for call / ret
;; Exit when either we hit an :end or the eip is beyond the last instruction (when this occurs return -1.
;; as the exit code).
;;=======================================================================================================
(defn interpret [instructions]
  (let [symbol-table (build-symbol-table instructions)]
    (loop [eip 0
           registers {}
           eip-stack []]
      (if (= eip (count instructions))
        (assoc-in registers [:internal-registers :exit-code] -1)
        (let [[instruction & args] (nth instructions eip)]
          (cond (= :end instruction)
                registers
                (= :mov instruction)
                (recur (inc eip) (apply (partial mov registers) args) eip-stack)
                (is-unary-operation? instruction)
                (recur (inc eip) (apply (partial unary-op registers (get-unary-operation instruction)) args) eip-stack)
                (is-binary-operation? instruction)
                (recur (inc eip) (apply (partial binary-op registers (get-binary-operations instruction)) args) eip-stack)
                (= :cmp instruction)
                (recur (inc eip)  (apply (partial cmp registers) args) eip-stack)
                (= :jmp instruction)
                (recur (apply (partial jmp symbol-table) args) registers eip-stack)
                (= :jnz instruction)
                (recur (+ eip (apply (partial jnz registers) args)) registers eip-stack)
                (is-cmp-jmp? instruction)
                (recur (apply (partial cmp-jmp registers symbol-table eip (cmp-jump-predicates instruction)) args)
                        registers
                        eip-stack)
                (= :call instruction)
                (let [call-location (apply (partial call symbol-table) args)]
                  (recur call-location registers (conj eip-stack eip)))
                (= :ret instruction) (if (nil? eip-stack)
                                       (assoc-in registers [:internal-registers :exit-code] -1)
                                       (recur (inc (last eip-stack)) registers (butlast eip-stack)))
                (or (= :nop instruction) (= :label instruction)) (recur (inc eip) registers eip-stack)))))))