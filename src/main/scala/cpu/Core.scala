package cpu

import chisel3._
import chisel3.util.{MuxLookup}

class CoreData extends Bundle {
  val pc   = Input(UInt(Instructions.ARCH_SIZE.W))
  val data = Input(UInt(Instructions.WORD_SIZE.W))
}

class CoreOut extends CoreData {
  val addr   = Input(UInt(Instructions.ARCH_SIZE.W))
  val pc_sel = Input(Bool())
  val wr_en  = Input(Bool())
  val ld_en  = Input(Bool())
  val halt   = Input(Bool())
}

class CoreIO extends Bundle {
  val ins    = Input(UInt(Instructions.INS_SIZE.W))
  val in     = new CoreData
  val out    = Flipped(new CoreOut)
}

class DecodeIO extends Bundle {
  val ins  = Input(UInt(Instructions.INS_SIZE.W))
  val data = Input(UInt(Instructions.WORD_SIZE.W))
  val ctl  = new Control
  val src  = new RegisterData
  val imm  = Output(SInt(Instructions.WORD_SIZE.W))
  val halt = Output(Bool())
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

class LoadControl extends Bundle {
  val data_valid = Bool()
  val ins        = UInt(Instructions.INS_SIZE.W)
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
  regs.io.dst_en := ctl.io.ctl.wb.orR
  regs.io.dst    := io.ins(5,3)
  regs.io.addr0  := io.ins(8,6)
  regs.io.addr1  := io.ins(12,10)

  io.ctl <> ctl.io.ctl
  io.src <> regs.io.out
  io.imm := immgen.io.imm
  io.halt := io.ctl.halt
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
  ld := Mux(io.ctl.ld===Control.LD_IMM, io.imm.asUInt, 0.U)
  io.ld_word := ld + io.imm.asUInt
}

class Core extends Module {
  val io = IO(new CoreIO)

  val decode = Module(new Decode)
  val exec   = Module(new Execute)

  val ldCtl  = Reg(new LoadControl)

  /* Load previous instruction when load data is valid. This is because the
   * previous instruction understands where to load the data into (2 cycle load). */
  decode.io.ins  := Mux(ldCtl.data_valid, ldCtl.ins, io.ins)
  decode.io.data := MuxLookup(decode.io.ctl.wb, 0.U, Seq(
    Control.WB_ALU  -> exec.io.alu.out,
    Control.WB_PC   -> (io.in.pc + 2.U), /* Stores current pc + 2 during Jump operations */
    Control.WB_DATA -> io.in.data,
    Control.WB_WORD -> exec.io.ld_word
  ))

  exec.io.src <> decode.io.src
  exec.io.ctl <> decode.io.ctl
  exec.io.imm   := decode.io.imm
  exec.io.pc_in := io.in.pc

  /* We check and see if we have a data load. That needs to be done on the following
   * cycle because this CPU cannot read and fetch at the same time. Needs to be cleared
   * afterwards. */
  ldCtl.data_valid := Mux(ldCtl.data_valid, false.B, decode.io.ctl.ld===Control.LD_DATA)
  ldCtl.ins        := io.ins

  /* Delays branch flag for next cycle */
  val brReg  = RegInit(false.B)
  brReg := exec.io.alu.br

  io.out.data := decode.io.src.src1
  io.out.pc   := exec.io.pc_out
  io.out.ld_en := ldCtl.data_valid

  /* Jumps always select computed pc */
  io.out.pc_sel := Mux(io.ins(2,0)===5.U, true.B, brReg)
  io.out.addr   := exec.io.alu.out
  io.out.wr_en  := decode.io.ctl.wr.orR
  io.out.halt   := decode.io.halt
}