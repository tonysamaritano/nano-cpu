// See README.md for license details.

package cfu

import cfu.core._
import cfu.config._

import chisel3._
import chisel3.tester._
import chisel3.tester.RawTester.test

/**
  * This provides an alternate way to run tests, by executing then as a main
  * From sbt (Note: the test: prefix is because this main is under the test package hierarchy):
  * {{{
  * test:runMain cfu.main
  * }}}
  */
object main extends App {
  val rnd = scala.util.Random;

  def max = scala.math.pow(2, Config.WORD_SIZE).toLong - 1

  def toTwoCompl(max: Long, value: Long) : Long = {
    val res = max-value+1
    res
  }

  test(new RegisterFile) { c=>
    /* Registers */
    val x0 = 0.U
    val t0 = 3.U
    val t1 = 4.U
    val a0 = 7.U

    /* Data to store */
    var x = rnd.nextInt((max/2).toInt)
    var y = rnd.nextInt((max/2).toInt)
    var z = rnd.nextInt((max/2).toInt)

    /* Write random value to register */
    c.io.in.data.poke(x.U)
    c.io.in.dst.poke(t0)
    c.io.in.dst_en.poke(true.B)
    c.clock.step(1)
    c.io.in.data.poke(y.U)
    c.io.in.dst.poke(t1)
    c.io.in.dst_en.poke(true.B)
    c.clock.step(1)
    c.io.in.data.poke(z.U)
    c.io.in.dst.poke(a0)
    c.io.in.dst_en.poke(true.B)
    c.clock.step(1)

    /* Check Registers */
    c.io.in.dst_en.poke(false.B)
    c.io.in.dst.poke(t0)
    c.io.in.data.poke(z.U)
    c.io.in.addr0.poke(t0)
    c.io.in.addr1.poke(t1)
    c.io.out.src0.expect(x.U)
    c.io.out.src1.expect(y.U)
    c.io.in.addr0.poke(a0)
    c.io.out.src0.expect(z.U)
    c.clock.step(1)

    /* Verify output is still the same after clock */
    c.io.out.src0.expect(z.U)
    c.io.out.src1.expect(y.U)

    /* Verify x0 is always zero even after on data write */
    c.io.in.data.poke(x.U)
    c.io.in.dst.poke(x0)
    c.io.in.dst_en.poke(true.B)
    c.clock.step(1)
    c.io.in.addr0.poke(x0)
    c.io.out.src0.expect(0.U)
  }

