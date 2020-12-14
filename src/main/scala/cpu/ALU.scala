package cpu

import chisel3._
import chisel3.util._

class ALU(width: Int) extends Module {
  val io = IO(new Bundle {
    val sel     = Input(Bool())
    val inA     = Input(UInt(width.W))
    val inB     = Input(UInt(width.W))
    val out     = Output(UInt(width.W))
  })
  
  when (io.sel) {
    io.out := io.inA + io.inB
  }.otherwise {
    io.out := io.inA - io.inB
  }
}