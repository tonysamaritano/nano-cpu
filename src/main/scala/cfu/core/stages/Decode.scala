package cfu.core

import cfu.config._

import chisel3._

class DecodeIn extends Bundle {
  val ins  = Input(UInt(Config.INS_SIZE.W))
  val data = Input(UInt(Config.WORD_SIZE.W))
}

class DecodeOut extends Bundle {
  val ctl  = new ControlSignals
  val src  = new RegisterOut
  val imm  = Output(SInt(Config.WORD_SIZE.W))
  val halt = Output(Bool())
}

class DecodeIO extends Bundle {
  val in   = new DecodeIn
  val out  = new DecodeOut
}

class Decode extends Module {
  val io = IO(new DecodeIO)

  /* Decode Modules */
  val ctl    = Module(new Controller)
  val immgen = Module(new ImmGen)
  val regs   = Module(new RegisterFile)

  /* Decode Instruction */
  ctl.io.ins := io.in.ins

  /* Generate Immediate Values */
  immgen.io.ins := io.in.ins
  immgen.io.ctl := ctl.io.ctl.imm

  /* Control Registers */
  regs.io.in.data   := io.in.data
  regs.io.in.dst_en := ctl.io.ctl.wb.orR
  regs.io.in.dst    := io.in.ins(5,3)
  regs.io.in.addr0  := io.in.ins(8,6)
  regs.io.in.addr1  := io.in.ins(12,10)

  /* Wire Outputs */
  io.out.ctl  <> ctl.io.ctl
  io.out.src  <> regs.io.out
  io.out.imm  := immgen.io.imm
  io.out.halt := ctl.io.ctl.halt
}
