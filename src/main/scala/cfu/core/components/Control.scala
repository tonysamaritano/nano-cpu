package cfu.core

import cfu.config._

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
  def LD_BITWIDTH     = 2.W
  def LD_XXX          = 0.U(LD_BITWIDTH)
  def LD_IMM          = 1.U(LD_BITWIDTH)
  def LD_DATA         = 2.U(LD_BITWIDTH)

  /* Write Control */
  def WR_BITWIDTH     = 2.W
  def WR_XXX          = 0.U(WR_BITWIDTH)
  def WR_MEMB         = 1.U(WR_BITWIDTH)
  def WR_MEMW         = 2.U(WR_BITWIDTH)

  /* Default Control Signal */
  def default = List(IMM_RI, SRC1DP_REG, WB_XXX, ALU_ADD, BR_PC,  LD_XXX, WR_XXX)

  /* Control Signal Lookup */
  /*               Imm Gen, Src1,       Write,   ALU Ctl,  Br Src*/
  def map = Array(
    ADD  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_ADD,  BR_PC,  LD_XXX, WR_XXX),
    ADDI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU,  ALU_ADD,  BR_PC,  LD_XXX, WR_XXX),
    SUB  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_SUB,  BR_PC,  LD_XXX, WR_XXX),

    AND  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_AND,  BR_PC,  LD_XXX, WR_XXX),
    OR   -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_OR,   BR_PC,  LD_XXX, WR_XXX),
    XOR  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_XOR,  BR_PC,  LD_XXX, WR_XXX),
    NOT  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_NOT,  BR_PC,  LD_XXX, WR_XXX),
    SLL  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_SLL,  BR_PC,  LD_XXX, WR_XXX),
    SLLI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU,  ALU_SLL,  BR_PC,  LD_XXX, WR_XXX),
    SRL  -> List(  IMM_RI,  SRC1DP_REG, WB_ALU,  ALU_SRL,  BR_PC,  LD_XXX, WR_XXX),
    SRLI -> List(  IMM_RI,  SRC1DP_IMM, WB_ALU,  ALU_SRL,  BR_PC,  LD_XXX, WR_XXX),

    LW   -> List(  IMM_I5,  SRC1DP_IMM, WB_DATA, ALU_ADD,  BR_PC,  LD_DATA, WR_XXX),
    LLI  -> List(  IMM_UU,  SRC1DP_IMM, WB_XXX,  ALU_ZERO, BR_PC,  LD_IMM,  WR_XXX),
    LUAI -> List(  IMM_UUU, SRC1DP_IMM, WB_WORD, ALU_ZERO, BR_PC,  LD_XXX,  WR_XXX),

    SW   -> List(  IMM_S,   SRC1DP_IMM, WB_XXX,  ALU_ADD,  BR_PC,  LD_XXX, WR_MEMW),

    EQ   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_EQ,   BR_PC,  LD_XXX, WR_XXX),
    NEQ  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_NE,   BR_PC,  LD_XXX, WR_XXX),
    GE   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_GE,   BR_PC,  LD_XXX, WR_XXX),
    GEU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_GEU,  BR_PC,  LD_XXX, WR_XXX),
    LT   -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_LT,   BR_PC,  LD_XXX, WR_XXX),
    LTU  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_LTU,  BR_PC,  LD_XXX, WR_XXX),

    JALR -> List(  IMM_I5,  SRC1DP_IMM, WB_PC,   ALU_ZERO, BR_REG, LD_XXX, WR_XXX),
    JAL  -> List(  IMM_U,   SRC1DP_IMM, WB_PC,   ALU_ZERO, BR_PC,  LD_XXX, WR_XXX),
    BR   -> List(  IMM_B,   SRC1DP_IMM, WB_XXX,  ALU_ZERO, BR_PC,  LD_XXX, WR_XXX),

    HLT  -> List(  IMM_RI,  SRC1DP_REG, WB_XXX,  ALU_ZERO, BR_PC,  LD_XXX, WR_XXX),
  )
}

class ControlSignals extends Bundle {
  val imm  = Output(UInt(Control.IMM_BITWIDTH))
  val src1 = Output(UInt(Control.SRC1DP_BITWIDTH))
  val wb   = Output(UInt(Control.WB_BITWIDTH))
  val alu  = Output(UInt(Control.ALU_BITWIDTH))
  val br   = Output(UInt(Control.BR_BITWIDTH))
  val ld   = Output(UInt(Control.LD_BITWIDTH))
  val wr   = Output(UInt(Control.WR_BITWIDTH))
  val halt = Output(Bool())
}

class Controller extends Module {
  val io = IO(new Bundle {
    val ins = Input(UInt(Config.INS_SIZE.W))
    val ctl = Output(new ControlSignals)
  })

  val ctrlSignals = ListLookup(io.ins, Control.default, Control.map)

  io.ctl.imm  := ctrlSignals(0) /* Imm Gen Signal */
  io.ctl.src1 := ctrlSignals(1) /* Source 1 datapath signal select */
  io.ctl.wb   := ctrlSignals(2) /* Write back control */
  io.ctl.alu  := ctrlSignals(3) /* ALU control signal */
  io.ctl.br   := ctrlSignals(4) /* Branch Source */
  io.ctl.ld   := ctrlSignals(5) /* Load Control */
  io.ctl.wr   := ctrlSignals(6) /* Write Control */

  /* Halt Ins */
  io.ctl.halt := (io.ins(2,0) === 7.U(3.W)) && (io.ins(15,13) === 7.U(3.W))
}