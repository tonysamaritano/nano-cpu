package cpu

import chisel3._
import chisel3.util._

object CPUStuff {
  /* Instructions */
  val NOP = 0.U(4.W)
  val LDA = 1.U(4.W)
  val ADD = 2.U(4.W)
  val SUB = 3.U(4.W)

  val STA = 4.U(4.W)
  val LDI = 5.U(4.W)
  val JMP = 6.U(4.W)
  val JC  = 7.U(4.W)

  val JZ  = 8.U(4.W)

  val OUT = 14.U(4.W)
  val HLT = 15.U(4.W)
}

class CPU(width: Int) extends Module {
  val io = IO(new Bundle {
    val halt    = Input(Bool())
    val res  = Output(UInt(width.W))
  })

  var a = RegInit(0.U(width.W))
  var b = RegInit(0.U(width.W))
  val memory = Mem(16, UInt(width.W))

  /* Fetch Stage of the CPU */
  var fetch = Module(new FetchStage(width))

  /* ALU will be wrapped into the Execute Stage and
   * controlled via control signals */
  var alu = Module(new ALU(width))

  /* Tie to input for now */
  fetch.io.halt := io.halt

  /* Wire ALU */
  alu.io.sel := true.B
  alu.io.inA := a
  alu.io.inB := memory(fetch.io.data)

  /* I think that's all you'd need to do for the Add/Sub
   * operations. */
  when (fetch.io.opcode===CPUStuff.ADD) {
    alu.io.sel := true.B /* Select Addition */
    a := alu.io.out /* Put the output in the A register */
  }.elsewhen(fetch.io.opcode===CPUStuff.SUB) {
    alu.io.sel := false.B /* Select Subtraction */
    a := alu.io.out /* Put the output in the A register */
  }.otherwise {
    alu.io.sel := true.B /* It doesn't really matter here */
  }

  /* Load A register */
  when (fetch.io.opcode===CPUStuff.LDA){
    a := memory(fetch.io.data)
  }.elsewhen(fetch.io.opcode===CPUStuff.LDI) {
    a := fetch.io.data
  }

  when (fetch.io.opcode===CPUStuff.STA) {
    memory(fetch.io.data) := a
  }

  /* Temporary */
  io.res := memory(1.U)

  // when (fetch.io.opcode===CPUStuff.NOP){
  //   printf("NOP\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.LDA){
  //   printf("LDA\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.ADD){
  //   printf("ADD\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.SUB){
  //   printf("SUB\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.STA){
  //   printf("STA\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.LDI){
  //   printf("LDI\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.JMP){
  //   printf("JMP\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.JC){
  //   printf("JC\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.JZ){
  //   printf("JZ\n")
  // }.elsewhen(fetch.io.opcode===CPUStuff.OUT){
  //   printf("OUT\n")
  // }.otherwise {
  //   printf("HLT\n")
  // }

  /* Debug some shit */
  // printf(p"Opcode: ${fetch.io.opcode}\n")
  // printf(p"A: ${a}\n")
  // printf(p"B: ${memory(fetch.io.data)}\n")
  // printf(p"Mem0: ${memory(0.U)}\n")
}