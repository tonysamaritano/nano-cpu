package cpu

import chisel3._
import chisel3.util._

class FetchStage(width: Int) extends Module {
  val io = IO(new Bundle {
    val halt    = Input(Bool())
    val data    = Output(UInt(width.W))
    val opcode  = Output(UInt(4.W)) /* This shouldn't come out of here */
  })

  /* These modules are literally generated into this class
   * and run every cycle. This is really cool. */
  var pm = Module(new ProgramMemory(width, 256))
  var pc = Module(new Counter(width))
  var ins = Module(new InsDecoder(4, 4))

  var insReg = RegInit(0.U(width.W))

  /* All inputs need to be assigned or else the hardware generation
   * will fail*/
  pc.io.en := ~io.halt
  pc.io.set := false.B
  pc.io.in := 0.U

  /* Assign the program counter to the program memory */
  pm.io.addr := pc.io.out
  insReg := pm.io.data

  /* This is all garbage for now */
  ins.io.ins := insReg
  io.data := ins.io.data
  io.opcode := ins.io.opcode
}