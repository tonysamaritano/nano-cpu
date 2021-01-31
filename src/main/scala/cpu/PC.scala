package cpu

import chisel3._
import chisel3.util.{MuxLookup, log2Ceil}

class PCIO extends Bundle {
  val br     = Input(Bool())
  val ctl    = Input(UInt(Control.PC_BITWIDTH))
  val pc     = Input(UInt(Instructions.ARCH_SIZE.W))
  val imm    = Input(SInt(Instructions.WORD_SIZE.W))
  val src0   = Input(UInt(Instructions.WORD_SIZE.W))
  val pc_out = Output(UInt(Instructions.ARCH_SIZE.W))
  val pc_nxt = Output(UInt(Instructions.ARCH_SIZE.W))
}

class ProgramCounterCircuit extends Module {
  val io = IO(new PCIO)

  /* Always increment by the instruction size */
  def inc = (Instructions.INS_SIZE/8)

  /* Always multiply the immidiate by the instruction size */
  val shifted  = io.imm.asUInt << log2Ceil(inc)
  val pcNext   = io.pc + inc.U

  io.pc_out := MuxLookup(io.ctl, io.pc, Seq(
    Control.PC_INC  -> pcNext,
    Control.PC_BRI  -> Mux(io.br, (io.pc + shifted), pcNext),
    Control.PC_BRR  -> Mux(io.br, (io.src0 + shifted), pcNext),
  ))
  io.pc_nxt := pcNext
}