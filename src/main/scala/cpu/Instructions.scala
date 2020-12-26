package cpu

import chisel3._
import chisel3.util.BitPat

object Instructions {
    /* Arithmetic Operations */
    def ADD  = BitPat("b_0000_0???")
    def SUB  = BitPat("b_0000_1???")

    /* Load/Store */
    def LDA  = BitPat("b_0001_????")
    def LDAH = BitPat("b_0010_????")
    def LDB  = BitPat("b_0011_????")
    def LDBH = BitPat("b_0100_????")
    def LDI  = BitPat("b_0101_????")
    def LDIH = BitPat("b_0110_????")
    def STA  = BitPat("b_0111_????")

    /* Branch/Jump */
    def JMP = BitPat("b_1000_????")
    def JSR = BitPat("b_1001_0???")
    def RSR = BitPat("b_1001_1???")
    def BZ  = BitPat("b_1010_????")

    /* CSR */
    def CLR = BitPat("b_1011_0???")
    def FLG = BitPat("b_1011_1???")
    def HLT = BitPat("b_1111_????")
}