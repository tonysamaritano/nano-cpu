package cpu

import chisel3._
import chisel3.util.{Cat}

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