  test(new ALU) { c =>
    var x = rnd.nextInt((max/8).toInt) + (max/8).toInt
    var y = rnd.nextInt((max/8).toInt) - 1

    require((x+y) < max)
    require(x > y)

    /* Add */
    c.io.in.ctl.poke(Control.ALU_ADD)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.data.expect((x+y).U)

    /* Sub */
    c.io.in.ctl.poke(Control.ALU_SUB)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.data.expect((x-y).U)
    c.io.in.src0.poke(y.U)
    c.io.in.src1.poke(x.U)
    c.io.out.data.expect(toTwoCompl(max, x-y).U)

    /* And */
    c.io.in.ctl.poke(Control.ALU_AND)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.data.expect((x&y).U)

    /* Or */
    c.io.in.ctl.poke(Control.ALU_OR)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.data.expect((x|y).U)

    /* Xor */
    c.io.in.ctl.poke(Control.ALU_XOR)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.data.expect((x^y).U)

    /* Not */
    c.io.in.ctl.poke(Control.ALU_NOT)
    c.io.in.src0.poke(x.U)
    c.io.out.data.expect((toTwoCompl(max, x)-1).U)

    /* Shift Left */
    c.io.in.ctl.poke(Control.ALU_SLL)
    c.io.in.src0.poke("b_0010_1011".U)
    c.io.in.src1.poke(1.U)
    c.io.out.data.expect("b_0101_0110".U)
    c.io.in.src1.poke(2.U)
    c.io.out.data.expect("b_1010_1100".U)

    /* Shift Right */
    c.io.in.ctl.poke(Control.ALU_SRL)
    c.io.in.src0.poke("b_0010_1011".U)
    c.io.in.src1.poke(1.U)
    c.io.out.data.expect("b_001_0101".U)
    c.io.in.src1.poke(2.U)
    c.io.out.data.expect("b_00_1010".U)

    /* EQ */
    c.io.in.ctl.poke(Control.ALU_EQ)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(x.U)
    c.io.out.br.expect(true.B)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.br.expect(false.B)

    /* NE */
    c.io.in.ctl.poke(Control.ALU_NE)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(x.U)
    c.io.out.br.expect(false.B)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.br.expect(true.B)

    /* GE */
    c.io.in.ctl.poke(Control.ALU_GEU)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(x.U)
    c.io.out.br.expect(true.B)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.br.expect(true.B)
    c.io.in.src0.poke(y.U)
    c.io.in.src1.poke(x.U)
    c.io.out.br.expect(false.B)
    c.io.in.ctl.poke(Control.ALU_GE)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(x.U)
    c.io.out.br.expect(true.B)
    if (x > max/2) c.io.in.src0.poke(x.U)
    else c.io.in.src0.poke(toTwoCompl(max, x).U)
    c.io.in.src1.poke(toTwoCompl(max, y).U)
    c.io.out.br.expect(false.B)
    if (x > max/2) c.io.in.src1.poke(x.U)
    else c.io.in.src1.poke(toTwoCompl(max, x).U)
    c.io.in.src0.poke(toTwoCompl(max, y).U)
    c.io.out.br.expect(true.B)
    if (x > max/2) c.io.in.src1.poke(x.U)
    else c.io.in.src1.poke(toTwoCompl(max, x).U)
    c.io.in.src0.poke(y.U)
    c.io.out.br.expect(true.B)

    /* LT */
    c.io.in.ctl.poke(Control.ALU_LTU)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(x.U)
    c.io.out.br.expect(false.B)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(y.U)
    c.io.out.br.expect(false.B)
    c.io.in.src0.poke(y.U)
    c.io.in.src1.poke(x.U)
    c.io.out.br.expect(true.B)
    c.io.in.ctl.poke(Control.ALU_LT)
    c.io.in.src0.poke(x.U)
    c.io.in.src1.poke(x.U)
    c.io.out.br.expect(false.B)
    if (x > max/2) c.io.in.src1.poke(x.U)
    else c.io.in.src1.poke(toTwoCompl(max, x).U)
    c.io.in.src0.poke(toTwoCompl(max, y).U)
    c.io.out.br.expect(false.B)
    if (x > max/2) c.io.in.src1.poke(x.U)
    else c.io.in.src1.poke(toTwoCompl(max, x).U)
    c.io.in.src0.poke(y.U)
    c.io.out.br.expect(false.B)
    if (x > max/2) c.io.in.src0.poke(x.U)
    else c.io.in.src0.poke(toTwoCompl(max, x).U)
    c.io.in.src1.poke(y.U)
    c.io.out.br.expect(true.B)
  }

  test(new ImmGen) { c =>
    /* 4-bit immediate */
    c.io.ins.poke("b_0011_0010_0000_0000".U)
    c.io.ctl.poke(Control.IMM_RI)
    c.io.imm.expect(-7.S)

    c.io.ins.poke("b_0010_1110_0000_0000".U)
    c.io.ctl.poke(Control.IMM_RI)
    c.io.imm.expect(7.S)

    /* 5-bit immediate */
    c.io.ins.poke("b_1010_0010_0000_0000".U)
    c.io.ctl.poke(Control.IMM_I5)
    c.io.imm.expect(-15.S)

    c.io.ins.poke("b_0011_1110_0000_0000".U)
    c.io.ctl.poke(Control.IMM_I5)
    c.io.imm.expect(15.S)

    /* 8-bit immediate */
    c.io.ins.poke("b_0010_0011_0000_0000".U)
    c.io.ctl.poke(Control.IMM_U)
    c.io.imm.expect(-127.S)

    c.io.ins.poke("b_1011_1110_1100_0000".U)
    c.io.ctl.poke(Control.IMM_U)
    c.io.imm.expect(127.S)

    /* 8-bit immediate unsigned */
    c.io.ins.poke("b_1011_1111_1100_0000".U)
    c.io.ctl.poke(Control.IMM_UU)
    c.io.imm.expect(255.S)

    /* 8-bit immediate unsigned upper */
    c.io.ins.poke("b_1001_1111_1100_0000".U)
    c.io.ctl.poke(Control.IMM_UUU)
    if (Config.WORD_SIZE > 16) c.io.imm.expect(65280.S)
    else c.io.imm.expect(-256.S) /* -256.S is 65280.U */

    /* 5-bit immediate */
    c.io.ins.poke("b_1010_0010_0000_0000".U)
    c.io.ctl.poke(Control.IMM_S)
    c.io.imm.expect(-15.S)

    c.io.ins.poke("b_0010_0010_0011_1000".U)
    c.io.ctl.poke(Control.IMM_S)
    c.io.imm.expect(15.S)

    /* 11-bit immediate */
    c.io.ins.poke("b_0000_0010_0010_0000".U)
    c.io.ctl.poke(Control.IMM_B)
    c.io.imm.expect(-1023.S)

    c.io.ins.poke("b_1111_1111_1101_1000".U)
    c.io.ctl.poke(Control.IMM_B)
    c.io.imm.expect(1023.S)
  }

