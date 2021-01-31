package cpu

import chisel3._
import chisel3.util.{ListLookup}

import Instructions._

object Control {
  /* ALU Control */
  def ALU_BITWIDTH = 4.W
  def ALU_ZERO = 0.U(ALU_BITWIDTH)
  def ALU_ADD  = 1.U(ALU_BITWIDTH)
  def ALU_SUB  = 2.U(ALU_BITWIDTH)
  def ALU_AND  = 3.U(ALU_BITWIDTH)
  def ALU_OR   = 4.U(ALU_BITWIDTH)
  def ALU_XOR  = 5.U(ALU_BITWIDTH)
  def ALU_NOT  = 6.U(ALU_BITWIDTH)
  def ALU_SLL  = 7.U(ALU_BITWIDTH)
  def ALU_SRL  = 8.U(ALU_BITWIDTH)
  def ALU_EQ   = 9.U(ALU_BITWIDTH)
  def ALU_NE   = 10.U(ALU_BITWIDTH)
  def ALU_GE   = 11.U(ALU_BITWIDTH)
  def ALU_GEU  = 12.U(ALU_BITWIDTH)
  def ALU_LT   = 13.U(ALU_BITWIDTH)
  def ALU_LTU  = 14.U(ALU_BITWIDTH)

  /* Imm Control */
  def IMM_BITWIDTH = 3.W
  def IMM_RI      = 0.U(IMM_BITWIDTH)
  def IMM_I5      = 1.U(IMM_BITWIDTH)
  def IMM_U       = 2.U(IMM_BITWIDTH)
  def IMM_UU      = 3.U(IMM_BITWIDTH)
  def IMM_S       = 4.U(IMM_BITWIDTH)

  /* Source 1 Datapath */
  def SRC1DP_BITWIDTH = 1.W
  def SRC1DP_REG      = 0.U(SRC1DP_BITWIDTH)
  def SRC1DP_IMM      = 1.U(SRC1DP_BITWIDTH)

  /* Write Control */
  def WB_BITWIDTH     = 2.W
  def WB_XXX          = 0.U(WB_BITWIDTH)
  def WB_REG          = 1.U(WB_BITWIDTH)

  /* PC Control */
  def PC_BITWIDTH     = 2.W
  def PC_STALL        = 0.U(PC_BITWIDTH)
  def PC_INC          = 1.U(PC_BITWIDTH)
  def PC_BRI          = 2.U(PC_BITWIDTH)
  def PC_BRR          = 3.U(PC_BITWIDTH)

  /* Branch Control */
  def BR_BITWIDTH     = 3.W
  def BR_J            = 0.U(BR_BITWIDTH)
  def BR_EQ           = 1.U(BR_BITWIDTH)
  def BR_NE           = 2.U(BR_BITWIDTH)
  def BR_GE           = 3.U(BR_BITWIDTH)
  def BR_LT           = 4.U(BR_BITWIDTH)

  /* Default Control Signal */
  val default = List(IMM_RI, SRC1DP_REG, WB_XXX, ALU_ADD, PC_STALL, BR_EQ)

  /* Control Signal Lookup */
  /*               Imm Gen, Src1,       Write,  ALU Ctl, PC Ctl, Branch Ctl */
  val map = Array(
    ADD  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_ADD,  PC_INC, BR_EQ),
    ADDI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_ADD,  PC_INC, BR_EQ),
    SUB  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_SUB,  PC_INC, BR_EQ),

    AND  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_AND,  PC_INC, BR_EQ),
    OR   -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_OR,   PC_INC, BR_EQ),
    XOR  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_XOR,  PC_INC, BR_EQ),
    NOT  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_NOT,  PC_INC, BR_EQ),
    SLL  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_SLL,  PC_INC, BR_EQ),
    SLLI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_SLL,  PC_INC, BR_EQ),
    SRL  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_SRL,  PC_INC, BR_EQ),
    SRLI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_SRL,  PC_INC, BR_EQ),

    EQ   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_EQ,   PC_INC, BR_EQ),
    NEQ  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_NE,   PC_INC, BR_EQ),
    GE   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_GE,   PC_INC, BR_EQ),
    GEU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_GEU,  PC_INC, BR_EQ),
    LT   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_LT,   PC_INC, BR_EQ),
    LTU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_LTU,  PC_INC, BR_EQ),

    JALR -> List(  IMM_I5,  SRC1DP_IMM, WB_REG, ALU_ZERO, PC_BRR,  BR_J),
    JAL  -> List(  IMM_U,   SRC1DP_IMM, WB_REG, ALU_ZERO, PC_BRI,  BR_J),
    BFEQ -> List(  IMM_U,   SRC1DP_IMM, WB_XXX, ALU_ZERO, PC_BRI,  BR_EQ),
    BFNE -> List(  IMM_U,   SRC1DP_IMM, WB_XXX, ALU_ZERO, PC_BRI,  BR_NE),
    BFGT -> List(  IMM_U,   SRC1DP_IMM, WB_XXX, ALU_ZERO, PC_BRI,  BR_GE),
    BFLT -> List(  IMM_U,   SRC1DP_IMM, WB_XXX, ALU_ZERO, PC_BRI,  BR_LT),
  )
}

class ControllerIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val imm  = Output(UInt(Control.IMM_BITWIDTH))
  val src1 = Output(UInt(Control.SRC1DP_BITWIDTH))
  val wb   = Output(UInt(Control.WB_BITWIDTH))
  val alu  = Output(UInt(Control.ALU_BITWIDTH))
  val pc   = Output(UInt(Control.PC_BITWIDTH))
  val br   = Output(UInt(Control.BR_BITWIDTH))
}

class Controller extends Module {
  val io = IO(new ControllerIO)

  val ctrlSignals = ListLookup(io.ins, Control.default, Control.map)

  io.imm  := ctrlSignals(0) /* Imm Gen Signal */
  io.src1 := ctrlSignals(1) /* Source 1 datapath signal select */
  io.wb   := ctrlSignals(2) /* Write back control */
  io.alu  := ctrlSignals(3) /* ALU control signal */
  io.pc   := ctrlSignals(4) /* PC control signal */
  io.br   := ctrlSignals(5) /* Branch control signal */
}