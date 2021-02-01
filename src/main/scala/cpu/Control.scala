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
  def IMM_UUU      = 6.U(IMM_BITWIDTH)

  /* Source 1 Datapath */
  def SRC1DP_BITWIDTH = 1.W
  def SRC1DP_REG      = 0.U(SRC1DP_BITWIDTH)
  def SRC1DP_IMM      = 1.U(SRC1DP_BITWIDTH)

  /* Write Back Control */
  def WB_BITWIDTH     = 3.W
  def WB_XXX          = 0.U(WB_BITWIDTH)
  def WB_ALU          = 1.U(WB_BITWIDTH)
  def WB_PC           = 2.U(WB_BITWIDTH)
  def WB_DATA         = 3.U(WB_BITWIDTH)
  def WB_WORD         = 4.U(WB_BITWIDTH)

  /* Branch Control */
  def BR_BITWIDTH     = 1.W
  def BR_REG          = 0.U(WB_BITWIDTH)
  def BR_PC           = 1.U(WB_BITWIDTH)

  /* Load Control */
  def LD_BITWIDTH     = 1.W
  def LD_XXX          = 0.U(LD_BITWIDTH)
  def LD_IMM          = 1.U(LD_BITWIDTH)

  /* Default Control Signal */
  val default = List(IMM_RI, SRC1DP_REG, WB_XXX, ALU_ADD, BR_PC,  LD_XXX)

  /* Control Signal Lookup */
  /*               Imm Gen, Src1,       Write,   ALU Ctl,  Br Src*/
  val map = Array(
    ADD  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_ADD,  BR_PC,  LD_XXX),
    ADDI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU,  ALU_ADD,  BR_PC,  LD_XXX),
    SUB  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_SUB,  BR_PC,  LD_XXX),

    AND  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_AND,  BR_PC,  LD_XXX),
    OR   -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_OR,   BR_PC,  LD_XXX),
    XOR  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_XOR,  BR_PC,  LD_XXX),
    NOT  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_NOT,  BR_PC,  LD_XXX),
    SLL  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_SLL,  BR_PC,  LD_XXX),
    SLLI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU,  ALU_SLL,  BR_PC,  LD_XXX),
    SRL  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_SRL,  BR_PC,  LD_XXX),
    SRLI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU,  ALU_SRL,  BR_PC,  LD_XXX),

    // LW   -> List(  IMM_I5,  SRC1DP_IMM, WB_ALU, ALU_ADD,  BR_EQ),
    LLI  -> List(  IMM_UU,  SRC1DP_IMM, WB_XXX,  ALU_ZERO, BR_PC,  LD_IMM),
    LUAI -> List(  IMM_UUU, SRC1DP_IMM, WB_WORD, ALU_ZERO, BR_PC,  LD_XXX),

    EQ   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_EQ,   BR_PC,  LD_XXX),
    NEQ  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_NE,   BR_PC,  LD_XXX),
    GE   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_GE,   BR_PC,  LD_XXX),
    GEU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_GEU,  BR_PC,  LD_XXX),
    LT   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_LT,   BR_PC,  LD_XXX),
    LTU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_LTU,  BR_PC,  LD_XXX),

    JALR -> List(  IMM_I5,  SRC1DP_IMM, WB_PC,   ALU_ZERO, BR_REG, LD_XXX),
    JAL  -> List(  IMM_U,   SRC1DP_IMM, WB_PC,   ALU_ZERO, BR_PC,  LD_XXX),
    BR   -> List(  IMM_B,   SRC1DP_IMM, WB_XXX,  ALU_ZERO, BR_PC,  LD_XXX),
  )
}

class Control extends Bundle {
  val imm  = Output(UInt(Control.IMM_BITWIDTH))
  val src1 = Output(UInt(Control.SRC1DP_BITWIDTH))
  val wb   = Output(UInt(Control.WB_BITWIDTH))
  val alu  = Output(UInt(Control.ALU_BITWIDTH))
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
  io.ctl.br   := ctrlSignals(4) /* Branch Source */
  io.ctl.ld   := ctrlSignals(5) /* Load Control */
}