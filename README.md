# Chisel Project Template
8-bit CPU based on the Ben Eater ISA

# Intro
An 8-bit computer for learning how to build a computer from scratch.

# ISA
|INS|Desciption|Op Code|Mem Address|
|-|-|-|-|
|NOP|No Operation|0b0000|0bXXXX|
|LDA|Load from address into A register|0b0001|4-bit address|
|ADD|Add A and B register, store in A|0b0010|4-bit address|
|SUB|Subtract A and B register, store in A|0b0011|4-bit address|
|STA|Store A register into memory|0b0100|4-bit address|
|LDI|Load immediate into A register|0b0101|4-bit value|
|JMP|Jump to program address|0b0110|4-bit address|
|JC|Jump to if carry is set|0b0111|4-bit address|
|JZ|Jump to if sum is zero|0b1000|4-bit address|
|OUT|Displays contents of A register|0b1110|0bXXXX|
|HLT|Halts processor|0b1111|0bXXXX|

## Example
```
STORE_A=0xe
STORE_B=0xf

store_21a:
  LDI #0x6     // Load 6
  STA $STORE_A // Put 6 into STORE_A address
  LDI #0xf     // Load 15
  ADD $0xf     // Add address STORE_A to reg A
  STA $STORE_A // Store 21 into STORE_A address

store_21b:
  LDI #0x6     // Load 6
  STA $STORE_B // Put 6 into STORE_B address
  LDI #0xf     // Load 15
  ADD $0xf     // Add address STORE_B to reg A
  STA $STORE_B // Store 21 into STORE_B address

add_42:
  LDA $STORE_A // Load contents of STORE_A into reg A
  ADD $STORE_B // Puts STORE_B in reg B then adds reg B and reg A
  OUT          // Outputs the contents of reg A
```

# Components
> Clock and reset are a part of every component
## Instruction Register
|Input|Ouput|
|-|-|
|Instruction from Memory (8-bit)|Op Code (4-bit)|
||Address/Imm Out (4-bit)|
## Program Counter
|Input|Ouput|
|-|-|
|Enable (1-bit)|Address (8-bit)|
|Address (8-bit)||
|Set (1-bit)||
## Memory
|Input|Output|
|-|-|
|Address (8-bit)|Data (8-bit)|
|Data (8-bit)||
|Write Enable (1-bit)||
|Read Enable (1-bit)||
## ALU
|Input|Output|
|-|-|
|Input A (8-bit)|Output (8-bit)|
|Input B (8-bit)|Carry (1-bit)|
|Signed (1-bit)|Zero (1-bit)|
## Registers
|Input|Output|
|-|-|
|Data (8-bit)|Data (8-bit)|
|Write Enable (1-bit)||
## Controller

# Stages
## Stage 1 - Fetch
Gets the next instruction

## Stage 2 - Execute
### NOP
Do Nothing

### LDA
Access memory at address and place it into register A

### ADD
Add register A and register B and then store in register A

### SUB

### STA
Store register A at address

### LDI
Put immediate into register A

### JMP
Load value into program counter

### JC

### JZ

### OUT

### HLT

