package cpu

import chisel3._
import chisel3.util.{MuxLookup, Cat}

class ALUIO extends Bundle {
  val ctl   = Input(UInt(Control.ALU_BITWIDTH))
  val src0  = Input(UInt(Instructions.WORD_SIZE.W))
  val src1  = Input(UInt(Instructions.WORD_SIZE.W))
  val out   = Output(UInt(Instructions.WORD_SIZE.W))
  val br    = Output(Bool())
}

class ALU extends Module {
  val io = IO(new ALUIO)

  /* Lookup the operation to execute */
  io.out := MuxLookup(io.ctl, 0.U, Seq(
    Control.ALU_ADD -> (io.src0 + io.src1),
    Control.ALU_SUB -> (io.src0 - io.src1),
    Control.ALU_AND -> (io.src0 & io.src1),
    Control.ALU_OR  -> (io.src0 | io.src1),
    Control.ALU_XOR -> (io.src0 ^ io.src1),
    Control.ALU_NOT -> (~io.src0),
    Control.ALU_SLL -> (io.src0 << io.src1),
    Control.ALU_SRL -> (io.src0 >> io.src1),
  ))

  /* Figure out whether or not to branch */
  io.br := MuxLookup(io.ctl, false.B, Seq(
    Control.ALU_EQ  -> (io.src0 === io.src1),
    Control.ALU_NE  -> (io.src0 =/= io.src1),
    Control.ALU_GE  -> (io.src0.asSInt >= io.src1.asSInt),
    Control.ALU_GEU -> (io.src0 >= io.src1),
    Control.ALU_LT  -> (io.src0.asSInt < io.src1.asSInt),
    Control.ALU_LTU -> (io.src0 < io.src1),
  ))
}