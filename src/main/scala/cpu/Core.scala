package cpu

import chisel3._

class CoreIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val data = Output(UInt(Instructions.WORD_SIZE.W))
  val ctl  = Output(new Control)
}

class DecodeIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val data = Input(UInt(Instructions.WORD_SIZE.W))
  val ctl  = new Control
  val src  = new RegisterData
  val imm  = Output(SInt(Instructions.WORD_SIZE.W))
}

class ExecuteIO extends Bundle {
  val src = Flipped(new RegisterData)
  val ctl = Flipped(new Control)
  val imm = Input(SInt(Instructions.WORD_SIZE.W))
  val alu = new ALUData
}

class Decode extends Module {
  val io = IO(new DecodeIO)

  val ctl    = Module(new Controller)
  val immgen = Module(new ImmGen)
  val regs   = Module(new RegisterFile)

  ctl.io.ins := io.ins

  immgen.io.ins := io.ins
  immgen.io.ctl := ctl.io.ctl.imm

  regs.io.data   := io.data
  regs.io.dst_en := ctl.io.ctl.wb
  regs.io.dst    := io.ins(5,3)
  regs.io.addr0  := io.ins(8,6)
  regs.io.addr1  := io.ins(12,10)

  io.ctl <> ctl.io.ctl
  io.src <> regs.io.out
  io.imm := immgen.io.imm
}

class Execute extends Module {
  val io = IO(new ExecuteIO)

  /* Implement Execute */
}

// class Core extends Module {
//   val io = new CoreIO
// }