package cpu

import chisel3._
import chisel3.util.{MuxLookup}

class CoreData extends Bundle {
  val pc   = Input(UInt(Instructions.ARCH_SIZE.W))
  val data = Input(UInt(Instructions.WORD_SIZE.W))
}

class CoreIO extends Bundle {
  val ins    = Input(UInt(Instructions.INS_SIZE.W))
  val in     = new CoreData
  val out    = Flipped(new CoreData)
  val pc_sel = Output(Bool())
}

class DecodeIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val data = Input(UInt(Instructions.WORD_SIZE.W))
  val ctl  = new Control
  val src  = new RegisterData
  val imm  = Output(SInt(Instructions.WORD_SIZE.W))
}

class ExecuteIO extends Bundle {
  val src     = Flipped(new RegisterData)
  val ctl     = Flipped(new Control)
  val imm     = Input(SInt(Instructions.WORD_SIZE.W))
  val pc_in   = Input(UInt(Instructions.ARCH_SIZE.W))
  val pc_out  = Output(UInt(Instructions.ARCH_SIZE.W))
  val ld_word = Output(UInt(Instructions.WORD_SIZE.W))
  val alu     = new ALUData
}

class Decode extends Module {
  val io = IO(new DecodeIO)

  val ctl    = Module(new Controller)
  val immgen = Module(new ImmGen)
  val regs   = Module(new RegisterFile)

  ctl.io.ins := io.ins

  immgen.io.ins := io.ins
  immgen.io.ctl := ctl.io.ctl.imm

  regs.io.data   := io.data
  regs.io.dst_en := ctl.io.ctl.wb =/= 0.U /* Write to destination is disabled if 0 */
  regs.io.dst    := io.ins(5,3)
  regs.io.addr0  := io.ins(8,6)
  regs.io.addr1  := io.ins(12,10)

  io.ctl <> ctl.io.ctl
  io.src <> regs.io.out
  io.imm := immgen.io.imm
}

class Execute extends Module {
  val io = IO(new ExecuteIO)

  val alu = Module(new ALU)

  alu.io.ctl  := io.ctl.alu
  alu.io.src0 := io.src.src0
  alu.io.src1 := Mux(io.ctl.src1.asBool, io.imm.asUInt, io.src.src1)

  io.alu <> alu.io.data

  /* Multiply Imm by 2 because PC can never be an odd number */
  val immShift = (io.imm.asUInt << 1)
  io.pc_out := Mux(io.ctl.br.asBool,
    io.pc_in + immShift,
    io.src.src0 + immShift
  )

  /* Adds upper and lower 8 bits on subsequent cycles */
  val ld  = RegInit(0.U)
  ld := Mux(io.ctl.ld.asBool, io.imm.asUInt, 0.U)
  io.ld_word := ld + io.imm.asUInt
}

class Core extends Module {
  val io = IO(new CoreIO)

  val decode = Module(new Decode)
  val exec   = Module(new Execute)

  decode.io.ins  := io.ins
  decode.io.data := MuxLookup(decode.io.ctl.wb, 0.U, Seq(
    Control.WB_ALU  -> exec.io.alu.out,
    Control.WB_PC   -> (io.in.pc + 2.U),
    Control.WB_DATA -> io.in.data,
    Control.WB_WORD -> exec.io.ld_word
  ))

  exec.io.src <> decode.io.src
  exec.io.ctl <> decode.io.ctl
  exec.io.imm   := decode.io.imm
  exec.io.pc_in := io.in.pc

  /* Delays branch flag for next cycle */
  val brReg  = RegInit(false.B)
  brReg := exec.io.alu.br

  io.out.data := exec.io.alu.out
  io.out.pc   := exec.io.pc_out

  /* Jumps always select computed pc */
  io.pc_sel   := Mux(io.ins(2,0)===5.U, true.B, brReg)
}