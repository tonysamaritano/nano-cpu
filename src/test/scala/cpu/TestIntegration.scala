package cpu

import chisel3._
import chisel3.util.{log2Ceil, MuxLookup}

class Instruction(ins: UInt, desc: String)
class InstructionValidation(ins: UInt, out: UInt, desc: String) extends Instruction(ins, desc) {
  def desc() : String = desc
  def ins() : UInt = ins
  def out() : UInt = out
}

object CoreTestPrograms {
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
    new InstructionValidation("b_1001_1001_1101_1110".U, 0.U, "BR PC+2040"),
    new InstructionValidation("b_0010_0100_0100_0100".U, 0.U, "Checks if x1 != x1"),
    new InstructionValidation("b_1000_0100_0100_0110".U, 0.U, "BR PC+100"),
    new InstructionValidation("b_0010_1000_1100_0101".U, 0.U, "Jumps to 200(PC) and stores PC into x0"),
    new InstructionValidation("b_0010_0001_0000_0101".U, 0.U, "Jumps to -256(PC) and stores PC into x0"),
  )

  var program4 = Array(
    new InstructionValidation("b_0000_0000_0000_0101".U, 0.U, "Jumps to x0 and stores PC into x0"),
    new InstructionValidation("b_1101_1111_1100_0010".U, 0.U, "Load Lower Imm 255"),
    new InstructionValidation("b_1111_1111_1100_1010".U, 0.U, "Load Upper Imm 255 and store in x1"),
    new InstructionValidation("b_0000_0000_0100_0000".U, 65535.U, "Adds x0 and x1 into x0 to check output of x1"),
    new InstructionValidation("b_1101_1111_1100_0010".U, 0.U, "Load Lower Imm 255"),
    new InstructionValidation("b_1111_1110_1100_1010".U, 0.U, "Load Upper Imm 127 and store in x1"),
    new InstructionValidation("b_0000_0000_0100_0000".U, 32767.U, "Adds x0 and x1 into x0 to check output of x1"),
  )

  var program5 = Array(
    new InstructionValidation("b_0000_0000_0000_0101".U, 0.U, "Jumps to x0 and stores PC into x0"),
    new InstructionValidation("b_1101_1111_1100_0010".U, 0.U, "Load Lower Imm 255"),
    new InstructionValidation("b_1111_1111_1100_1010".U, 0.U, "Load Upper Imm 255 and store in x1"),
    new InstructionValidation("b_0000_0000_0100_0000".U, 65535.U, "Adds x0 and x1 into x0 to check output of x1"),
    new InstructionValidation("b_1100_0101_1000_0010".U, 0.U, "Load Lower to get Imm 1234"),
    new InstructionValidation("b_0110_1000_0001_0010".U, 0.U, "Load Upper to get Imm 1234 into x2"),
    new InstructionValidation("b_0000_0000_1000_0000".U, 1234.U, "Adds x0 and x2 into x0 to check output of x2"),
    new InstructionValidation("b_0010_0100_1000_1011".U, 1236.U, "Store x1 in 2(x2)"),
  )

  var program6 = Array(
    new InstructionValidation("b_0000_0000_0000_0101".U, 0.U, "Jumps to x0 and stores PC into x0"),
    new InstructionValidation("b_1100_0101_1000_0010".U, 0.U, "Load Lower to get Imm 1234"),
    new InstructionValidation("b_0110_1000_0000_1010".U, 0.U, "Load Upper to get Imm 1234 into x1"),
    new InstructionValidation("b_0011_1110_0101_0010".U, (1234+15).U, "Loads data from 15(x1) into x2"),
    new InstructionValidation("b_0000_0000_0000_0000".U, (1234+15).U, "NOP for bubble"),
    new InstructionValidation("b_0000_0000_1000_0000".U, 54321.U, "Adds x0 and x1 into x0 to check output of x1"),
  )
}

object Programs {
  var program_42 = Array(
    new InstructionValidation("b_0010_1110_0000_1000".U, 0.U, "Adds x0 and imm (7) into x1"),
    new InstructionValidation("b_1010_0100_0100_1001".U, 0.U, "Shift x1 by 2 to multiply by 4 and store back in x1"),
    new InstructionValidation("b_0010_1110_0001_0000".U, 0.U, "Adds x0 and imm (7) into x2"),
    new InstructionValidation("b_1010_0010_1001_0001".U, 0.U, "Shift x2 by 1 to multiply by 2 and store back in x2"),
    new InstructionValidation("b_0000_1000_0101_1000".U, 0.U, "Adds x1 and x2 into x3"),
    new InstructionValidation("b_0010_1100_0000_0011".U, 0.U, "Store x3 in 0(x0)"),
  )

  var program_soft_mul = Array(
    new InstructionValidation("b_0010_1110_0000_1000".U, 0.U, "Adds x0 and imm (7) into x1"),
    new InstructionValidation("b_0010_1100_0001_0000".U, 0.U, "Adds x0 and imm (6) into x2"),
    new InstructionValidation("b_1010_1010_1001_0001".U, 0.U, "Shift x2 by 5 to multiply by 32 and store back in x2"),
    new InstructionValidation("b_0000_0000_0001_1000".U, 0.U, "Adds x0 and imm (0) into x3"),
    new InstructionValidation("b_0011_1110_1001_0000".U, 0.U, "Adds x2 and imm (-1) into x2"),
    new InstructionValidation("b_0000_1100_1001_1000".U, 0.U, "Adds x2 and x3 into x3"),
    new InstructionValidation("b_0011_1110_0100_1000".U, 0.U, "Adds x1 and imm (-1) into x1"),
    new InstructionValidation("b_0010_0000_0100_0100".U, 0.U, "Checks if x1 != x0"),
    new InstructionValidation("b_1001_1011_1111_1011".U, 0.U, "BR PC-6"),
    new InstructionValidation("b_0010_1100_0000_0011".U, 0.U, "Store x3 in 0(x0)"),
  )
}

class TestCore extends Module {
  val io = IO(new Bundle {
    val ins = Input(UInt(Instructions.INS_SIZE.W))
    val out = Flipped(new CoreOut)
  })

  val pc   = RegInit(0.U(Instructions.ARCH_SIZE.W))
  val core = Module(new Core)

  core.io.ins := io.ins
  core.io.in.data := 54321.U
  core.io.in.pc := pc

  /* If we've loaded, we want to restore the previous PC because it
   * takes 2 cycles to load (von Neumann bottleneck) */
  pc := Mux(core.io.out.pc_sel, core.io.out.pc,
    Mux(core.io.out.ld_en,  pc, pc + 2.U))

  /* Output of the ALU is the memory address */
  io.out <> core.io.out
}

class TestProcessor extends Module {
  val io = IO(new Bundle {
    /* For program loading */
    val mem = new MagicMemoryIn
  })

  val cpu = Module(new Processor)
  val mem = Module(new MagicMemory(1024))

  cpu.io.mem <> mem.io

  /* Print when the processor writes to memory */
  when (cpu.io.mem.write.we) {
    printf(p"Mem[${cpu.io.mem.write.addr}] = ${cpu.io.mem.write.data}\n")
  }

  /* Reset the processor while loading program into memory */
  cpu.io.reset := io.mem.we

  /* Program loading */
  when (io.mem.we) {
    mem.io.write <> io.mem
  }
}