  // if (Instructions.WORD_SIZE == 16) {
  //   test(new MagicMemory(1024)) { c =>
  //     val dataFill = Array.range(0, 511)

  //     for((data, i) <- dataFill.zipWithIndex)
  //     {
  //       c.io.write.addr.poke((i*2).U)
  //       c.io.write.data.poke(data.U)
  //       c.io.write.we.poke(false.B)
  //       c.clock.step(1)
  //     }

  //     for((data, i) <- dataFill.zipWithIndex)
  //     {
  //       c.io.write.addr.poke((i*2).U)
  //       c.io.read.data.expect(0.U)
  //       c.clock.step(1)
  //     }

  //     for((data, i) <- dataFill.zipWithIndex)
  //     {
  //       c.io.write.addr.poke((i*2).U)
  //       c.io.write.data.poke(data.U)
  //       c.io.write.we.poke(true.B)
  //       c.clock.step(1)
  //       c.io.read.addr.poke((i*2).U)
  //       c.io.read.data.expect(data.U)
  //       c.clock.step(1)
  //     }
  //   }

  //   test(new TestCore) { c =>
  //     /* NOTE: c.io.out.addr is the output to the ALU */
  //     for ((ins, i) <- CoreTestPrograms.program1.zipWithIndex) {
  //       c.io.ins.poke(ins.ins())
  //       println(s"${i}: ${ins.desc()}")
  //       c.io.out.addr.expect(ins.out())
  //       c.clock.step(1)
  //     }

  //     for ((ins, i) <- CoreTestPrograms.program2.zipWithIndex) {
  //       c.io.ins.poke(ins.ins())
  //       println(s"${i}: ${ins.desc()}")
  //       c.io.out.addr.expect(ins.out())
  //       c.clock.step(1)
  //     }

  //     for ((ins, i) <- CoreTestPrograms.program3.zipWithIndex) {
  //       c.io.ins.poke(ins.ins())
  //       println(s"${i}: ${ins.desc()}")
  //       c.io.out.addr.expect(ins.out())
  //       c.clock.step(1)
  //     }

  //     for ((ins, i) <- CoreTestPrograms.program4.zipWithIndex) {
  //       c.io.ins.poke(ins.ins())
  //       println(s"${i}: ${ins.desc()}")
  //       c.io.out.addr.expect(ins.out())
  //       c.clock.step(1)
  //     }

  //     for ((ins, i) <- CoreTestPrograms.program5.zipWithIndex) {
  //       c.io.ins.poke(ins.ins())
  //       println(s"${i}: ${ins.desc()}")
  //       c.io.out.addr.expect(ins.out())
  //       c.clock.step(1)
  //     }

  //     for ((ins, i) <- CoreTestPrograms.program6.zipWithIndex) {
  //       c.io.ins.poke(ins.ins())
  //       println(s"${i}: ${ins.desc()}")
  //       c.io.out.addr.expect(ins.out())
  //       c.clock.step(1)
  //     }
  //   }
  // }

  // test(new TestProcessor) { c =>
  //   /* Program load */
  //   for((ins, i) <- Programs.program_42.zipWithIndex)
  //   {
  //     c.io.mem.addr.poke((i*2).U)
  //     c.io.mem.data.poke(ins.ins())
  //     c.io.mem.we.poke(true.B)
  //     c.clock.step(1)
  //   }

  //   /* Program execution */
  //   c.io.mem.we.poke(false.B)
  //   for((ins, i) <- Programs.program_42.zipWithIndex)
  //   {
  //     c.clock.step(1)
  //   }

  //   /* Program load */
  //   for((ins, i) <- Programs.program_soft_mul.zipWithIndex)
  //   {
  //     c.io.mem.addr.poke((i*2).U)
  //     c.io.mem.data.poke(ins.ins())
  //     c.io.mem.we.poke(true.B)
  //     c.clock.step(1)
  //   }

  //   /* Program execution */
  //   c.io.mem.we.poke(false.B)
  //   for(i <- 0 to 40)
  //   {
  //     c.clock.step(1)
  //   }
  // }

  println("SUCCESS!!")
}