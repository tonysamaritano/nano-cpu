package cfu.core

import cfu.config._

import chisel3._

class ExecuteIn extends Bundle {
  val src     = Flipped(new RegisterOut)
  val ctl     = Flipped(new ControlSignals)
  val imm     = Input(SInt(Config.WORD_SIZE.W))
  val pc      = Input(UInt(Config.ARCH_SIZE.W))
}

class ExecuteOut extends Bundle {
  val pc      = Output(UInt(Config.ARCH_SIZE.W))
  val alu     = new ALUOut
}

class ExecuteIO extends Bundle {
  val in      = new ExecuteIn
  val out     = new ExecuteOut
}

class Execute extends Module {
  val io = IO(new ExecuteIO)

  /* Execute Modules */
  val alu      = Module(new ALU)

  /* Wire ALU */
  alu.io.in.ctl  := io.in.ctl.alu
  alu.io.in.src0 := io.in.src.src0
  alu.io.in.src1 := Mux(io.in.ctl.src1.asBool, io.in.imm.asUInt, io.in.src.src1)
  io.out.alu <> alu.io.out

  /* Update PC based on branch value.
   *
   * Note: Multiply Imm by 2 because PC can never be an odd number */
  val immShift = (io.in.imm.asUInt << 1)
  io.out.pc := Mux(io.in.ctl.br.asBool,
    io.in.pc + immShift,
    io.in.src.src0 + immShift
  )
}