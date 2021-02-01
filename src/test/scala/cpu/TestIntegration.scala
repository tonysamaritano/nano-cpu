package cpu

import chisel3._
import chisel3.util.{log2Ceil, MuxLookup}

class Instruction(ins: UInt, desc: String)
class InstructionValidation(ins: UInt, out: UInt, desc: String) extends Instruction(ins, desc) {
  def desc() : String = desc
  def ins() : UInt = ins
  def out() : UInt = out
}

object TestPrograms {
  var program1 = Array(
    new InstructionValidation("b_0000_0000_0011_1101".U, 0.U, "Jumps to x0 and stores PC into x7"),
    new InstructionValidation("b_0010_1110_0000_1000".U, "b_0111".U, "Adds x0 and imm (7) into x1"),
    new InstructionValidation("b_1010_0010_0100_1001".U, "b_1110".U, "Shift x1 by 1 to increase the value by 2 and store back in x1"),
    new InstructionValidation("b_0010_0010_0100_1000".U, "b_1111".U, "Adds x1 and imm (1) into x1"),
    new InstructionValidation("b_0000_0000_0100_0000".U, "b_1111".U, "Adds x0 and x1 into x0 to check output of x1"),
    new InstructionValidation("b_1010_1000_0100_1001".U, "b_1111_0000".U, "Shift x1 by 4 to increase the value by 2^4 and store back in x1"),
    new InstructionValidation("b_0010_0010_0101_0000".U, "b_1111_0001".U, "Add 1 to x1 and store in x2"),
    new InstructionValidation("b_0000_0000_1000_0000".U, "b_1111_0001".U, "Adds contents of x0 and x2 into x0 to check output of x2"),
    new InstructionValidation("b_0000_1000_0101_1000".U, "b1_1110_0001".U, "Add x1 and x2 and store in x3"),
    new InstructionValidation("b_0000_0000_1100_0000".U, "b1_1110_0001".U, "Adds x0 and x3 into x0 to check output of x3"),
    new InstructionValidation("b_1110_0010_0110_0001".U, "b_111_1000".U, "Divide x1 by 2 using a right shift and store in x4"),
    new InstructionValidation("b_0000_0001_0000_0000".U, "b_111_1000".U, "Adds x0 and x4 into x0 to check output of x4"),
    new InstructionValidation("b_0101_0000_1110_1000".U, "b_1_0110_1001".U, "Subtract x3 by x4 and store result in x5"),
    new InstructionValidation("b_0100_1101_0011_0000".U, "b_1111_1110_1001_0111".U, "Subtract x4 by x3 and store result in x6"),
    new InstructionValidation("b_0000_0001_1000_0000".U, "b_1111_1110_1001_0111".U, "Adds x0 and x6 into x0 to check output of x6"),
    new InstructionValidation("b_0110_0001_1011_1001".U, "b_0000_0001_0110_1000".U, "Inverts x6 into x7"),
    new InstructionValidation("b_0001_1001_1000_0001".U, "b_1111_1110_1001_0111".U, "Ands x6 and x6 into x0 to check output of x6"),
    new InstructionValidation("b_0001_1101_1000_0001".U, 0.U, "Ands x6 and x6 into x0 to check output of x6"),
  )

  var program2 = Array(
    new InstructionValidation("b_0000_0000_0011_1101".U, 0.U, "Jumps to x0 and stores PC into x7"),
    new InstructionValidation("b_0010_1110_0000_1000".U, "b_0000_0000_0000_0111".U, "Adds x0 and imm (7) into x1"),
    new InstructionValidation("b_0011_0010_0001_0000".U, "b_1111_1111_1111_1001".U, "Adds x0 and imm (-7) into x2"),
    new InstructionValidation("b_0000_1000_0100_0000".U, 0.U, "Adds x1 and x2 into x0"),
    new InstructionValidation("b_0100_1000_0100_0000".U, 14.U, "Subtracts x1 and x2 into x0"),
  )

  var program3 = Array(
    new InstructionValidation("b_0000_0000_0011_1101".U, 0.U, "Jumps to x0 and stores PC into x7"),
    new InstructionValidation("b_0010_0110_0000_1000".U, "b_0000_0000_0000_0011".U, "Adds x0 and imm (3) into x1"),
    new InstructionValidation("b_0010_1110_0001_0000".U, "b_0000_0000_0000_0111".U, "Adds x0 and imm (7) into x2"),
    new InstructionValidation("b_0011_0010_0001_1000".U, "b_1111_1111_1111_1001".U, "Adds x0 and imm (-7) into x3"),
    new InstructionValidation("b_0000_0100_0100_0100".U, 0.U, "Checks if x1 == x1"),
    new InstructionValidation("b_0010_1000_0100_0100".U, 0.U, "Checks if x1 != x2"),
    new InstructionValidation("b_0100_0100_1000_0100".U, 0.U, "Checks if x2 > x1"),
    new InstructionValidation("b_0100_1000_0100_0100".U, 0.U, "Checks if x1 > x2"),
    new InstructionValidation("b_0100_1100_1000_0100".U, 0.U, "Checks if x2 > x3"),
    new InstructionValidation("b_0110_1100_1000_0100".U, 0.U, "Checks if x2 > x3 unsigned"),
    new InstructionValidation("b_0000_0100_0100_0100".U, 0.U, "Checks if x1 == x1"),
    new InstructionValidation("b_1000_0100_0100_0110".U, 0.U, "BR PC+100"),
    new InstructionValidation("b_0010_0100_0100_0100".U, 0.U, "Checks if x1 != x1"),
    new InstructionValidation("b_1000_0100_0100_0110".U, 0.U, "BR PC+100"),
    new InstructionValidation("b_0010_1000_1100_0101".U, 0.U, "Jumps to 200(PC) and stores PC into x0"),
    new InstructionValidation("b_0010_0001_0000_0101".U, 0.U, "Jumps to -256(PC) and stores PC into x0"),
  )

  var program4 = Array(
    new InstructionValidation("b_0011_1110_0000_1010".U, 15.U, "Load 30(x0) to x1"),
  )
}

class IntegrationIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val out  = Output(UInt(Instructions.WORD_SIZE.W))
  val flg  = Output(UInt(Instructions.WORD_SIZE.W))
}

class TestCore extends Module {
  val io = IO(new Bundle {
    val ins = Input(UInt(Instructions.INS_SIZE.W))
    val out = Output(UInt(Instructions.WORD_SIZE.W))
  })

  val pc   = RegInit(0.U(Instructions.ARCH_SIZE.W))
  val core = Module(new Core)

  core.io.ins := io.ins
  core.io.in.data := 0.U
  core.io.in.pc := pc

  pc := Mux(core.io.pc_sel, core.io.out.pc, pc + 2.U)

  printf(p"PC: $pc BR: ${core.io.pc_sel}\n")

  io.out := core.io.out.data
}