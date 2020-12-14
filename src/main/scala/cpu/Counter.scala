package cpu

import chisel3._

class Counter(width: Int) extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val set = Input(Bool())
    val in  = Input(UInt(width.W))
    val out = Output(UInt(width.W))
  })
  
  val register = RegInit(0.U(width.W))

  when (io.set) {
    register := io.in
  }.elsewhen (io.en) {
    register := register + 1.U
  }
  io.out := register
}