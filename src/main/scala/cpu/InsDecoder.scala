package cpu

import chisel3._
import chisel3.util._

class InsDecoder(opCodeWidth: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val ins     = Input(UInt((dataWidth+opCodeWidth).W))
    val data    = Output(UInt((dataWidth+opCodeWidth).W))
    val opcode  = Output(UInt(opCodeWidth.W))
  })
  
  /* Grab the most significant bits for the opcode */
  io.opcode := io.ins(opCodeWidth+dataWidth-1, dataWidth)

  /* Grab the lower bits for the data */
  io.data := Cat(0.U(4.W), io.ins(dataWidth-1, 0))
}