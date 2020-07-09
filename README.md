clj-asm

A very simple toy assembly language parser and interpreter.

Supported instructions (so far):
Instruction	Examples	Explanation
mov	"mov a b"	moves b (number or register) into register :a
add	"add a b"	a + b (numbers or registers), result goes into :a
sub	"sub a b"	a - b (numbers or registers), result goes into :a
mul	"mul a b"	a * b (numbers or registers), result goes into :a
div	"div a b"	a / b (numbers or registers), result goes into :a (result is floored)
and	"and a b"	a ∧ b (numbers or registers), result goes into :a
or	"or a b"	a ∨ b (numbers or registers), result goes into :a
xor	"xor a b"	a ⊕ b (numbers or registers), result goes into :a
dec	"dec a"	decrements the register :a by one
inc	"dec a"	increments the register :a by one
jnz	"jnz x y"	jumps y (number or register) instructions (positive or negative) if x (number or register) is not zero.
label	"foo:"	Creates a label foo: that can be used by jmp or call instructions. If encountered as an instruction it is ignored.
jmp	"jmp foo"	Moves the execution pointer to the label foo.
nop	"nop"	Does nothing.
cmp	"cmp x y"	compares x and y and stores the result in the internal register :cmp, result will either be x < y, x = y, x > y.
jne	"jne foo"	jumps to the label foo if the result of the previous cmp call was that x /= y
jg	"jg foo"	jumps to the label foo if the result of the previous cmp call was that x > y
jge	"jge foo"	jumps to the label foo if the result of the previous cmp call was that x >= y
je	"je foo"	jumps to the label foo if the result of the previous cmp call was that x = y
jle	"jle foo"	jumps to the label foo if the result of the previous cmp call was that x <= y
jl	"jl foo"	jumps to the label foo if the result of the previous cmp call was that x < y
call	"call foo"	Moves the execution pointer to the label foo, pushes the current execution pointer onto an execution pointer so that it can be returned to by a ret instruction.
ret	"ret"	returns execution to the top execution pointer on the execution pointer stack.
end	"end"	terminates the program and returns the registers.
comments	";"	Comments are ignored, can be on own line or e.g. "mov a b ; moves b into a"

If the program terminates without encountering an :end instruction it sets the return value to -1.
Registers

The registers are stored in a map. Internal registers such as return-code and cmp are stored as an :internal-register map within the registers map.

e.g.

{:x 5 :y 6 :internal-registers {:cmp :eq :return-code -1}}
Examples

(interpret (parse "; function calls.
                  mov a 0    ; a = 0
                  mov b 1    ; a = 0, b = 1
                  mov c 2    ; a = 0, b = 1, c = 2
                  call foo   ; move eip to foo, push eip to eip-stack
                  mul c b    ; a = 0, b = 2, c = 4        
                  cmp a b    ; :cmp = lt
                  jne quax   ; jump
                  mul c 10   ;
                  
                  ;; quax:: call bar and zero :b
                  quax:      ; 
                  nop        ;
                  call bar   ; move eip to bar, push eip to eip-stack
                  xor b b    ; a = 7, b = 0, c = 3 
                  end        ; a = 7, b = 0, c = 3
                  
                  ;; foo:: increment b
                  foo:
                  inc b      ; a = 0, b = 2, c = 2
                  ret        ; ret to foo call, pop eip stack
                  
                  ;; bar:: add 7 to a and decrement c
                  bar:
                  add a 7    ; a = 7, b = 2, c = 4
                  sub c 1    ; a = 7, b = 2, c = 3
                  ret        ; ret to bar call, pop eip stack"))

=> {:a 7, :b 0, :c 3, :internal-registers {:cmp :lt}}

TODO

msg command to allow setting a messages.

prn command to allow displaying a message.

set command to allow setting a return value.
