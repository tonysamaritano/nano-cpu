package cpu

import chisel3._
import chisel3.util.{Cat, MuxLookup}

class ImmGenIO extends Bundle {
  val ctl = Input(UInt(Control.IMM_BITWIDTH))
  val ins = Input(UInt(Instructions.INS_SIZE.W))
  val imm = Output(UInt(Instructions.WORD_SIZE.W))
}

class ImmGen extends Module {
  val io = IO(new ImmGenIO)

  /* Default case is Control.IMM_RI. This means that both R type
   * and I type will output the same thing. That's OK for R type
   * because R type won't use the output from the ImmGen */
  io.imm := MuxLookup(io.ctl, io.ins(12,9), Seq(
    Control.IMM_I5 -> Cat(io.ins(15), io.ins(12,9)),
    Control.IMM_U  -> Cat(io.ins(8,6), io.ins(15), io.ins(12,9)),
    Control.IMM_S  -> Cat(io.ins(15), io.ins(5,3), io.ins(9)),
  ))
}