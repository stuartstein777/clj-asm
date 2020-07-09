# clj-asm

A very simple toy assembly language parser and interpreter. 

Supported instructions (so far):

<table>
<tr><th>Instruction</th><th>Examples</th><th>Explanation</th></tr>
<tr><td>mov</td><td>"mov a b"</td><td>moves b (number or register) into register :a</td></tr>
<tr><td>add</td><td>"add a b"</td><td>a + b (numbers or registers), result goes into :a</td></tr>
<tr><td>sub</td><td>"sub a b"</td><td>a - b (numbers or registers), result goes into :a</td></tr>
<tr><td>mul</td><td>"mul a b"</td><td>a * b (numbers or registers), result goes into :a</td></tr>
<tr><td>div</td><td>"div a b"</td><td>a / b (numbers or registers), result goes into :a (result is floored)</td></tr>
<tr><td>and</td><td>"and a b"</td><td>a ∧ b (numbers or registers), result goes into :a</td></tr>
<tr><td>or</td><td>"or a b"</td><td>a ∨ b (numbers or registers), result goes into :a</td></tr>
<tr><td>xor</td><td>"xor a b"</td><td>a ⊕ b (numbers or registers), result goes into :a</td></tr>
<tr><td>dec</td><td>"dec a"</td><td>decrements the register :a by one</td></tr>
<tr><td>inc</td><td>"dec a"</td><td>increments the register :a by one</td></tr>
<tr><td>jnz</td><td>"jnz x y"</td><td>jumps y (number or register) instructions (positive or negative) if x (number or register) is not zero.</td></tr>
<tr><td>label</td><td>"foo:"</td><td>Creates a label foo: that can be used by jmp or call instructions. If encountered as an instruction it is ignored.</td></tr>
<tr><td>jmp</td><td>"jmp foo"</td><td>Moves the execution pointer to the label foo.</td></tr>
<tr><td>nop</td><td>"nop"</td><td>Does nothing.</td></tr>
<tr><td>cmp</td><td>"cmp x y"</td><td>compares x and y and stores the result in the internal register :cmp, result will either be x < y, x = y, x > y.</td></tr>
<tr><td>jne</td><td>"jne foo"</td><td>jumps to the label foo if the result of the previous cmp call was that x /= y</td></tr>
<tr><td>jg</td><td>"jg foo"</td><td>jumps to the label foo if the result of the previous cmp call was that x > y</td></tr>
<tr><td>jge</td><td>"jge foo"</td><td>jumps to the label foo if the result of the previous cmp call was that x >= y</td></tr>
<tr><td>je</td><td>"je foo"</td><td>jumps to the label foo if the result of the previous cmp call was that x = y</td></tr>
<tr><td>jle</td><td>"jle foo"</td><td>jumps to the label foo if the result of the previous cmp call was that x <= y</td></tr>
<tr><td>jl</td><td>"jl foo"</td><td>jumps to the label foo if the result of the previous cmp call was that x < y</td></tr>
<tr><td>call</td><td>"call foo"</td><td>Moves the execution pointer to the label foo, pushes the current execution pointer onto an execution pointer so that it can be returned to by a ret instruction.</td></tr>
<tr><td>ret</td><td>"ret"</td><td>returns execution to the top execution pointer on the execution pointer stack.</td></tr>
<tr><td>end</td><td>"end"</td><td>terminates the program and returns the registers.</td></tr>
<tr><td>msg</td><td>"msg 'x= ' x ', y= " y</td><td>Creates a message that is returned on program exit, unlimited arguments can be strings or registers or values.</td></tr>
<tr><td>comments</td><td>";"</td><td>Comments are ignored, can be on own line or e.g. "mov a b  ; moves b into a" </td></tr>
</table>
 
 If the program terminates without encountering an `:end` instruction it sets the return value to -1.
 
 # Registers
 
 The registers are stored in a map. Internal registers such as return-code and cmp are stored as an :internal-register map
 within the registers map.
 
 e.g.
 
 ```{:x 5 :y 6 :internal-registers {:cmp :eq :return-code -1}}``` 
 
 # Examples
 
 ```clojure

(interpret (asm.parser/parse "; function calls.
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
                               msg 'a = ' a ', b = ' b ', c = ' c
                               end        ; a = 7, b = 0, c = 3
            
                               ;; foo:: increment b
                               foo:
                               inc b      ; a = 0, b = 2, c = 2
                               ret        ; ret to foo call, pop eip stack
            
                               ;; bar:: add 7 to a and decrement c
                               bar:
                               add a 7    ; a = 7, b = 2, c = 4
                               sub c 1    ; a = 7, b = 2, c = 3
                               ret        ; ret to bar call, pop eip stack"), true)

=> ["a = 7, b = 0, c = 3" {:a 7, :b 0, :c 3}]
```

# TODO

* Nicer display when running, showing registers and instruction.
