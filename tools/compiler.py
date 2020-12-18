import sys

class Ins():
    ins = {
        "NOP": 0,
        "LDA": 1,
        "ADD": 2,
        "SUB": 3,
        "STA": 4,
        "LDI": 5,
        "JMP": 6,
        "JC": 7,
        "JZ": 8,
        "OUT": 14,
        "HLT": 15
    }
    def __init__(self, line):
        self._operation = line.split()

    def toBytes(self):
        res = 0x00
        res = res | (Ins.ins[self._operation[0]]<<4)

        if len(self._operation) == 2:
            if '0x' in self._operation[1]:
                res = res | int(self._operation[1], 16)
            else:
                res = res | int(self._operation[1])

        return bytes([res])

    def __str__(self):
        return str(self._operation)

asm = open(sys.argv[1], "r")
output = open(f"{sys.argv[1]}.bin", "wb")

for line in asm:
    # Strip Comments
    ins = Ins(line.split("//")[0].rstrip())
    output.write(ins.toBytes())

output.close()



