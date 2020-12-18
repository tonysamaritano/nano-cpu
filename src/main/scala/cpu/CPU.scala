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

class CPU(width: Int, autoLoad: Boolean) extends Module {
  val io = IO(new Bundle {
    val load = Input(Bool())
    val addr = Input(UInt(width.W))
    val data = Input(UInt(width.W))
    val halt = Output(Bool())
    val valid   = Output(Bool())
    val output  = Output(UInt(width.W))
  })

  /* A and B registers */
  var a = RegInit(0.U(width.W))
  var b = RegInit(0.U(width.W))

  /* Program Counter */
  val pc = Module(new Counter(width))

  /* Instructions */
  var ins = Module(new InsDecoder(4, 4))
  var insReg = RegInit(0.U(width.W))

  /* Memory */
  val insCache = Mem(16, UInt(width.W))
  val dataCache = Mem(16, UInt(width.W))

  /* Wireup a startup program */
  if (autoLoad) {
    for (i <- 0 until ProgramMemory.program.length)
    {
      insCache(i) := ProgramMemory.program(i)
    }
  }

  /* Fetch Ins */
  insReg := insCache(pc.io.out)

  when (io.load) {
    /* Wire pc enable to false when loading */
    pc.io.en := false.B

    insCache(io.addr) := io.data
    ins.io.ins := 0.U
  }.otherwise {
    /* Wire Ins to instructionReg */
    pc.io.en := true.B
    ins.io.ins := insReg
  }

  /* ALU will be wrapped into the Execute Stage and
   * controlled via control signals */
  var alu = Module(new ALU(width))

  /* Wire ALU */
  alu.io.sel := true.B
  alu.io.inA := a
  alu.io.inB := dataCache(ins.io.data)

  /* I think that's all you'd need to do for the Add/Sub
   * operations. */
  when (ins.io.opcode===CPUStuff.ADD) {
    alu.io.sel := true.B /* Select Addition */
    a := alu.io.out /* Put the output in the A register */
  }.elsewhen(ins.io.opcode===CPUStuff.SUB) {
    alu.io.sel := false.B /* Select Subtraction */
    a := alu.io.out /* Put the output in the A register */
  }.otherwise {
    alu.io.sel := true.B /* It doesn't really matter here */
  }

  /* Load A register */
  when (ins.io.opcode===CPUStuff.LDA){
    a := dataCache(ins.io.data)
  }.elsewhen(ins.io.opcode===CPUStuff.LDI) {
    a := ins.io.data
  }

  /* Store from A */
  when (ins.io.opcode===CPUStuff.STA) {
    dataCache(ins.io.data) := a
  }

  /* Jump */
  when (ins.io.opcode===CPUStuff.JMP) {
    pc.io.set := true.B
    // pc.io.en := false.B
    pc.io.in := ins.io.data
  }.otherwise {
    pc.io.set := false.B
    // pc.io.en := true.B
    pc.io.in := 0.U
  }

  /* Halt */
  when (ins.io.opcode===CPUStuff.HLT) {
    io.halt := true.B
  }. otherwise {
    io.halt := false.B
  }

  /* Output */
  when (ins.io.opcode===CPUStuff.OUT) {
    io.valid := true.B
    io.output := a
  }.otherwise {
    io.valid := false.B
    io.output := 0.U
  }

  // when (ins.io.opcode===CPUStuff.NOP){
  //   printf("NOP\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.LDA){
  //   printf("LDA\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.ADD){
  //   printf("ADD\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.SUB){
  //   printf("SUB\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.STA){
  //   printf("STA\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.LDI){
  //   printf("LDI\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.JMP){
  //   printf("JMP\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.JC){
  //   printf("JC\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.JZ){
  //   printf("JZ\n")
  // }.elsewhen(ins.io.opcode===CPUStuff.OUT){
  //   printf("OUT\n")
  // }.otherwise {
  //   printf("HLT\n")
  // }

  /* Debug some shit */
  // printf(p"Opcode: ${ins.io.opcode}\n")
  // printf(p"A: ${a}\n")
  // printf(p"B: ${memory(ins.io.data)}\n")
  // printf(p"Mem0: ${memory(0.U)}\n")
}