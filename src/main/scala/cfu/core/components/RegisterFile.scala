package cfu.core

import cfu.config._

import chisel3._
import chisel3.util.log2Ceil

class RegisterIn extends Bundle {
  val data    = Input(UInt(Config.WORD_SIZE.W))
  val dst_en  = Input(Bool())
  val dst     = Input(UInt(log2Ceil(Config.REGFILE_SIZE).W))
  val addr0   = Input(UInt(log2Ceil(Config.REGFILE_SIZE).W))
  val addr1   = Input(UInt(log2Ceil(Config.REGFILE_SIZE).W))
}

class RegisterOut extends Bundle {
  val src0    = Output(UInt(Config.WORD_SIZE.W))
  val src1    = Output(UInt(Config.WORD_SIZE.W))
}

class RegisterFile extends Module {
  val io = IO(new Bundle {
    val in    = new RegisterIn
    val out   = new RegisterOut
  })

  val regs = Mem(
    Config.REGFILE_SIZE,
    UInt(Config.WORD_SIZE.W)
  )

  when (io.in.dst_en) {
    regs(io.in.dst) := io.in.data
  }

  io.out.src0 := Mux(io.in.addr0 > 0.U, regs(io.in.addr0), 0.U)
  io.out.src1 := Mux(io.in.addr1 > 0.U, regs(io.in.addr1), 0.U)
}