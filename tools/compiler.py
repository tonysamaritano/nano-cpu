import sys
import struct
import array
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--asm', help='assembly file')
parser.add_argument('--bin', help='output bin file')

args = parser.parse_args()

class Instruction:
    def __init__(self, opcode, funct3, optype):
        self._opcode = opcode
        self._funct3 = funct3
        self._dst = None
        self._src0 = None
        self._src1 = None
        self._imm = None
        self._type = optype

    def setOperands(self, dst=None, src0=None, src1=None, imm=None):
        self._dst = dst
        self._src0 = src0
        self._src1 = src1
        self._imm = imm

    def getType(self):
        return self._type

    def toBinary(self):
        b = 0x0000
        b = b | self._opcode
        b = b | (self._funct3 << 13)
        return array.array('B', struct.pack('<H', b))

    def _packDst(self, b):
        return self._insertDst(b, self._dst)

    def _packSrc0(self, b):
        return self._insertSrc0(b, self._src0)

    def _packSrc1(self, b):
        return self._insertSrc1(b, self._src1)

    def _insertOp3(self, b, val, offset):
        a = struct.unpack('<H', b)[0]
        a = a | (val << offset)
        return array.array('B', struct.pack('<H', a))

    def _insertDst(self, b, val):
        return self._insertOp3(b, val, 3)

    def _insertSrc0(self, b, val):
        return self._insertOp3(b, val, 6)

    def _insertSrc1(self, b, val):
        return self._insertOp3(b, val, 10)

    def _insertFunct1(self, b, val):
        assert val == 0 or val == 1
        a = struct.unpack('<H', b)[0]
        a = a | (val << 9)
        return array.array('B', struct.pack('<H', a))

    def _insertImm1(self, b, val):
        assert val == 0 or val == 1
        a = struct.unpack('<H', b)[0]
        a = a | (val << 15)
        return array.array('B', struct.pack('<H', a))

    def _convertImm(self, bitwidth):
        r = (1 << bitwidth)
        assert self._imm > -r and self._imm < r, f"Imm is too big. Imm -{r} < {self._imm} < {r}"
        val = abs(self._imm)
        imm = val
        if self._imm < 0:
            val = ~val
            imm = val + 0x1

        return imm

    def __str__(self):
        out = f"Type: {self._type} Op: {self._opcode} F3: {self._funct3}:"
        if self._dst is not None:  out += f"\n  dst:  {self._dst}"
        if self._src0 is not None: out += f"\n  src0: {self._src0}"
        if self._src1 is not None: out += f"\n  src1: {self._src1}"
        if self._imm is not None:  out += f"\n  imm:  {self._imm}"
        return out

class InstructionR(Instruction):
    def toBinary(self):
        b = super().toBinary()
        b = self._packDst(b)
        b = self._packSrc0(b)
        b = self._packSrc1(b)
        return b

class InstructionI(Instruction):
    def toBinary(self):
        b = super().toBinary()
        b = self._packDst(b)
        b = self._packSrc0(b)

        return self._packImm(b)

    def _packImm(self, b):
        signedImm = self._convertImm(bitwidth=4)

        b = self._insertFunct1(b, signedImm & 0x1)
        b = self._insertSrc1(b, (signedImm >> 1) & 0x7)

        return b

class InstructionI5(InstructionI):
    def _packImm(self, b):
        signedImm = self._convertImm(bitwidth=5)

        b = self._insertFunct1(b, signedImm & 0x1)
        b = self._insertSrc1(b, (signedImm >> 1) & 0x7)
        b = self._insertImm1(b, (signedImm >> 4) & 0x1)

        return b
class InstructionU(Instruction):
    def toBinary(self):
        b = super().toBinary()
        b = self._packDst(b)

        return self._packImm(b)

    def _packImm(self, b):
        signedImm = self._convertImm(bitwidth=8)

        b = self._insertFunct1(b, signedImm & 0x1)
        b = self._insertSrc1(b, (signedImm >> 1) & 0x7)
        b = self._insertImm1(b, (signedImm >> 4) & 0x1)
        b = self._insertSrc0(b, (signedImm >> 5) & 0x7)

        return b

class InstructionUU(InstructionU):
    def toBinary(self):
        b = Instruction.toBinary(self)
        return self._packImm(b)

class InstructionC(Instruction):
    def toBinary(self):
        b = super().toBinary()
        b = self._packSrc0(b)
        b = self._packSrc1(b)
        return b

class InstructionS(Instruction):
    def toBinary(self):
        b = super().toBinary()
        b = self._packSrc0(b)
        b = self._packSrc1(b)
        return b

    def _packImm(self, b):
        signedImm = self._convertImm(bitwidth=5)

        b = self._insertFunct1(b, signedImm & 0x1)
        b = self._insertDst(b, (signedImm >> 1) & 0x7)
        b = self._insertImm1(b, (signedImm >> 4) & 0x1)

        return b

class InstructionB(Instruction):
    def toBinary(self):
        b = super().toBinary()
        return self._packImm(b)

    def _packImm(self, b):
        signedImm = self._convertImm(bitwidth=11)

        b = self._insertFunct1(b, signedImm & 0x1)
        b = self._insertSrc1(b, (signedImm >> 1) & 0x7)
        b = self._insertImm1(b, (signedImm >> 4) & 0x1)
        b = self._insertSrc0(b, (signedImm >> 5) & 0x7)
        b = self._insertDst(b,  (signedImm >> 8) & 0x7)

        return b

