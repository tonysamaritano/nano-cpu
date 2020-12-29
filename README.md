# Chisel Project Template
8-bit von Neumann CPU inspired by the Ben Eater ISA. The point of this project is to create an 8-bit computer for learning how to build a computer from scratch based on my current understanding of computer architecture.

# How to build (__deprecated__)
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

# ISA 1.0 (Ben Eater)

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

# ISA 2.0

|INS|Desciption|Op Code|Mem Address|
|-|-|-|-|
|ADD|Add A and B register, store in A|0b0000|0b0XXX|
|SUB|Subtract A and B register, store in A|0b0000|0b1XXX|
|LDA|Load from address into A register|0b0001|4-bit address lower|
|LDAH|Load from address into A register|0b0010|4-bit address upper|
|LDB|Load from address into B register|0b0011|4-bit address lower|
|LDBH|Load from address into B register|0b0100|4-bit address upper|
|LDI|Load immediate into A register|0b0101|4-bit value lower|
|LDIH|Load from immediate into A register|0b0110|4-bit value upper|
|STA|Store contents in A to address stored in B|0b0111|0bXXXX|
|JMP|Jump to address in A register|0b1000|0bXXXX|
|JSR|Jump to Subroutine at address in A register|0b1001|0b0XXX|
|RSR|Return from Subroutine at address in A register|0b1001|0b1XXX|
|BZ|Branch to relative, unsigned address if result of reg A and reg B is zero|0b1010|4-bit unsigned relative address|
|CLR|Clear processor flags|0b1011|0b0XXX|
|FLG|Copy processor flags to reg A|0b1011|0b1XXX|
|HLT|Halts processor|0b1111|0bXXXX|

# ISA 2.1
Its useful to break down an ISA into its component parts: arithmatic, load/store, branch, and CSR. I think the 8-bit CPU is too limiting for this project because you have no overhead to do anything significant. I think I'd rather change the architecture to a limited 16-bit architecture so that I don't need to deal with the complexities of fitting everything into such a small space.

I want to put all opcodes into a 3-bit space and then the different types of can be organized accordingly.

The instructions should read in destination, source order (Intel syntax).

The register stucture will be simular to RISC-V with a zero register and the PC as its own register. The register map will look like this:

|Register|Register Name|Desciption|
|-|-|-|
|x0|zero|This register is read only and only returns zero|
|x1|ra|Return address register|
|x2|sp|Stack pointer register|
|x3|t0|Temporary Storage 0|
|x4|t1|Temporary Storage 1|
|x5|s0|Saved Register 0|
|x6|a0|General purpose or for passing valiables into a function|
|x7|a1|General purpose or for passing valiables into a function|

|Register|Register Name|Desciption|
|-|-|-|
|pc|pc|Program Counter|

I think this will be a nice, simple way for the processor to be mildly useful.

## A-Type Operations
Add or subtract signed integers. These operations are for arithmatic and bit manupulation operations like `ADD`, `SUB`, `AND`, `OR`, etc. operations.

|Reg2(3-bit)/Imm|Reg1(3-bit)|Reg0(3-bit)|Type (4-bit)|Opcode (3-bit)|
|-|-|-|-|-|
|src1|src0|dst|`ADD` (`0x0`)|Arithmatic (`0x0`)|
|imm|src0|dst|`ADDI` (`0x1`)|Arithmatic (`0x0`)|
|src1|src0|dst|`SUB` (`0x2`)|Arithmatic (`0x0`)|
|src1|src0|dst|`AND` (`0x3`)|Arithmatic (`0x0`)|
|src1|src0|dst|`OR` (`0x4`)|Arithmatic (`0x0`)|
|src1|src0|dst|`XOR` (`0x5`)|Arithmatic (`0x0`)|
||src0|dst|`NOT` (`0x6`)|Arithmatic (`0x0`)|


## Load/Store
`LDI`, `LDIH`: Load immediate into destination register

`LDB, LDW`: Load Byte from address in src register into destination register

`STB, STW`: Store Byte (1-byte) or word (2-bytes) in register into destination address

### Immediate
|Immediate(8-bit)|Register(3-bit)|Type (2-bit)|Opcode (3-bit)|
|-|-|-|-|-|
|imm|dst|`LDI` (`0x0`)|LS (`0x1`)|
|imm|dst|`LDIH` (`0x1`)|LS (`0x1`)|

### Address
|Word (1-bit)|Register(3-bit)|Register(3-bit)|Type (2-bit)|Opcode (3-bit)|
|-|-|-|-|-|
|`0x0`|src|dst|`LDB` (`0x2`)|LS (`0x1`)|
|`0x1`|src|dst|`LDW` (`0x2`)|LS (`0x1`)|
|`0x0`|src|dst|`STB` (`0x3`)|LS (`0x1`)|
|`0x1`|src|dst|`STW` (`0x3`)|LS (`0x1`)|


## Branch/Jmp
`JMP`: unconditional jump to address loaded in source register

`JSR`: jump to subroutine at address loaded in source register

`RSR`: return from subroutine

`BZ`: branch on zero to address in src

`BC`: branch on carry to address in src

|Arguments|Type (4-bit)|Opcode (3-bit)|
-|-|-|
|src (3-bit)|`JMP` (`0x0`)|BR (`0x2`)|
|src (3-bit)|`JSR` (`0x1`)|BR (`0x2`)|
||`RSR` (`0x2`)|BR (`0x2`)|
|src (3-bit)|`BZ` (`0x3`)|BR (`0x2`)|


## CSR

`CLR`: clear processor flags

`FLG`: copy processor flags into dst register

`HLT`: halt the processor

|Arguments|Type (4-bit)|Opcode (3-bit)|
-|-|-|
||`CLR` (`0x0`)|CSR (`0x3`)|
|dst (3-bit)|`FLG` (`0x1`)|CSR (`0x3`)|
||`HLT` (`0x2`)|CSR (`0x3`)|

# Components
## ALU
The ALU needs to take in two inputs (A and B registers) and multiplex control inputs. The ALU will be able to add or subtract either signed or unsigned integers. The ALU will output the result, a bit for carry, and a bit for zero.

|Requirement|Desciption|Complete|
|-|-|-|
|Unsigned Addition|The ALU shall implement unsigned addition|Complete|
|Zero Result Bit|The ALU shall output a zero bit from the result if the result of the addition or subtraction is 0|Complete|
|Carry Result Bit|The ALU shall output a carry/borrow bit if there was an overflow as a result of the operation|Complete|

### How it works
Once decoded, the `ADD`, `SUB` instruction must store the result of A and B in register A.
```
Clock 1:
- Put result of A and B on the bus and latch the bus to A
- Store zero and carry flag
```
I think this can be done in one clock cycle.

## Register File
The register file holds the A and B registers. It must store data based on an enable and control signal. It'll always output the values in the A and B registers to the rest of the CPU

|Requirement|Desciption|Complete|
|-|-|-|
|Store En Input|The register file shall store in either A, B, A and B, or none off of the bus|Complete|
|Output A and B|The register file shall output the A and B registers to the CPU|Complete|

