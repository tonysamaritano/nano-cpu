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

  /* Default Control Signal */
  val default = List(ALU_XXX)

  /* Control Signal Lookup */
  val map = Array(
    ADD  -> List(ALU_ADD),
    SUB  -> List(ALU_SUB)
  )
}

class ControlSignals(width: Int) extends Module {
  val io = IO(new Bundle {
    val ins = Input(UInt(width.W))
    val alu = Output(UInt(Control.ALU_BITWIDTH))
  })

  /* Decode Instruction and Lookup Control Signals */
  val ctrlSignals = ListLookup(io.ins, Control.default, Control.map)

  /* ALU Control Signals */
  io.alu := ctrlSignals(0)
}