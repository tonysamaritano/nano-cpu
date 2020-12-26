package cpu

import chisel3._

class TestALU(width: Int) extends Module {
  val io = IO(new Bundle {
    val ins = Input(UInt(width.W))
    val inA = Input(UInt(width.W))
    val inB = Input(UInt(width.W))
    val out = Output(UInt(width.W))
    val zero = Output(Bool())
    val carry = Output(Bool())
  })

  /* Control Module */
  val control = Module(new ControlSignals(width))
  val alu = Module(new ALU(width))

  /* Decode Instruction */
  control.io.ins := io.ins

  /* Connect Control Signals and Inputs */
  alu.io.ctr := control.io.alu
  alu.io.inA := io.inA
  alu.io.inB := io.inB

  /* Temp */
  io.out := alu.io.out
  io.carry := alu.io.carry
  io.zero := alu.io.zero
}