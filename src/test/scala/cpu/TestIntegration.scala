package cpu

import chisel3._
import chisel3.util.{log2Ceil, MuxLookup}

// class TestIntegration(width: Int, registers: Int) extends Module {
//   val io = IO(new Bundle {
//     val ins   = Input(UInt(width.W))
//     val data  = Input(UInt(width.W))
//     val addr  = Input(UInt(log2Ceil(registers).W))
//     val we    = Input(Bool())
//     val out   = Output(UInt(width.W))
//   })

//   /* Modules */
//   val control = Module(new ControlSignals(width))
//   val alu     = Module(new ALU(width))
//   val regs    = Module(new RegisterFile(registers, width))
//   val immgen  = Module(new ImmGen(width))

//   /* Decode Instruction */
//   control.io.ins := io.ins

//   /* Immidiate Generation */
//   immgen.io.ins := io.ins
//   immgen.io.ctr := control.io.imm

//   /* ALU Input Selection */
//   val src1Sel = Wire(UInt(width.W))
//   when (control.io.imm === Control.IMM_XXX) {
//     src1Sel := regs.io.out1
//   }.otherwise {
//     src1Sel := immgen.io.imm
//   }

//   when (io.we) {
//     /* This is just for initialization of the registers */
//     regs.io.dst     := io.addr
//     regs.io.dst_en  := true.B
//     regs.io.data    := io.data
//     regs.io.addr0   := 0.U
//     regs.io.addr1   := 0.U
//   }.otherwise {
//     /* Wire addresses to register file */
//     regs.io.addr0   := control.io.src0
//     regs.io.addr1   := control.io.src1
//     regs.io.dst     := control.io.dst
//     regs.io.dst_en  := control.io.reg
//     regs.io.data    := alu.io.out
//   }

//   /* Connect Control Signals and Inputs */
//   alu.io.ctr := control.io.alu
//   alu.io.inA := regs.io.out0
//   alu.io.inB := src1Sel

//   /* Temp */
//   io.out := alu.io.out
// }

class Instruction(ins: UInt, desc: String)
class InstructionValidation(ins: UInt, verf: UInt, desc: String) extends Instruction(ins, desc) {
  def desc() : String = desc
  def ins() : UInt = ins
  def verf() : UInt = verf
}

object TestPrograms {
  var program1 = Array(
    new InstructionValidation("b_0011_1110_0000_1000".U, "b_1111".U, "Adds x0 and imm (15) into x1"),
    new InstructionValidation("b_0000_0000_0100_0000".U, "b_1111".U, "Adds x0 and x1 into x0 to check output of x1"),
    new InstructionValidation("b_1010_1000_0100_1001".U, "b_1111_0000".U, "Shift x1 by 4 to increase the value by 2^4 and store back in x1"),
    new InstructionValidation("b_0010_0010_0101_0000".U, "b_1111_0001".U, "Add 1 to x1 and store in x2"),
    new InstructionValidation("b_0000_0000_1000_0000".U, "b_1111_0001".U, "Adds contents of x0 and x2 into x0 to check output of x2"),
    new InstructionValidation("b_0000_1000_0101_1000".U, "b1_1110_0001".U, "Add x1 and x2 and store in x3"),
    new InstructionValidation("b_0000_0000_1100_0000".U, "b1_1110_0001".U, "Adds x0 and x3 into x0 to check output of x3"),
    new InstructionValidation("b_1100_0000_0110_0001".U, "b_111_1000".U, "Divide x1 by 2 using a right shift and store in x4"),
    new InstructionValidation("b_0000_0001_0000_0000".U, "b_111_1000".U, "Adds x0 and x4 into x0 to check output of x4"),
    new InstructionValidation("b_0101_0000_1110_1000".U, "b_1_0110_1001".U, "Subtract x3 by x4 and store result in x5"),
    new InstructionValidation("b_0100_1101_0011_0000".U, "b_1111_1110_1001_0111".U, "Subtract x4 by x3 and store result in x6"),
    new InstructionValidation("b_0000_0001_1000_0000".U, "b_1111_1110_1001_0111".U, "Adds x0 and x6 into x0 to check output of x6"),
  )
}

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