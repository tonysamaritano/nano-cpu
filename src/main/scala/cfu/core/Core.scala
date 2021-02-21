package cfu.core

import cfu.config._

import chisel3._
import chisel3.util.{MuxLookup}

class CoreIn extends Bundle {
  val pc   = Input(UInt(Config.ARCH_SIZE.W))
  val data = Input(UInt(Config.WORD_SIZE.W))
}

class CoreOut extends Bundle {
  val pc     = Output(UInt(Config.ARCH_SIZE.W))
  val data   = Output(UInt(Config.WORD_SIZE.W))
  val addr   = Output(UInt(Config.ARCH_SIZE.W))
  val pc_sel = Output(Bool())
  val wr_en  = Output(Bool())
  val ld_en  = Output(Bool())
  val halt   = Output(Bool())
}

class CoreIO extends Bundle {
  val ins    = Input(UInt(Config.INS_SIZE.W))
  val in     = new CoreIn
  val out    = new CoreOut
}

class Core extends Module {
  val io = IO(new CoreIO)

  val decode = Module(new Decode)
  val exec   = Module(new Execute)

  val lw     = RegInit(0.U(Config.WORD_SIZE.W))
  val ldCtl  = Reg(new Bundle {
    val data_valid = Bool()
    val ins        = UInt(Config.INS_SIZE.W)
  })

  /* Load previous instruction when load data is valid. This is because the
   * previous instruction understands where to load the data into (2 cycle load). */
  decode.io.in.ins  := Mux(ldCtl.data_valid, ldCtl.ins, io.ins)

  /* Write Back Lookup */
  decode.io.in.data := MuxLookup(decode.io.out.ctl.wb, 0.U, Seq(
    Control.WB_ALU  -> exec.io.out.alu.data,
    Control.WB_PC   -> (io.in.pc + 2.U), /* Stores current pc + 2 during Jump operations */
    Control.WB_DATA -> io.in.data,
    Control.WB_WORD -> (lw + decode.io.out.imm.asUInt)
  ))

  /* Decode/Execute interface */
  exec.io.in.src <> decode.io.out.src
  exec.io.in.ctl <> decode.io.out.ctl
  exec.io.in.imm := decode.io.out.imm
  exec.io.in.pc  := io.in.pc

  /* This section is a complete hack to make the two-step load instuction work in a
   * single cycle Core.
   *
   * We check and see if we have a data load. That needs to be done on the following
   * cycle because this CPU cannot read and fetch at the same time. Needs to be cleared
   * afterwards. */
  lw               := Mux(decode.io.out.ctl.ld===Control.LD_IMM, decode.io.out.imm.asUInt, 0.U)
  ldCtl.data_valid := Mux(ldCtl.data_valid, false.B, decode.io.out.ctl.ld===Control.LD_DATA)
  ldCtl.ins        := io.ins

  /* Delays branch flag for next cycle */
  val brReg  = RegInit(false.B)
  brReg := exec.io.out.alu.br


  /* Core Output */
  io.out.data   := decode.io.out.src.src1
  io.out.pc     := exec.io.out.pc
  io.out.ld_en  := Mux(ldCtl.data_valid, decode.io.out.ctl.ld===Control.LD_DATA, false.B)
  io.out.pc_sel := Mux(io.ins(2,0)===5.U, true.B, brReg) /* Jumps always select computed pc */
  io.out.addr   := exec.io.out.alu.data
  io.out.wr_en  := decode.io.out.ctl.wr.orR
  io.out.halt   := decode.io.out.halt
}