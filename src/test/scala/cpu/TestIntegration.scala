package cpu

import chisel3._
import chisel3.util.{log2Ceil, MuxLookup}

class Instruction(ins: UInt, desc: String)
class InstructionValidation(ins: UInt, flags: UInt, out: UInt, desc: String) extends Instruction(ins, desc) {
  def desc() : String = desc
  def ins() : UInt = ins
  def out() : UInt = out
  def flags() : UInt = flags
}

object TestPrograms {
  var program1 = Array(
    new InstructionValidation("b_0000_0000_0000_0101".U, 0.U,  0.U, "JALR PC to x0"),
    new InstructionValidation("b_0010_1110_0000_1000".U, 0.U, "b_0111".U, "Adds x0 and imm (7) into x1"),
    new InstructionValidation("b_1010_0010_0100_1001".U, 0.U, "b_1110".U, "Shift x1 by 1 to increase the value by 2 and store back in x1"),
    new InstructionValidation("b_0010_0010_0100_1000".U, 0.U, "b_1111".U, "Adds x1 and imm (1) into x1"),
    new InstructionValidation("b_0000_0000_0100_0000".U, 0.U, "b_1111".U, "Adds x0 and x1 into x0 to check output of x1"),
    new InstructionValidation("b_1010_1000_0100_1001".U, 0.U, "b_1111_0000".U, "Shift x1 by 4 to increase the value by 2^4 and store back in x1"),
    new InstructionValidation("b_0010_0010_0101_0000".U, 0.U, "b_1111_0001".U, "Add 1 to x1 and store in x2"),
    new InstructionValidation("b_0000_0000_1000_0000".U, 0.U, "b_1111_0001".U, "Adds contents of x0 and x2 into x0 to check output of x2"),
    new InstructionValidation("b_0000_1000_0101_1000".U, 0.U, "b1_1110_0001".U, "Add x1 and x2 and store in x3"),
    new InstructionValidation("b_0000_0000_1100_0000".U, 0.U, "b1_1110_0001".U, "Adds x0 and x3 into x0 to check output of x3"),
    new InstructionValidation("b_1110_0010_0110_0001".U, 0.U, "b_111_1000".U, "Divide x1 by 2 using a right shift and store in x4"),
    new InstructionValidation("b_0000_0001_0000_0000".U, 0.U, "b_111_1000".U, "Adds x0 and x4 into x0 to check output of x4"),
    new InstructionValidation("b_0101_0000_1110_1000".U, 0.U, "b_1_0110_1001".U, "Subtract x3 by x4 and store result in x5"),
    new InstructionValidation("b_0100_1101_0011_0000".U, 0.U, "b_1111_1110_1001_0111".U, "Subtract x4 by x3 and store result in x6"),
    new InstructionValidation("b_0000_0001_1000_0000".U, 0.U, "b_1111_1110_1001_0111".U, "Adds x0 and x6 into x0 to check output of x6"),
    new InstructionValidation("b_0110_0001_1011_1001".U, 0.U, "b_0000_0001_0110_1000".U, "Inverts x6 into x7"),
    new InstructionValidation("b_0001_1001_1000_0001".U, 0.U, "b_1111_1110_1001_0111".U, "Ands x6 and x6 into x0 to check output of x6"),
    new InstructionValidation("b_0001_1101_1000_0001".U, 0.U, 0.U, "Ands x6 and x6 into x0 to check output of x6"),
  )

  var program2 = Array(
    new InstructionValidation("b_0000_0000_0000_0101".U, 0.U,  0.U, "JALR PC to x0"),
    new InstructionValidation("b_0010_1110_0000_1000".U, 0.U, "b_0000_0000_0000_0111".U, "Adds x0 and imm (7) into x1"),
    new InstructionValidation("b_0011_0010_0001_0000".U, 0.U, "b_1111_1111_1111_1001".U, "Adds x0 and imm (-7) into x2"),
    new InstructionValidation("b_0000_1000_0100_0000".U, 0.U, 0.U, "Adds x1 and x2 into x0"),
    new InstructionValidation("b_0100_1000_0100_0000".U, 0.U, 14.U, "Subtracts x1 and x2 into x0"),
  )

