package cpu

import chisel3._
import chisel3.util.{Cat, MuxLookup}

class ImmGenIO extends Bundle {
  val ctl = Input(UInt(Control2.IMM_BITWIDTH))
  val ins = Input(UInt(Instructions2.INS_SIZE.W))
  val imm = Output(UInt(Instructions2.WORD_SIZE.W))
}

class ImmGen(width: Int) extends Module {
  val io = IO(new Bundle {
    val ctr = Input(UInt(2.W))
    val ins = Input(UInt(Instructions.INS_SIZE.W))
    val imm = Output(UInt(width.W))
  })

  when (io.ctr === Control.IMM_3BR2) {
    io.imm := Cat(0.U(13.W), io.ins(15,13))
  }.elsewhen (io.ctr === Control.IMM_8BR2) {
    io.imm := Cat(0.U(8.W), io.ins(15,8))
  }.otherwise {
    io.imm := 0.U
  }
}

class ImmGen2 extends Module {
  val io = IO(new ImmGenIO)

  /* Default case is Control2.IMM_RI. This means that both R type
   * and I type will output the same thing. That's OK for R type
   * because R type won't use the output from the ImmGen */
  io.imm := MuxLookup(io.ctl, io.ins(12,9), Seq(
    Control2.IMM_I5 -> Cat(io.ins(15), io.ins(12,9)),
    Control2.IMM_U  -> Cat(io.ins(8,6), io.ins(15), io.ins(12,9)),
    Control2.IMM_S  -> Cat(io.ins(15), io.ins(5,3), io.ins(9)),
  ))
}