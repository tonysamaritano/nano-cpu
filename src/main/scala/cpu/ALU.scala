package cpu

import chisel3._
import chisel3.util.{MuxLookup, Cat}

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

class ALUIO extends Bundle {
  val ctl   = Input(UInt(Control2.ALU_BITWIDTH))
  val src0  = Input(UInt(Instructions2.WORD_SIZE.W))
  val src1  = Input(UInt(Instructions2.WORD_SIZE.W))
  val out   = Output(UInt(Instructions2.WORD_SIZE.W))
  val flags = Output(UInt(Control2.ALU_FLAGS))
}

class ALU2 extends Module {
  val io = IO(new ALUIO)

  /* Lookup the operation to execute */
  val out = MuxLookup(io.ctl, 0.U, Seq(
    Control2.ALU_ADD -> (io.src0 +& io.src1),
    Control2.ALU_SUB -> (io.src0 -& io.src1),
    Control2.ALU_AND -> (io.src0 & io.src1),
    Control2.ALU_OR  -> (io.src0 | io.src1),
    Control2.ALU_XOR -> (io.src0 ^ io.src1),
    Control2.ALU_NOT -> (~io.src0),
    Control2.ALU_SLL -> (io.src0 << io.src1),
    Control2.ALU_SRL -> (io.src0 >> io.src1),
  ))

  val eq = io.src0 === io.src1
  val gt = MuxLookup(io.ctl, 0.U, Seq(
    Control2.ALU_GE  -> (io.src0.asSInt > io.src1.asSInt),
    Control2.ALU_GEU -> (io.src0 > io.src1),
  ))

  /* Output Word */
  io.out := out
  io.flags := Cat(gt, eq)
}