package cfu.etc

import cfu.config._

import chisel3._

class MagicMemoryBase extends Bundle {
  val addr = Input(UInt(Config.WORD_SIZE.W))
}

class MagicMemoryIn extends MagicMemoryBase {
  val data = Input(UInt(Config.WORD_SIZE.W))
  val we   = Input(Bool())
}

class MagicMemoryOut extends MagicMemoryBase {
  val data  = Output(UInt(Config.WORD_SIZE.W))
}

class MagicMemoryInterface extends Bundle {
  val write = new MagicMemoryIn
  val read  = new MagicMemoryOut
}
