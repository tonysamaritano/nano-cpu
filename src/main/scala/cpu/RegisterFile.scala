package cpu

import chisel3._

class RegisterFile(width: Int) extends Module {
    val io = IO(new Bundle {
        val ctl = Input(UInt(Control.REG_BITWIDTH))
        val data = Input(UInt(width.W))
        val outA = Output(UInt(width.W))
        val outB = Output(UInt(width.W))
    })

    val regs = Mem(2, UInt(width.W))

    when (io.ctl===Control.REG_A) {
        regs(0) := io.data
    }.elsewhen (io.ctl===Control.REG_B) {
        regs(1) := io.data
    }.elsewhen (io.ctl===Control.REG_AB) {
        regs(0) := io.data
        regs(1) := io.data
    }

    io.outA := regs(0)
    io.outB := regs(1)
}