package cpu

import chisel3._
import chisel3.util._

object ProgramMemory {
  /* Program for program memory */
  val program = Array(
    //  op    d
    "b0000_0000".U, // NOP          // NOP to flush pipeline
    "b0101_0110".U, // LDI #0x6     // Load 6 into A Register
    "b0100_0000".U, // STA $0x0     // Put 6 into 0x0 address
    "b0101_1111".U, // LDI #0xf     // Load 15 into A Register
    "b0010_0000".U, // ADD $0x0     // Add address STORE_A to reg A
    "b0100_0000".U, // STA $0x0     // Reg A into 0x0 address
    "b0001_0000".U, // LDA $0x0     // Load 0x0 into A Register
    "b0010_0000".U, // ADD $0x0     // Add address STORE_A to reg A
    "b1110_0000".U, // OUT          // Outputs contents of reg A
    "b1111_0000".U  // HLT          // Halt processor
  )
}

class ProgramMemory(width: Int, depth: Int) extends Module {
  val io = IO(new Bundle {
    val addr    = Input(UInt(width.W))
    val data    = Output(UInt(width.W))
  })
  
  val memory = Mem(depth, UInt(width.W))

  /* This is probably a totally shit way of doing this */
  for (i <- 0 until ProgramMemory.program.length)
  {
    memory(i) := ProgramMemory.program(i)
  }

  io.data := memory(io.addr)
}