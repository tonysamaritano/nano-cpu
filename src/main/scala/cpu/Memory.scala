package cpu

import chisel3._
import chisel3.util.{Cat}

class MagicMemoryBase extends Bundle {
  val addr = Input(UInt(Instructions.WORD_SIZE.W))
}

class MagicMemoryIn extends MagicMemoryBase {
  val data = Input(UInt(Instructions.WORD_SIZE.W))
  val we   = Input(Bool())
}

class MagicMemoryOut extends MagicMemoryBase {
  val data  = Output(UInt(Instructions.WORD_SIZE.W))
}

class MagicMemoryInterface extends Bundle {
  val write = new MagicMemoryIn
  val read  = new MagicMemoryOut
}

class MagicMemory(size: Int) extends Module {
  val io = IO(new MagicMemoryInterface)

  val mem = Mem(size, UInt(8.W))

  when (io.write.we) {
    mem(io.write.addr) := io.write.data(7,0)
    mem(io.write.addr + 1.U) := io.write.data(15,8)
  }

  io.read.data := Cat(mem(io.read.addr + 1.U), mem(io.read.addr))
}