class InstructionFactory:
    Ins = {
        #               Opcode    Funct3
        "add":  ("R",   0x0,      0x0),
        "addi": ("I",   0x0,      0x1),
        "sub":  ("R",   0x0,      0x2),
        "and":  ("R",   0x1,      0x0),
        "op":   ("R",   0x1,      0x1),
        "xor":  ("R",   0x1,      0x2),
        "not":  ("R",   0x1,      0x3),
        "sll":  ("R",   0x1,      0x4),
        "slli": ("I",   0x1,      0x5),
        "srl":  ("R",   0x1,      0x6),
        "srli": ("I",   0x1,      0x7),
        # "lb":   ("I5",  0x2,      0x0), Unimplemented
        "lw":   ("I5",  0x2,      0x1),
        "lli":  ("UU",  0x2,      0x2),
        "luai": ("U",   0x2,      0x3),
        # "sb":   ("S",   0x3,      0x0), Unimplemented
        "sw":   ("S",   0x3,      0x1),
        "eq":   ("C",   0x4,      0x0),
        "neq":  ("C",   0x4,      0x1),
        "ge":   ("C",   0x4,      0x2),
        "geu":  ("C",   0x4,      0x3),
        "lt":   ("C",   0x4,      0x4),
        "ltu":  ("C",   0x4,      0x5),
        "jalr": ("I5",  0x5,      0x0),
        "jal":  ("U",   0x5,      0x1),
        "br":   ("B",   0x6,      0x0),
        "hlt":  ("R",   0x7,      0x7),
    }

    Operands = {
        "x0": 0, "x1": 1, "x2": 2, "x3": 3, "x4": 4, "x5": 5, "x6": 6, "x7": 7,
        "ra": 1, "sp": 2, "t0": 3, "t1": 4, "s0": 5, "a0": 6, "a1": 7,
    }

    def create(self, insStr):
        ins, ops = self._readIns(insStr.split("//")[0].rstrip().replace(",","").split(" "))
        opcode = InstructionFactory.Ins[ins]

        # Set Operands
        I = Instruction(opcode[1], opcode[2], opcode[0])
        if opcode[0]=="R":
            assert len(ops)==3, "R type must have 3 operands"
            I.setOperands(
                dst=ops[0],
                src0=ops[1],
                src1=ops[2])
        elif opcode[0]=="I" or opcode[0]=="I5":
            assert len(ops)==3, "I type must have 3 operands"
            I.setOperands(
                dst=ops[0],
                src0=ops[1],
                imm=ops[2])
        elif opcode[0]=="UU" or opcode[0]=="B":
            assert len(ops)==1, "UU type must have 1 operands"
            I.setOperands(
                imm=ops[0])
        elif opcode[0]=="U":
            assert len(ops)==2, "UUU type must have 2 operands"
            I.setOperands(
                dst=ops[0],
                imm=ops[1])
        elif opcode[0]=="S":
            assert len(ops)==3, "S type must have 3 operands"
            I.setOperands(
                src0=ops[0],
                src1=ops[1],
                imm=ops[2])
        elif opcode[0]=="C":
            assert len(ops)==2, "C type must have 2 operands"
            I.setOperands(
                src0=ops[0],
                src1=ops[1])

        # Return Derived Class
        return self.convert(I)

    def convert(self, ins):
        if ins.getType()=="R":
            ins.__class__ = InstructionR
        elif ins.getType()=="I":
            ins.__class__ = InstructionI
        elif ins.getType()=="I5":
            ins.__class__ = InstructionI5
        elif ins.getType()=="U":
            ins.__class__ = InstructionU
        elif ins.getType()=="UU":
            ins.__class__ = InstructionUU
        elif ins.getType()=="C":
            ins.__class__ = InstructionC
        elif ins.getType()=="B":
            ins.__class__ = InstructionB
        elif ins.getType()=="S":
            ins.__class__ = InstructionS

        return ins

    def _readIns(self, insArray):
        ins = insArray[0]
        ops = insArray[1:]

        return ins, self._convertOps(ops)

    def _convertOps(self, operands):
        ops = []
        for op in operands:
            if op in InstructionFactory.Operands:
                ops.append(InstructionFactory.Operands[op])
            elif InstructionFactory._isNumber(op):
                ops.append(int(op))

        return ops

    @staticmethod
    def _isNumber(val):
        try:
            int(val)
            return True
        except:
            return False

factory = InstructionFactory()

# ins = [
#     "add    x1, x2, x3      // poop",
#     "addi   x3, x7, -1       // poop",
#     "sub    x1, x2, x6      // shit",
#     "and    x1, x5, t1      // balls",
#     "op     x1, x5, t1      // balls",
#     "xor    x1, x5, t1      // balls",
#     "not    x1, x5, t1      // balls",
#     "sll    x1, x5, t1      // balls",
#     "slli   x1, x5, -7      // balls",
#     "srl    x1, x5, t1      // balls",
#     "srli   x1, x5, -2      // balls",
#     "lw     x1, x5, -1     // balls",
#     "lli    -1             // balls",
#     "luai   x2, -1         // balls",
#     "sw     x3, x4, 255     // balls",
#     "eq     x3, x4          // balls",
#     "neq    x3, x4          // balls",
#     "ge     x3, x4          // balls",
#     "geu    x3, x4          // balls",
#     "lt     x3, x4          // balls",
#     "ltu    x3, x4          // balls",
#     "jalr   x1, x5, -1      // balls",
#     "jal    x1, -1          // balls",
#     "br     -1             // balls",
# ]

asm = open(args.asm, "r")
output = open(f"{args.bin}", "wb")

for i in asm:
    I = factory.create(i)
    b = I.toBinary()
    # print(f'{hex(b[1])} {hex(b[0])} = {I}')
    output.write(b)

asm.close()
output.close()
