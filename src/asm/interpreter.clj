(ns asm.interpreter
  (:require [asm.parser :refer [build-symbol-table]]
            [asm.helpers :refer :all]))

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
        ; get the current instruction in the instructions list at the eip location and
        ; destructure into the instruction and its arguments.
        (let [[instruction & args] (nth instructions eip)]
          (cond (= :end instruction)
                registers

                (= :mov instruction)
                (let [[x y] args]
                  (recur (inc eip) (mov registers x y) eip-stack))

                (in-set? #{:inc :dec} instruction)
                (recur (inc eip) (unary-op registers (get-unary-operation instruction) (first args)) eip-stack)

                (in-set? #{:mul :add :sub :div :xor :and :or} instruction)
                (let [[x y] args]
                  (recur (inc eip) (binary-op registers (get-binary-operations instruction) x y) eip-stack))

                (= :cmp instruction)
                (let [[x y] args]
                  (recur (inc eip) (cmp registers x y) eip-stack))

                (= :jmp instruction)
                (recur (jmp symbol-table (first args)) registers eip-stack)

                (= :jnz instruction)
                (let [[x y] args]
                  (recur (+ eip (jnz registers x y)) registers eip-stack))

                (in-set? #{:jne :je :jge :jg :jle :jl} instruction)
                (let [pred (cmp-jump-predicates instruction)
                      x (first args)]
                  (recur (cmp-jmp registers symbol-table eip pred x) registers eip-stack))

                (= :call instruction)
                  (recur (call symbol-table (first args)) registers (conj eip-stack eip))

                (= :ret instruction)
                (cond (empty? eip-stack) (assoc-in registers [:internal-registers :exit-code] -1)
                      :else            (recur (inc (peek eip-stack)) registers (pop eip-stack)))

                (in-set? #{:nop :label} instruction)
                (recur (inc eip) registers eip-stack)))))))