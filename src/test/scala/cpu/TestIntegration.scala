package cpu

import chisel3._
import chisel3.util.{log2Ceil, MuxLookup}

class CPUIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val out  = Output(UInt(Instructions.WORD_SIZE.W))
}

class TestIntegration2 extends Module {
  val io = IO(new CPUIO)

  /* Modules */
  val control = Module(new Controller)
  val immgen  = Module(new ImmGen)
  val regs    = Module(new RegisterFile(
    Instructions.REGFILE_SIZE,
    Instructions.WORD_SIZE,
  ))
  val alu     = Module(new ALU)

  /* Control */
  control.io.ins := io.ins

  /* Immediate Generation */
  immgen.io.ins := io.ins
  immgen.io.ctl := control.io.imm

  /* Reg File Datapaths */
  regs.io.data   := alu.io.out
  regs.io.dst_en := MuxLookup(control.io.wb, false.B, Seq(
    Control.WB_REG -> true.B
  ))
  regs.io.dst    := io.ins(5,3)
  regs.io.addr0  := io.ins(8,6)
  regs.io.addr1  := io.ins(12,10)

  /* ALU Wire */
  alu.io.ctl  := control.io.alu
  alu.io.src0 := regs.io.out0
  alu.io.src1 := MuxLookup(control.io.src1, 1.U, Seq(
    Control.SRC1DP_REG -> regs.io.out1,
    Control.SRC1DP_IMM -> immgen.io.imm,
  ))

  /* Test word-sized outputs */
  io.out := alu.io.out
}