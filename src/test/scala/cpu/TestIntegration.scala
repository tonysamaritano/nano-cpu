package cpu

import chisel3._
import chisel3.util.log2Ceil

class TestIntegration(width: Int, registers: Int) extends Module {
  val io = IO(new Bundle {
    val ins   = Input(UInt(width.W))
    val data  = Input(UInt(width.W))
    val addr  = Input(UInt(log2Ceil(registers).W))
    val we    = Input(Bool())
    val out   = Output(UInt(width.W))
  })

  /* Modules */
  val control = Module(new ControlSignals(width))
  val alu     = Module(new ALU(width))
  val regs    = Module(new RegisterFile(registers, width))
  val immgen  = Module(new ImmGen(width))

  /* Decode Instruction */
  control.io.ins := io.ins

  /* Immidiate Generation */
  immgen.io.ins := io.ins
  immgen.io.ctr := control.io.imm

  /* ALU Input Selection */
  val src1Sel = Wire(UInt(width.W))
  when (control.io.imm === Control.IMM_XXX) {
    src1Sel := regs.io.out1
  }.otherwise {
    src1Sel := immgen.io.imm
  }

  when (io.we) {
    /* This is just for initialization of the registers */
    regs.io.dst     := io.addr
    regs.io.dst_en  := true.B
    regs.io.data    := io.data
    regs.io.addr0   := 0.U
    regs.io.addr1   := 0.U
  }.otherwise {
    /* Wire addresses to register file */
    regs.io.addr0   := control.io.src0
    regs.io.addr1   := control.io.src1
    regs.io.dst     := control.io.dst
    regs.io.dst_en  := control.io.reg
    regs.io.data    := alu.io.out
  }

  /* Connect Control Signals and Inputs */
  alu.io.ctr := control.io.alu
  alu.io.inA := regs.io.out0
  alu.io.inB := src1Sel

  /* Temp */
  io.out := alu.io.out
}