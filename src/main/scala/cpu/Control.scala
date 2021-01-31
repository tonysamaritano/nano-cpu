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

  /* Load Control */
  def LD_BITWIDTH     = 1.W
  def LD_PC           = 0.U(LD_BITWIDTH)
  def LD_ADDR         = 1.U(LD_BITWIDTH)

  /* Default Control Signal */
  val default = List(IMM_RI, SRC1DP_REG, WB_XXX, ALU_ADD, PC_STALL, BR_EQ, LD_PC)

  /* Control Signal Lookup */
  /*               Imm Gen, Src1,       Write,  ALU Ctl,  PC Ctl, Branch Ctl, */
  val map = Array(
    ADD  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_ADD,  PC_INC, BR_EQ,      LD_PC),
    ADDI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_ADD,  PC_INC, BR_EQ,      LD_PC),
    SUB  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_SUB,  PC_INC, BR_EQ,      LD_PC),

    AND  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_AND,  PC_INC, BR_EQ,      LD_PC),
    OR   -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_OR,   PC_INC, BR_EQ,      LD_PC),
    XOR  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_XOR,  PC_INC, BR_EQ,      LD_PC),
    NOT  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_NOT,  PC_INC, BR_EQ,      LD_PC),
    SLL  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_SLL,  PC_INC, BR_EQ,      LD_PC),
    SLLI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_SLL,  PC_INC, BR_EQ,      LD_PC),
    SRL  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_SRL,  PC_INC, BR_EQ,      LD_PC),
    SRLI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_SRL,  PC_INC, BR_EQ,      LD_PC),

    LW   -> List(  IMM_I5,  SRC1DP_IMM, WB_REG, ALU_ADD,  PC_INC, BR_EQ,      LD_ADDR),

    EQ   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_EQ,   PC_INC, BR_EQ,      LD_PC),
    NEQ  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_NE,   PC_INC, BR_EQ,      LD_PC),
    GE   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_GE,   PC_INC, BR_EQ,      LD_PC),
    GEU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_GEU,  PC_INC, BR_EQ,      LD_PC),
    LT   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_LT,   PC_INC, BR_EQ,      LD_PC),
    LTU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_LTU,  PC_INC, BR_EQ,      LD_PC),

    JALR -> List(  IMM_I5,  SRC1DP_IMM, WB_REG, ALU_ZERO, PC_BRR,  BR_J,      LD_PC),
    JAL  -> List(  IMM_U,   SRC1DP_IMM, WB_REG, ALU_ZERO, PC_BRI,  BR_J,      LD_PC),
    BFEQ -> List(  IMM_U,   SRC1DP_IMM, WB_XXX, ALU_ZERO, PC_BRI,  BR_EQ,     LD_PC),
    BFNE -> List(  IMM_U,   SRC1DP_IMM, WB_XXX, ALU_ZERO, PC_BRI,  BR_NE,     LD_PC),
    BFGT -> List(  IMM_U,   SRC1DP_IMM, WB_XXX, ALU_ZERO, PC_BRI,  BR_GE,     LD_PC),
    BFLT -> List(  IMM_U,   SRC1DP_IMM, WB_XXX, ALU_ZERO, PC_BRI,  BR_LT,     LD_PC),
  )
}

class Control extends Bundle {
  val imm  = Output(UInt(Control.IMM_BITWIDTH))
  val src1 = Output(UInt(Control.SRC1DP_BITWIDTH))
  val wb   = Output(UInt(Control.WB_BITWIDTH))
  val alu  = Output(UInt(Control.ALU_BITWIDTH))
  val pc   = Output(UInt(Control.PC_BITWIDTH))
  val br   = Output(UInt(Control.BR_BITWIDTH))
  val ld   = Output(UInt(Control.LD_BITWIDTH))
}

class Controller extends Module {
  val io = IO(new Bundle {
    val ins = Input(UInt(Instructions.INS_SIZE.W))
    val ctl = Output(new Control)
  })

  val ctrlSignals = ListLookup(io.ins, Control.default, Control.map)

  io.ctl.imm  := ctrlSignals(0) /* Imm Gen Signal */
  io.ctl.src1 := ctrlSignals(1) /* Source 1 datapath signal select */
  io.ctl.wb   := ctrlSignals(2) /* Write back control */
  io.ctl.alu  := ctrlSignals(3) /* ALU control signal */
  io.ctl.pc   := ctrlSignals(4) /* PC control signal */
  io.ctl.br   := ctrlSignals(5) /* Branch control signal */
  io.ctl.ld   := ctrlSignals(6) /* Load Control */
}