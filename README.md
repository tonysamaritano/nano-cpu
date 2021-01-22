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
|fl|Flags|Flags Register|

I think this will be a nice, simple way for the processor to be mildly useful.

## ISA Overview
|Instruction|Type|funct3 (3b)|src1 (3b)|func1 (1b)|src0 (3b)|dst (3b)|Opcode (3b)|
|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|ADD|R|`0x0`|src1|`N/A`|src0|dst|`0x0`|
|ADDI|I|`0x1`|imm[`3:1`]|imm[`0`]|src0|dst|`0x0`|
|SUB|R|`0x2`|src1|`N/A`|src0|dst|`0x0`|
|AND|R|`0x0`|src1|`N/A`|src0|dst|`0x1`|
|OR|R|`0x1`|src1|`N/A`|src0|dst|`0x1`|
|XOR|R|`0x2`|src1|`N/A`|src0|dst|`0x1`|
|NOT|R|`0x3`|`N/A`|`N/A`|src0|dst|`0x1`|
|SLL|R|`0x4`|`N/A`|`N/A`|src0|dst|`0x1`|
|SLLI|I|`0x5`|imm[`3:1`]|imm[`0`]|src0|dst|`0x1`|
|SRL|R|`0x6`|`N/A`|`N/A`|src0|dst|`0x1`|
|SRLI|I|`0x7`|imm[`3:1`]|imm[`0`]|src0|dst|`0x1`|
|LB|I|imm[`4`], `0x0`|imm[`3:1`]|imm[`0`]|src0|dst|`0x2`|
|LW|I|imm[`4`], `0x1`|imm[`3:1`]|imm[`0`]|src0|dst|`0x2`|
|LLI|U|imm[`4`], `0x2`|imm[`3:1`]|imm[`0`]|imm[`7:5`]|dst|`0x2`|
|LUAI|U|imm[`4`], `0x3`|imm[`3:1`]|imm[`0`]|imm[`7:5`]|dst|`0x2`|
|SPC|U|imm[`4`], `0x3`|imm[`3:1`]|imm[`0`]|imm[`7:5`]|dst|`0x3`|
|SB|S|imm[`4`], `0x1`|src1|imm[`0`]|src0|imm[`3:1`]|`0x3`|
|SW|S|imm[`4`], `0x2`|src1|imm[`0`]|src0|imm[`3:1`]|`0x3`|
|EQ|R|`0x0`|src1|`N/A`|src0|dst|`0x4`|
|NEQ|R|`0x1`|src1|`N/A`|src0|dst|`0x4`|
|GE|R|`0x2`|src1|`N/A`|src0|dst|`0x4`|
|GEU|R|`0x3`|src1|`N/A`|src0|dst|`0x4`|
|LT|R|`0x4`|src1|`N/A`|src0|dst|`0x4`|
|LTU|R|`0x5`|src1|`N/A`|src0|dst|`0x4`|
|JALR|I|imm[`4`], `0x0`|imm[`3:1`]|imm[`0`]|src0|dst|`0x5`|
|JAL|U|imm[`4`], `0x1`|imm[`3:1`]|imm[`0`]|imm[`7:5`]|dst|`0x5`|
|BFEQ|U|imm[`4`], `0x0`|imm[`3:1`]|imm[`0`]|imm[`7:5`]|dst|`0x6`|
|BFNE|U|imm[`4`], `0x1`|imm[`3:1`]|imm[`0`]|imm[`7:5`]|dst|`0x6`|
|BFGE|U|imm[`4`], `0x2`|imm[`3:1`]|imm[`0`]|imm[`7:5`]|dst|`0x6`|
|BFLT|U|imm[`4`], `0x3`|imm[`3:1`]|imm[`0`]|imm[`7:5`]|dst|`0x6`|
|CLR|R|`0x0`|`N/A`|`N/A`|`N/A`|dst|`0x7`|
|FLG|R|`0x1`|`N/A`|`N/A`|`N/A`|dst|`0x7`|
|HLT|R|`0x7`|`N/A`|`N/A`|`N/A`|`N/A`|`0x7`|

## ISA Description
|Instruction|Description|Example|Explaination|
|:-:|:-|:-|:-|
|ADD|Adds two registers|ADD x3, x4, x5|Adds contents of x4 and x5, stores in x3|
|ADDI|Adds immediate value (4-bits) to register|ADDI x3, x4, 1|Adds 1 to x4, stores in x3|
|SUB|Subtracts two registers|SUB x3, x4, x5|Subtracts contents of x4 and x5, stores in x3|
|LW|Loads word from memory|LW x3, 2(x4)|Loads word from address in x4 + 2 into x3|
> TODO: Finish ISA description

## Arithmatic Operations
Add or subtract signed integers. These operations are for arithmatic operations like `ADD`, `SUB`, `ADDI`, etc. operations.
> TODO: Finish Documentation

# Components
## ALU
The ALU needs to take in two inputs (A and B registers) and multiplex control inputs. The ALU will be able to add or subtract either signed or unsigned integers. The ALU will output the result, a bit for carry, and a bit for zero.
> TODO: Add GE, LT, EQ, NE flags

|Requirement|Desciption|Complete|
|-|-|-|
|Unsigned Addition|The ALU shall implement unsigned addition|Complete|
|Zero Result Bit|The ALU shall output a zero bit from the result if the result of the addition or subtraction is 0|Complete|
|Carry Result Bit|The ALU shall output a carry/borrow bit if there was an overflow as a result of the operation|Complete|

## Register File
> TODO: DDocument Register File

## Control Signals
> TODO: update control signals to include the new ISA

## Immediate Generation
The immediate generation module inputs an instruction and converts the instruction into immediate values.

|Requirement|Desciption|Complete|
|-|-|-|
|Instruction|The immediate generation shall use the instruction to generate the desired value|Complete|
|Input ctl|The immediate generation shall select its generation output from the controller module|Complete|
|Output Full Width Imm|The immediate generation shall output full width words based on input generation|Complete|

