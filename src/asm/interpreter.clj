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

(defn call [symbol-table label]
  (label symbol-table))

(defn interpret [instructions]
  (let [symbol-table (build-symbol-table instructions)]
    (loop [eip 0
           registers {}
           eip-stack []]
      (if (= eip (count instructions))
        (assoc-in registers [:internal-registers :exit-code] -1)
        (let [[instruction & opcodes] (nth instructions eip)]
          (cond (= :end instruction)
                registers
                (= :mov instruction) (recur (inc eip) (apply (partial mov registers) opcodes) eip-stack)
                (= :inc instruction) (recur (inc eip) (apply (partial unary-op registers inc) opcodes) eip-stack)
                (= :dec instruction) (recur (inc eip) (apply (partial unary-op registers dec) opcodes) eip-stack)
                (= :mul instruction) (recur (inc eip) (apply (partial binary-op registers *) opcodes) eip-stack)
                (= :add instruction) (recur (inc eip) (apply (partial binary-op registers +) opcodes) eip-stack)
                (= :sub instruction) (recur (inc eip) (apply (partial binary-op registers -) opcodes) eip-stack)
                (= :div instruction) (recur (inc eip) (apply (partial binary-op registers quot) opcodes) eip-stack)
                (= :xor instruction) (recur (inc eip) (apply (partial binary-op registers bit-xor) opcodes) eip-stack)
                (= :and instruction) (recur (inc eip) (apply (partial binary-op registers bit-and) opcodes) eip-stack)
                (= :or instruction)  (recur (inc eip) (apply (partial binary-op registers bit-or) opcodes) eip-stack)
                (= :cmp instruction) (recur (inc eip)  (apply (partial cmp registers) opcodes) eip-stack)
                (= :jmp instruction) (recur (apply (partial jmp symbol-table) opcodes) registers eip-stack)
                (= :jnz instruction) (recur (+ eip (apply (partial jnz registers) opcodes)) registers eip-stack)
                (= :jne instruction) (recur (apply (partial cmp-jmp registers symbol-table eip #{:lt :gt}) opcodes) registers eip-stack)
                (= :je instruction)  (recur (apply (partial cmp-jmp registers symbol-table eip #{:eq}) opcodes) registers eip-stack)
                (= :jge instruction) (recur (apply (partial cmp-jmp registers symbol-table eip #{:eq :gt}) opcodes) registers eip-stack)
                (= :jg instruction) (recur (apply (partial cmp-jmp registers symbol-table eip #{:gt}) opcodes) registers eip-stack)
                (= :jl instruction) (recur (apply (partial cmp-jmp registers symbol-table eip #{:lt}) opcodes) registers eip-stack)
                (= :jle instruction) (recur (apply (partial cmp-jmp registers symbol-table eip #{:lt :eq}) opcodes) registers eip-stack)
                (= :call instruction) (let [call-location (apply (partial call symbol-table) opcodes)]
                                        (recur call-location registers (conj eip-stack eip)))
                ;; if we hit a ret and have no pointer to the location to return to, exit with exit code -1
                (= :ret instruction) (if (nil? eip-stack)
                                       (assoc-in registers [:internal-registers :exit-code] -1)
                                       (recur (inc (last eip-stack)) registers (butlast eip-stack)))
                (or (= :nop instruction) (= :label instruction)) (recur (inc eip) registers eip-stack)))))))

(interpret [[:mov :a 7]                                     ; 0
            [:call :foo]                                    ; 1
            [:mul :a 7]                                     ; 2
            [:end]                                          ; 3
            [:label :foo]                                   ; 4
            [:inc :a]                                       ; 5
            [:ret]])                                        ; 6