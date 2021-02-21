package cfu.core

import cfu.config._

import chisel3._
import chisel3.util.{MuxLookup, Cat}

class ALUIn extends Bundle {
  val ctl   = Input(UInt(Control.ALU_BITWIDTH))
  val src0  = Input(UInt(Config.WORD_SIZE.W))
  val src1  = Input(UInt(Config.WORD_SIZE.W))
}

class ALUOut extends Bundle {
  val data  = Output(UInt(Config.WORD_SIZE.W))
  val br    = Output(Bool())
}

class ALU extends Module {
  val io = IO(new Bundle {
    val in  = new ALUIn
    val out = new ALUOut
  })

  val shift = io.in.src1(3,0).asUInt

  /* Lookup the operation to execute */
  io.out.data := MuxLookup(io.in.ctl, 0.U, Seq(
    Control.ALU_ADD -> (io.in.src0 + io.in.src1),
    Control.ALU_SUB -> (io.in.src0 - io.in.src1),
    Control.ALU_AND -> (io.in.src0 & io.in.src1),
    Control.ALU_OR  -> (io.in.src0 | io.in.src1),
    Control.ALU_XOR -> (io.in.src0 ^ io.in.src1),
    Control.ALU_NOT -> (~io.in.src0),
    Control.ALU_SLL -> (io.in.src0 << shift),
    Control.ALU_SRL -> (io.in.src0 >> shift),
  ))

  /* Figure out whether or not to branch */
  io.out.br := MuxLookup(io.in.ctl, false.B, Seq(
    Control.ALU_EQ  -> (io.in.src0 === io.in.src1),
    Control.ALU_NE  -> (io.in.src0 =/= io.in.src1),
    Control.ALU_GE  -> (io.in.src0.asSInt >= io.in.src1.asSInt),
    Control.ALU_GEU -> (io.in.src0 >= io.in.src1),
    Control.ALU_LT  -> (io.in.src0.asSInt < io.in.src1.asSInt),
    Control.ALU_LTU -> (io.in.src0 < io.in.src1),
  ))
}