  var program3 = Array(
    new InstructionValidation("b_0000_0000_0000_0101".U, 0.U,  0.U, "JALR PC to x0"),
    new InstructionValidation("b_0010_0110_0000_1000".U, 0.U, "b_0000_0000_0000_0011".U, "Adds x0 and imm (3) into x1"),
    new InstructionValidation("b_0010_1110_0001_0000".U, 0.U, "b_0000_0000_0000_0111".U, "Adds x0 and imm (7) into x2"),
    new InstructionValidation("b_0011_0010_0001_1000".U, 0.U, "b_1111_1111_1111_1001".U, "Adds x0 and imm (-7) into x3"),
    new InstructionValidation("b_0000_0100_0100_0100".U, 1.U,  0.U, "Checks if x1 == x1"),
    new InstructionValidation("b_0010_1000_0100_0100".U, 1.U,  0.U, "Checks if x1 != x2"),
    new InstructionValidation("b_0100_0100_1000_0100".U, 1.U,  0.U, "Checks if x2 > x1"),
    new InstructionValidation("b_0100_1000_0100_0100".U, 0.U,  0.U, "Checks if x1 > x2"),
    new InstructionValidation("b_0100_1100_1000_0100".U, 1.U,  0.U, "Checks if x2 > x3"),
    new InstructionValidation("b_0110_1100_1000_0100".U, 0.U,  0.U, "Checks if x2 > x3 unsigned"),
    new InstructionValidation("b_0010_1100_0000_0101".U, 0.U,  0.U, "JAL PC+12 into x4"),
    new InstructionValidation("b_0000_0100_0100_0100".U, 1.U,  0.U, "Checks if x1 == x1"),
    new InstructionValidation("b_1000_0100_0100_0110".U, 0.U,  0.U, "BEQ PC+100"),
    new InstructionValidation("b_0010_0100_0100_0100".U, 0.U,  0.U, "Checks if x1 != x1"),
    new InstructionValidation("b_1000_0100_0100_0110".U, 0.U,  0.U, "BEQ PC+100"),
  )

  var program4 = Array(
    new InstructionValidation("b_0000_0000_0000_0101".U, 0.U,  0.U, "JALR PC to x0"),
    new InstructionValidation("b_0011_1110_0000_1010".U, 0.U,  15.U, "Load 30(x0) to x1"),
  )
}

class IntegrationIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val out  = Output(UInt(Instructions.WORD_SIZE.W))
  val flg  = Output(UInt(Instructions.WORD_SIZE.W))
}

class TestIntegration2 extends Module {
  val io = IO(new IntegrationIO)

  /********************* Modules *********************/

  val control = Module(new Controller)
  val immgen  = Module(new ImmGen)
  val regs    = Module(new RegisterFile)
  val alu     = Module(new ALU)
  val pcCtl   = Module(new ProgramCounterCircuit)

  /******************** Registers ********************/

  val pc         = RegInit(0.U(Instructions.ARCH_SIZE.W))
  val ins_prev   = RegInit(0.U(Instructions.ARCH_SIZE.W))
  val flags      = RegInit(0.U(Instructions.ARCH_SIZE.W))
  val cycleCount = RegInit(0.U(64.W))
  val insCount   = RegInit(0.U(64.W))

  /**************** Internal Counters ****************/

  /* Increment Cycles */
  cycleCount := cycleCount + 1.U
  when (control.io.ctl.pc =/= Control.PC_STALL) {
    insCount := insCount + 1.U
  }

  /******************** Controller ********************/

  /* Store previous instruction for two cycle instructions */
  ins_prev       := io.ins

  /* Wire the instruction into the controller */
  control.io.ins := io.ins

  /************************ PC ************************/

  /* Jump and Link: Use the current branch flag (JAL opcode == 5)
   * Branch: Use the previous branch flag */
  pcCtl.io.br     := Mux(io.ins(2,0) === 5.U, true.B, flags(0))

  /* Rest of the PC inputs */
  pcCtl.io.pc     := pc
  pcCtl.io.imm    := immgen.io.imm
  pcCtl.io.src0   := regs.io.out.src0
  pcCtl.io.ctl    := control.io.ctl.pc

  /* Stores the PC output next cycle */
  pc := pcCtl.io.pc_out

  /*************** Immediate Generation ***************/

  immgen.io.ins := io.ins
  immgen.io.ctl := control.io.ctl.imm

  /****************** Register File *******************/

  /* If we are in JAL insructions, the PC must be stored, otherwise the
   * ALU output is connected. */
  regs.io.data   := Mux(io.ins(2,0) === 5.U, pcCtl.io.pc_nxt, alu.io.data.out)

  /* Controls when a WB to the register file occurs */
  regs.io.dst_en := MuxLookup(control.io.ctl.wb, false.B, Seq(
    Control.WB_REG -> true.B
  ))

  /* Reg File Datapaths */
  regs.io.dst    := io.ins(5,3)
  regs.io.addr0  := io.ins(8,6)
  regs.io.addr1  := io.ins(12,10)

  /*********************** ALU ************************/

  alu.io.ctl  := control.io.ctl.alu
  alu.io.src0 := regs.io.out.src0
  alu.io.src1 := MuxLookup(control.io.ctl.src1, 0.U, Seq(
    Control.SRC1DP_REG -> regs.io.out.src1,
    Control.SRC1DP_IMM -> immgen.io.imm.asUInt,
  ))

  /********************** Flags ***********************/

  flags := alu.io.data.br /* There's only one flag for now */

  /* Test word-sized outputs */
  io.out := alu.io.data.out
  io.flg := alu.io.data.br

  /* What address are we accessing next? */

  val addrR = MuxLookup(control.io.ctl.ld, pc, Seq(
    Control.LD_ADDR -> (alu.io.data.out << 1), /* Multiply by two to get the right addr */
  ))

  printf(p"addrR: $addrR\n")

}