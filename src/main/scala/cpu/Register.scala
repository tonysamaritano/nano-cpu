package cpu

import chisel3._

class Register(width: Int) extends Module {
  val io = IO(new Bundle {
    val en  = Input(Bool())
    val in  = Input(UInt(width.W))
    val out = Output(UInt(width.W))
  })
  
  val register = RegInit(0.U(width.W))

  when (io.en) {
    register := io.in
    // io.out := io.in
  }.otherwise {
    // io.out := register
  }
  io.out := register
}