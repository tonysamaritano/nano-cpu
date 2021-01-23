package cpu

import chisel3._
import chisel3.util.{ListLookup, Enum, log2Ceil}

import Instructions._

object Control {
  /* ALU Control */
  def ALU_BITWIDTH = 2.W
  val ALU_XXX  = 0.U(ALU_BITWIDTH)
  val ALU_ADD  = 1.U(ALU_BITWIDTH)
  val ALU_SUB  = 2.U(ALU_BITWIDTH)

  /* Bus Control */
  def BUS_BITWIDTH = 1.W
  val BUS_XXX     = 0.U(BUS_BITWIDTH)
  val BUS_ALU_OUT = 1.U(BUS_BITWIDTH)

  /* CPU Flags Control */
  def FLG_BITWIDTH = 1.W
  val FLG_XXX      = 0.U(FLG_BITWIDTH)
  val FLG_STORE_ZC = 1.U(FLG_BITWIDTH)

  /* Register Control */
  def REG_WB_BITWIDTH = 1.W
  val REG_WB_OFF      = 0.U(REG_WB_BITWIDTH)
  val REG_WB_EN       = 1.U(REG_WB_BITWIDTH)

  /* Imm Control */
  def IMM_BITWIDTH = 2.W
  val IMM_XXX      = 0.U(IMM_BITWIDTH)
  val IMM_3BR2     = 1.U(IMM_BITWIDTH)
  val IMM_8BR2     = 2.U(IMM_BITWIDTH)

  /* Default Control Signal */
  val default = List(BUS_XXX, FLG_XXX, REG_WB_OFF, ALU_XXX, IMM_XXX)

  /* Control Signal Lookup */
  /*               Bus Ctrl,    Flag Storage, Reg Enable, ALU Op,  Imm Gen */
  val map = Array(
    ADD  -> List(  BUS_ALU_OUT, FLG_STORE_ZC, REG_WB_EN,  ALU_ADD, IMM_XXX),
    ADDI -> List(  BUS_ALU_OUT, FLG_STORE_ZC, REG_WB_EN,  ALU_ADD, IMM_3BR2),
    SUB  -> List(  BUS_ALU_OUT, FLG_STORE_ZC, REG_WB_EN,  ALU_SUB, IMM_XXX),
    LDI  -> List(  BUS_XXX,     FLG_XXX,      REG_WB_EN,  ALU_XXX, IMM_8BR2),
  )
}

class ControlSignals(width: Int) extends Module {
  val io = IO(new Bundle {
    val ins  = Input(UInt(width.W))
    val bus  = Output(UInt(Control.BUS_BITWIDTH))
    val flg  = Output(UInt(Control.FLG_BITWIDTH))
    val alu  = Output(UInt(Control.ALU_BITWIDTH))
    val reg  = Output(UInt(Control.REG_WB_BITWIDTH))
    val imm  = Output(UInt(Control.IMM_BITWIDTH))
    val dst  = Output(UInt(log2Ceil(REGFILE_SIZE).W))
    val src0 = Output(UInt(log2Ceil(REGFILE_SIZE).W))
    val src1 = Output(UInt(log2Ceil(REGFILE_SIZE).W))
  })

  /* Decode Instruction and Lookup Control Signals */
  val ctrlSignals = ListLookup(io.ins, Control.default, Control.map)
  val opcode = io.ins(2,0)

  /* Wire registers up */
  when (opcode===0.U) {
    io.dst  := io.ins(9, 7)
    io.src0 := io.ins(12, 10)
    io.src1 := io.ins(15, 13)
  }.otherwise {
    io.dst  := 0.U
    io.src0 := 0.U
    io.src1 := 0.U
  }

  /* Wire Control Signal Outputs */
  io.bus  := ctrlSignals(0) /* Bus Control */
  io.flg  := ctrlSignals(1) /* Flag Control */
  io.reg  := ctrlSignals(2) /* Register Enable */
  io.alu  := ctrlSignals(3) /* ALU Control Signals */
  io.imm  := ctrlSignals(4) /* Imm Gen Signal */
}

object Control2 {
  /* Imm Control */
  def IMM_BITWIDTH = 2.W
  val IMM_RI      = 0.U(IMM_BITWIDTH)
  val IMM_I5      = 1.U(IMM_BITWIDTH)
  val IMM_U       = 2.U(IMM_BITWIDTH)
  val IMM_S       = 3.U(IMM_BITWIDTH)

  /* Default Control Signal */
  val default = List(IMM_RI)

  /* Control Signal Lookup */
  /*               Imm Gen */
  val map = Array(
    Instructions2.ADD  -> List(  IMM_RI),
    Instructions2.ADDI -> List(  IMM_RI),
  )
}

class Controller2 extends Bundle {
  val ins = Input(UInt(Instructions2.INS_SIZE.W))
  val imm = Output(UInt(Control2.IMM_BITWIDTH))
}

class Controller extends Module {
  val io = IO(new Controller2)

  val ctrlSignals = ListLookup(io.ins, Control2.default, Control2.map)

  io.imm := ctrlSignals(0)
}