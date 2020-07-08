(ns asm.core
  (:require [asm.parser :refer [parse]]
            [asm.interpreter :refer [interpret]])
  (:gen-class))

;; instructions we want to support
;; DONE: mov x y    ::    copies y to x (y can be integer or register).
;; DONE: inc x      ::    increment the register x.
;; DONE: dec x      ::    decrement the register x.
;; DONE: add x y   ::     add the content of register x with y (y can be register or value), stores result in x.
;; DONE: sub x y   ::     subtract y (either value or register) from x, store the result in x.
;; DONE: mul x y   ::     multiply the contents of y (either value or register) with x, store the result in x.
;; DONE: div x y   ::     divide the contents of x with y (either value or register), store the result in x.
;; DONE: label:     ::    define a label identifier for jumps.
;; DONE: jmp lbl    ::    jumps to the label lbl, moves the eip.
;; DONE: jnz x y    ::    jumps y instructions (positive or negative) if x is not zero.
;; DONE: cmp x y    ::    compares x (either value or register) with y (either value or register). Result is used.
;; DONE: jne lbl    ::    jump to the label lbl if the values of the previous cmp command were not equal.
;; DONE: je lbl     ::    jump to the label lbl if the values of the previous cmp command were equal.
;; TODO: jge lbl    ::    jump to the label lbl if x >= y in previous cmp command.
;; TODO: jg lbl     ::    jump to the label lbl if x > y in previous cmp command.
;; TODO: jle lbl    ::    jump to the label lbl if x <= y in previous cmp command.
;; TODO: jl lbl     ::    jump to the label lbl if x < y in previous cmp command.
;; DONE: xor x y    ::    bit-xor x with y and store the result in x.
;; DONE: or x y     ::    bit-or x with y and store the result in x.
;; DONE: and x y    ::    bit-and x with y and store the result in x.
;; DONE nop         ::    do nothing.
;; TODO: call lbl   ::    call the subroutine identified by lbl. When a ret is found in a subroutine, the eip should
;; TODO:                  return to the instruction next to this call command.
;; TODO: ret        ::    when a ret is found in a subroutine, the eip should return to the instruction that called the
;; TODO:                  function.
;; TODO: msg 'reg'  ::    this stores the output of the program. It may contain text strings (delimited by single
;; TODO:                  quotes) and registers, number of arguments isn't limited.
;;                        Example: msg 'a: ', a, ' is not equal to ', b
;;                        Given, :a = 5 and :b = 6.
;;                        This would store "5 is not equal to 6"
;; DONE: end        ::    indicates the program ends correctly, so output is returned. If this command isn't encountered
;; TODO:                  the program should return -1
;; DONE;            ::    comments.
;; DONE:            ::    parser to parse from string to vector of instructions
;; DONE: Parser to parse out labels and eip locations into map
;; How to deal with ret and msg 'reg' ???? Still to figure out...
;; initially not support numbers in register names.
;; TODO:            ::    step by step instruction output, with registers (int and binary).
;;

(defn -main [& args]
  (->>   (parse args)
         (interpret)))