package cpu

import chisel3._

class Processor extends Module {
  val io = IO(new Bundle {
    val mem   = Flipped(new MagicMemoryInterface)
    val halt  = Output(Bool())
  })

  val pc   = RegInit(0.U(Instructions.ARCH_SIZE.W))
  val core = Module(new Core)

  core.io.ins := io.mem.read.data
  core.io.in.data := io.mem.read.data
  core.io.in.pc := pc

  when (Module.reset.asBool) {
    pc := 0.U
  }.elsewhen (core.io.out.pc_sel) {
    pc := core.io.out.pc

  /* If we've loaded, we want to restore the previous PC because it
   * takes 2 cycles to load (von Neumann bottleneck) */
  }.elsewhen (~core.io.out.ld_en) {
    pc := pc + 2.U
  }

  io.mem.read.addr := pc
  io.mem.write.we := core.io.out.wr_en
  io.mem.write.addr := core.io.out.addr
  io.mem.write.data := core.io.out.data

  io.halt := core.io.out.halt
}