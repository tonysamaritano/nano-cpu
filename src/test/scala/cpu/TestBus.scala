package cpu

import chisel3._
import chisel3.util.{MuxLookup, Decoupled}

class TestBus(width: Int) extends Module {
  val io = IO(new Bundle {
    val sel = Input(UInt(2.W))
    val inA = Input(UInt(width.W))
    val inB = Input(UInt(width.W))
    val inC = Input(UInt(width.W))
    val out = UInt(width.W)
  })

  /* This is just a mux. I'd probably want to make an abstract
   * interface for the output of the bus so that the right
   * component is latching the bus. */
  io.out := MuxLookup(io.sel, 0.U, Seq(
    1.U -> (io.inA),
    2.U -> (io.inB),
    3.U -> (io.inC),
  ))
}