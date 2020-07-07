(ns asm.core
  (:gen-class))

;; instructions we want to support
;; TODO: mov x y    ::    copies y to x (y can be integer or register).
;; TODO: inc x      ::    increment the register x.
;; TODO: dec x      ::    decrement the register x.
;; TODO: add x y   ::    add the content of register x with y (y can be register or value), stores result in x.
;; TODO: sub x y   ::    subtract y (either value or register) from x, store the result in x.
;; TODO: mul x y   ::    multiply the contents of y (either value or register) with x, store the result in x.
;; TODO: div x y   ::    divide the contents of x with y (either value or register), store the result in x.
;; TODO: label:     ::    define a label identifier for jumps.
;; TODO: jmp lbl    ::    jumps to the label lbl, moves the eip.
;; TODO: cmp x y   ::    compares x (either value or register) with y (either value or register). Result is used.
;; TODO: jne lbl    ::    jump to the label lbl if the values of the previous cmp command were not equal.
;; TODO: je lbl     ::    jump to the label lbl if the values of the previous cmp command were equal.
;; TODO: jge lbl    ::    jump to the label lbl if x >= y in previous cmp command.
;; TODO: jg lbl     ::    jump to the label lbl if x > y in previous cmp command.
;; TODO: jle lbl    ::    jump to the label lbl if x <= y in previous cmp command.
;; TODO: jl lbl     ::    jump to the label lbl if x < y in previous cmp command.
;; TODO: call lbl   ::    call the subroutine identified by lbl. When a ret is found in a subrouting, the eip should
;; TODO:                  return to the instruction next to this call command.
;; TODO:  ret       ::    when a ret is found in a subroutine, the eip should return to the instruction that called the
;; TODO:                  function.
;; TODO: msg 'reg'  ::    this stores the output of the program. It may contain text strings (delimited by single
;; TODO:                  quotes) and registers, number of arguments isn't limited.
;;                        Example: msg 'a: ', a, ' is not equal to ', b
;;                        Given, :a = 5 and :b = 6.
;;                        This would store "5 is not equal to 6"
;; TODO: end        ::    indicates the program ends correctly, so output is returned. If this command isn't encountered
;; TODO:                  the program should return -1
;; TODO: ;          ::    comments.

;; TODO: Parser to parse string of program into vector of instructions and operands
;; TODO: Parser to parse out labels and eip locations into map
;; How to deal with ret and msg 'reg' ???? Still to figure out...

(defn -main
  "I don't do a whole lot ... yet."
  [& _]
  (println "Hello, World!"))
