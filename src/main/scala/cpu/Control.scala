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
  def IMM_RI       = 0.U(IMM_BITWIDTH)
  def IMM_I5       = 1.U(IMM_BITWIDTH)
  def IMM_U        = 2.U(IMM_BITWIDTH)
  def IMM_UU       = 3.U(IMM_BITWIDTH)
  def IMM_S        = 4.U(IMM_BITWIDTH)
  def IMM_B        = 5.U(IMM_BITWIDTH)

  /* Source 1 Datapath */
  def SRC1DP_BITWIDTH = 1.W
  def SRC1DP_REG      = 0.U(SRC1DP_BITWIDTH)
  def SRC1DP_IMM      = 1.U(SRC1DP_BITWIDTH)

  /* Write Back Control */
  def WB_BITWIDTH     = 2.W
  def WB_XXX          = 0.U(WB_BITWIDTH)
  def WB_ALU          = 1.U(WB_BITWIDTH)
  def WB_PC           = 2.U(WB_BITWIDTH)
  def WB_DATA         = 3.U(WB_BITWIDTH)

  /* Branch Control */
  def BR_BITWIDTH     = 1.W
  def BR_REG          = 0.U(WB_BITWIDTH)
  def BR_PC           = 1.U(WB_BITWIDTH)

  /* Default Control Signal */
  val default = List(IMM_RI, SRC1DP_REG, WB_XXX, ALU_ADD, BR_PC)

  /* Control Signal Lookup */
  /*               Imm Gen, Src1,       Write,  ALU Ctl,  Br Src*/
  val map = Array(
    ADD  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU, ALU_ADD,  BR_PC),
    ADDI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU, ALU_ADD,  BR_PC),
    SUB  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU, ALU_SUB,  BR_PC),

    AND  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU, ALU_AND,  BR_PC),
    OR   -> List(  IMM_RI,  SRC1DP_REG, WB_ALU, ALU_OR,   BR_PC),
    XOR  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU, ALU_XOR,  BR_PC),
    NOT  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU, ALU_NOT,  BR_PC),
    SLL  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU, ALU_SLL,  BR_PC),
    SLLI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU, ALU_SLL,  BR_PC),
    SRL  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU, ALU_SRL,  BR_PC),
    SRLI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU, ALU_SRL,  BR_PC),

    // LW   -> List(  IMM_I5,  SRC1DP_IMM, WB_ALU, ALU_ADD,  BR_EQ),

    EQ   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_EQ,   BR_PC),
    NEQ  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_NE,   BR_PC),
    GE   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_GE,   BR_PC),
    GEU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_GEU,  BR_PC),
    LT   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_LT,   BR_PC),
    LTU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX, ALU_LTU,  BR_PC),

    JALR -> List(  IMM_I5,  SRC1DP_IMM, WB_PC,  ALU_ZERO, BR_REG),
    JAL  -> List(  IMM_U,   SRC1DP_IMM, WB_PC,  ALU_ZERO, BR_PC),
    BR   -> List(  IMM_B,   SRC1DP_IMM, WB_XXX, ALU_ZERO, BR_PC),
  )
}

class Control extends Bundle {
  val imm  = Output(UInt(Control.IMM_BITWIDTH))
  val src1 = Output(UInt(Control.SRC1DP_BITWIDTH))
  val wb   = Output(UInt(Control.WB_BITWIDTH))
  val alu  = Output(UInt(Control.ALU_BITWIDTH))
  val br   = Output(UInt(Control.BR_BITWIDTH))
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
  io.ctl.br   := ctrlSignals(4) /* Branch Source */
}