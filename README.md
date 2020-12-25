# Chisel Project Template
8-bit CPU based on the Ben Eater ISA

# Intro
An 8-bit computer for learning how to build a computer from scratch.

# How to build
In order to build the CPU and simulate it, run the following commands:
```
# Builds all required source into a build/ folder to run the CPU
make
```

```
# Will load and run the default program
./build/VCPU
```
Prints:
> We got an output! out: 42

## Advanced features
I've built this to allow for more advance debugging features such as simulated trace via gtkwave (.vcd file) and printing out instructions per cycle. You can also load your own compiled program as a .bin

```
# Compile the example loop program
make examples/loop
```
```
./build/VCPU -t build/loop.vcd -b build/loop.bin -v
```
`-t` creates a trace file for debugging
`-b` loads in a binary
`-v` verbose output that prints instructions per clock

Output:
```
We got an output! out: 0
Out[6]: ins 0x61 res 0
Out[1]: ins 0x 0 res 0
Out[2]: ins 0x51 res 0
Out[3]: ins 0x20 res 0
Out[4]: ins 0x40 res 0
Out[5]: ins 0xE0 res 1
We got an output! out: 1
Out[6]: ins 0x61 res 0
Out[1]: ins 0x 0 res 0
Out[2]: ins 0x51 res 0
Out[3]: ins 0x20 res 0
Out[4]: ins 0x40 res 0
Out[5]: ins 0xE0 res 2
We got an output! out: 2
Out[6]: ins 0x61 res 0
Out[1]: ins 0x 0 res 0
```
> Debug the trace file by running `gtkwave`
```
gtkwave build/loop.vcd
```
![alt text](examples/loop.png "Title")

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

