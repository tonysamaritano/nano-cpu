package cpu

import chisel3._
import chisel3.util.ListLookup

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
  def REG_BITWIDTH = 2.W
  val REG_NONE     = 0.U(REG_BITWIDTH)
  val REG_A        = 1.U(REG_BITWIDTH)
  val REG_B        = 2.U(REG_BITWIDTH)
  val REG_AB       = 3.U(REG_BITWIDTH)

  /* Default Control Signal */
  val default = List(BUS_XXX, FLG_XXX, REG_NONE, ALU_XXX)

  /* Control Signal Lookup */
  val map = Array(
    ADD  -> List(BUS_ALU_OUT, FLG_STORE_ZC, REG_A, ALU_ADD),
    SUB  -> List(BUS_ALU_OUT, FLG_STORE_ZC, REG_A, ALU_SUB)
  )
}

class ControlSignals(width: Int) extends Module {
  val io = IO(new Bundle {
    val ins = Input(UInt(width.W))
    val bus = Output(UInt(Control.BUS_BITWIDTH))
    val flg = Output(UInt(Control.FLG_BITWIDTH))
    val reg = Output(UInt(Control.REG_BITWIDTH))
    val alu = Output(UInt(Control.ALU_BITWIDTH))
  })

  /* Decode Instruction and Lookup Control Signals */
  val ctrlSignals = ListLookup(io.ins, Control.default, Control.map)

  /* Wire Control Signal Outputs */
  io.bus := ctrlSignals(0) /* Bus Control */
  io.flg := ctrlSignals(1) /* Flag Control */
  io.reg := ctrlSignals(2) /* Register Control */
  io.alu := ctrlSignals(3) /* ALU Control Signals */
}