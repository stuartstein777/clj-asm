(ns asm.macros)

(defmacro apply-unary-function [registers eip eip-stack op & opcodes]
  (list 'recur (list 'inc eip) (list 'apply (list 'partial 'unary-op registers op) 'opcodes) eip-stack))

(defmacro apply-binary-function [registers eip eip-stack op & opcodes]
  (list 'recur (list 'inc eip) (list 'apply (list 'partial 'binary-op registers op) 'opcodes) eip-stack))

