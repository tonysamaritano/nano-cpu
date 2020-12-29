package cpu

import chisel3._
import chisel3.util.log2Ceil

class RegisterFile(size: Int, width: Int) extends Module {
  val io = IO(new Bundle {
    val data    = Input(UInt(width.W))
    val dst_en  = Input(Bool())
    val dst     = Input(UInt(log2Ceil(size).W))
    val addr0   = Input(UInt(log2Ceil(size).W))
    val addr1   = Input(UInt(log2Ceil(size).W))
    val out0    = Output(UInt(width.W))
    val out1    = Output(UInt(width.W))
  })

  val regs = Mem(size, UInt(width.W))

  when (io.dst_en) {
    regs(io.dst) := io.data
  }

  io.out0 := Mux(io.addr0 > 0.U, regs(io.addr0), 0.U)
  io.out1 := Mux(io.addr1 > 0.U, regs(io.addr1), 0.U)
}