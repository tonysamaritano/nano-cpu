package cpu

import chisel3._
import chisel3.util.{ListLookup}

import Instructions._

object Control {
  /* ALU Control */
  def ALU_BITWIDTH = 4.W
  def ALU_ADD = 0.U(ALU_BITWIDTH)
  def ALU_SUB = 1.U(ALU_BITWIDTH)
  def ALU_AND = 2.U(ALU_BITWIDTH)
  def ALU_OR  = 3.U(ALU_BITWIDTH)
  def ALU_XOR = 4.U(ALU_BITWIDTH)
  def ALU_NOT = 5.U(ALU_BITWIDTH)
  def ALU_SLL = 6.U(ALU_BITWIDTH)
  def ALU_SRL = 7.U(ALU_BITWIDTH)
  def ALU_EQ  = 8.U(ALU_BITWIDTH)
  def ALU_GE  = 9.U(ALU_BITWIDTH)
  def ALU_GEU = 10.U(ALU_BITWIDTH)

  /* ALU Flags */
  def ALU_FLAGS = 2.W
  def ALU_FEQ  = 0
  def ALU_FGT  = 1

  /* Imm Control */
  def IMM_BITWIDTH = 2.W
  def IMM_RI      = 0.U(IMM_BITWIDTH)
  def IMM_I5      = 1.U(IMM_BITWIDTH)
  def IMM_U       = 2.U(IMM_BITWIDTH)
  def IMM_S       = 3.U(IMM_BITWIDTH)

  /* Source 1 Datapath */
  def SRC1DP_BITWIDTH = 2.W
  def SRC1DP_REG      = 0.U(SRC1DP_BITWIDTH)
  def SRC1DP_IMM      = 1.U(SRC1DP_BITWIDTH)
  def SRC1DP_ONE      = 2.U(SRC1DP_BITWIDTH)

  /* Write Control */
  def WB_BITWIDTH     = 2.W
  def WB_XXX          = 0.U(WB_BITWIDTH)
  def WB_REG          = 1.U(WB_BITWIDTH)

  /* Default Control Signal */
  val default = List(IMM_RI, SRC1DP_REG, WB_XXX, ALU_ADD)

  /* Control Signal Lookup */
  /*               Imm Gen, Src1,       Write,  ALU Ctl, */
  val map = Array(
    ADD  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_ADD),
    ADDI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_ADD),
    SUB  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_SUB),
    AND  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_AND),
    OR   -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_OR),
    XOR  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_XOR),
    NOT  -> List(  IMM_RI,  SRC1DP_REG, WB_REG, ALU_NOT),
    SLL  -> List(  IMM_RI,  SRC1DP_ONE, WB_REG, ALU_SLL),
    SLLI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_SLL),
    SRL  -> List(  IMM_RI,  SRC1DP_ONE, WB_REG, ALU_SRL),
    SRLI -> List(  IMM_RI,  SRC1DP_IMM, WB_REG, ALU_SRL),
  )
}

class ControllerIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val imm  = Output(UInt(Control.IMM_BITWIDTH))
  val src1 = Output(UInt(Control.SRC1DP_BITWIDTH))
  val wb   = Output(UInt(Control.WB_BITWIDTH))
  val alu  = Output(UInt(Control.ALU_BITWIDTH))
}

class Controller extends Module {
  val io = IO(new ControllerIO)

  val ctrlSignals = ListLookup(io.ins, Control.default, Control.map)

  io.imm  := ctrlSignals(0) /* Imm Gen Signal */
  io.src1 := ctrlSignals(1) /* Source 1 datapath signal select */
  io.wb   := ctrlSignals(2) /* Write back control */
  io.alu  := ctrlSignals(3) /* ALU control signal */
}