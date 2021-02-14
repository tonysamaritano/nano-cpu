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
            assert len(ops)==2, "U type must have 2 operands"
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
            elif InstructionFactory._isRelative(op):
                split = op.split("(")
                imm = split[0]
                reg = split[1].split(")")[0]

                if (reg in InstructionFactory.Operands and InstructionFactory._isNumber(imm)):
                    ops.append(InstructionFactory.Operands[reg])
                    ops.append(int(imm))

        return ops

    @staticmethod
    def _isNumber(val):
        try:
            int(val)
            return True
        except:
            return False

    @staticmethod
    def _isRelative(val):
        if "(" in val and ")" in val:
            return True
        else:
            return False

class Stage():
    def execute(self):
        return "No File"

class PreProcessor(Stage):
    def __init__(self, file):
        self._file = file

    def execute(self):
        outfile = self._file.split("/")[-1:][0].split(".")[0] + ".s"
        out = open(outfile, "w")
        asm = open(self._file, "r")

        for line in asm:
            l = line.split("//")[0].rstrip()
            if len(l) > 0:
                out.write(l + '\n')

        out.close()
        asm.close()

        return outfile

class Subroutine():
    def __init__(self, name):
        self._name = name
        self._routine = []

    def addInstruction(self, ins):
        self._routine.append(ins.lstrip())

    def getRoutine(self):
        return self._routine

    def __str__(self):
        out = f"Subroutine {self._name}:"
        for ins in self._routine:
            out += f"\n  {ins}"

        return out

class DataItem():
    def __init__(self, name, value):
        self._name = name
        self._value = value

    def toBytes(self):
        pass

    def __str__(self):
        return f"{self._name} = {self._value}"

class WordDataItem(DataItem):
    def toBytes(self):
        return array.array('B', struct.pack('<H', int(self._value)))

class StringDataItem(DataItem):
    def toBytes(self):
        return array.array('B', struct.pack(f'{len(self._value)}s', self._value.encode('utf-8')))

class Assembler(Stage):
    Sections = [
        ".data",
        ".text",
    ]

    TextKeywords = [
        "global"
    ]

    DataKeywords = [
        ".word",
        ".string",
    ]

    def __init__(self, file):
        self._file = file
        self._currentSection = None
        self._currentSubroutine = None
        self._subroutines = dict()
        self._currentDataitem = None
        self._dataitems = dict()
        self._entry = None

    def execute(self):
        asm = open(self._file, "r")

        for line in asm:
            if line.rstrip() in Assembler.Sections:
                self._currentSection = line.rstrip()
                continue

            if self._currentSection in Assembler.Sections[0]:
                self._processDataSection(line.rstrip())
            if self._currentSection in Assembler.Sections[1]:
                self._processTextSection(line.rstrip())

        asm.close()

        return None

    def _processTextSection(self, line):
        # This is a hack to find global
        if Assembler.TextKeywords[0] in line:
            self._currentSubroutine = None
            self._entry = line.lstrip().split(" ")[1]
            return

        if line[0].isspace() and self._currentSubroutine is not None:
            self._subroutines[self._currentSubroutine].addInstruction(line.lstrip())
        else:
            sr = line.split(':')[0]
            self._subroutines[sr] = Subroutine(sr)
            self._currentSubroutine = sr

    def _processDataSection(self, line):
        if line[0].isspace():
            dataItem = line.lstrip().split(" ")
            dataType = dataItem[0]

            if ".word" in dataType:
                self._dataitems[self._currentDataitem] = WordDataItem(self._currentDataitem, dataItem[1])
            elif ".string" in dataType:
                self._dataitems[self._currentDataitem] = StringDataItem(self._currentDataitem, dataItem[1])

            self._currentDataitem = None
        else:
            self._currentDataitem = line.split(":")[0]

    def getDataItems(self):
        return self._dataitems

    def getSubroutines(self):
        return self._subroutines

    def getEntry(self):
        return self._entry

class Linker(Stage):
    def __init__(self, assembler, output):
        self._assembler = assembler
        self._output = output

    def execute(self):
        factory = InstructionFactory()
        entry = self._assembler.getEntry()
        entryLocation = 0
        subroutines = self._assembler.getSubroutines()
        dataitems = self._assembler.getDataItems()
        links = {}

        output = open(f"{self._output}", "wb")

        # Write Header
        output.write(array.array('B', struct.pack('<H', 0)))
        output.write(array.array('B', struct.pack('<H', 0)))
        output.write(array.array('B', struct.pack('<H', 0)))
        mem = 6 # start at 6th byte

        for d in dataitems:
            data = dataitems[d].toBytes()
            output.write(data)
            links[d] = mem
            mem += len(data)

        for key in subroutines:
            links[key] = mem

            if key == entry:
                entryLocation = mem

            for i in subroutines[key].getRoutine():
                for link in links:
                    if link in i:
                        # TODO: This is a hack, need a better way of handling this
                        if "jal " in i:
                            i = i.replace(link, f"{int((links[link]-mem)/2)}")
                        else:
                            i = i.replace(link, f"{links[link]}(x0)")

                mem += 2

                I = factory.create(i)
                output.write(I.toBinary())

        # Go back to the beginning to write initial jump
        output.seek(0)
        I = factory.create(f"jal x0, {int(entryLocation/2)}")
        output.write(I.toBinary())

        output.close()
        return None

# The preprocessor strips the file and prepares it for the assembler
pp = PreProcessor(args.asm)

# The assembler organizes all sections and gets them ready for the linker
ap = Assembler(pp.execute())
ap.execute()

# I really don't like the way I did the linker.. The others take in a file
# and this should conform to that as method as well, but I got lazy. The
# Linker takes the sections, subroutines, data and then links everything
# together.
ln = Linker(ap, args.bin)
ln.execute()

# TODO: The jumps are all relative which will be a problem if the jumps
# are too far. The linker need to replace near relative jumps with far
# jumps to memory