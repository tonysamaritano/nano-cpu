package cpu

import chisel3._
import chisel3.util.BitPat

object Instructions {
    /* Arithmetic Operations */
    def ADD  = BitPat("b_????_????_?000_0000")
    def ADDI = BitPat("b_????_????_?000_1000")
    def SUB  = BitPat("b_????_????_?001_0000")
    def AND  = BitPat("b_????_????_?001_1000")
    def OR   = BitPat("b_????_????_?010_0000")
    def XOR  = BitPat("b_????_????_?010_1000")
    def NOT  = BitPat("b_????_????_?011_0000")

    /* Load/Store */
    def LDI  = BitPat("b_????_????_???0_0001")
    def LDIH = BitPat("b_????_????_???0_1001")
    def LDB  = BitPat("b_????_0???_???1_0001")
    def LDW  = BitPat("b_????_1???_???1_0001")
    def STB  = BitPat("b_????_0???_???1_1001")
    def STW  = BitPat("b_????_1???_???1_1001")

    /* Branch/Jump */
    def JMP  = BitPat("b_????_????_?000_0010")
    def JSR  = BitPat("b_????_????_?000_1010")
    def RSR  = BitPat("b_????_????_?001_0010")
    def BZ   = BitPat("b_????_????_?001_1010")
    def BC   = BitPat("b_????_????_?010_0010")

    /* CSR */
    def CLR = BitPat("b_????_????_?000_0011")
    def FLG = BitPat("b_????_????_?000_1011")
    def HLT = BitPat("b_????_????_?001_0011")

    /* Parameters */
    def REGFILE_SIZE     = 8
}