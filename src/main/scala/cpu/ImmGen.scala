package cpu

import chisel3._
import chisel3.util.{Cat, MuxLookup}

class ImmGen extends Module {
  val io = IO(new Bundle {
    val ctl = Input(UInt(Control.IMM_BITWIDTH))
    val ins = Input(UInt(Instructions.INS_SIZE.W))
    val imm = Output(SInt(Instructions.WORD_SIZE.W))
  })

  /* Because io.imm is signed, chisel will automatically sign extend the output of the
   * mux to preserve 2's compliment. IMM_UU is essentially zero extended and then
   * casted to an SInt. */
  io.imm := MuxLookup(io.ctl, io.ins(12,9).asSInt, Seq(
    Control.IMM_I5  -> Cat(io.ins(15), io.ins(12,9)).asSInt,
    Control.IMM_U   -> Cat(io.ins(8,6), io.ins(15), io.ins(12,9)).asSInt,
    Control.IMM_UU  -> Cat(0.U((Instructions.WORD_SIZE-8).W), io.ins(8,6), io.ins(15), io.ins(12,9)).asSInt,
    Control.IMM_S   -> Cat(io.ins(15), io.ins(5,3), io.ins(9)).asSInt,
    Control.IMM_B   -> Cat(io.ins(5,3), io.ins(8,6), io.ins(15), io.ins(12,9)).asSInt,
    Control.IMM_UUU -> Cat(io.ins(8,6), io.ins(15), io.ins(12,9), 0.U((Instructions.WORD_SIZE-8).W)).asSInt,
  ))
}