package cpu

import chisel3._
import chisel3.util.MuxLookup

class ALU(width: Int) extends Module {
  val io = IO(new Bundle {
    val ctr = Input(UInt(Control.ALU_BITWIDTH))
    val inA = Input(UInt(width.W))
    val inB = Input(UInt(width.W))
    val out = Output(UInt(width.W))
    val zero = Output(Bool())
    val carry = Output(Bool())
  })

  /* Lookup the operation to execute */
  val sum = MuxLookup(io.ctr, 0.U, Seq(
    Control.ALU_ADD -> (io.inA +& io.inB),
    Control.ALU_SUB -> (io.inA -& io.inB)
  ))

  /* Output Sum */
  io.out := sum

  /* Flags for carry/borrow and zero */
  io.carry := sum(width)
  io.zero := sum===0.U
}