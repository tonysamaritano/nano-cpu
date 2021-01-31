package cpu

import chisel3._
import chisel3.util.log2Ceil

class RegisterData extends Bundle {
  val src0 = Output(UInt(Instructions.WORD_SIZE.W))
  val src1 = Output(UInt(Instructions.WORD_SIZE.W))
}

class RegisterFile extends Module {
  val io = IO(new Bundle {
    val data    = Input(UInt(Instructions.WORD_SIZE.W))
    val dst_en  = Input(Bool())
    val dst     = Input(UInt(log2Ceil(Instructions.REGFILE_SIZE).W))
    val addr0   = Input(UInt(log2Ceil(Instructions.REGFILE_SIZE).W))
    val addr1   = Input(UInt(log2Ceil(Instructions.REGFILE_SIZE).W))
    val out     = Output(new RegisterData)
  })

  val regs = Mem(
    Instructions.REGFILE_SIZE,
    UInt(Instructions.WORD_SIZE.W)
  )

  when (io.dst_en) {
    regs(io.dst) := io.data
  }

  io.out.src0 := Mux(io.addr0 > 0.U, regs(io.addr0), 0.U)
  io.out.src1 := Mux(io.addr1 > 0.U, regs(io.addr1), 0.U)
}