// See README.md for license details.

package cpu

import chisel3._
import chisel3.tester._
import chisel3.tester.RawTester.test

/**
  * This provides an alternate way to run tests, by executing then as a main
  * From sbt (Note: the test: prefix is because this main is under the test package hierarchy):
  * {{{
  * test:runMain cpu.main
  * }}}
  */
object main extends App {
  def archWidth = 16
  val rnd = scala.util.Random;

  /* Ths instruction does not exist in the ISA */
  val invalid_ins = "b_0101_0101_1110_1101".U

  /* Arithmetic Operations */
  val add  = "b_1111_1001_1000_0000".U /* Adds contents of x6 and x7 into x3 */
  val addi = "b_1101_1001_1000_1000".U /* Adds contents of x6 and immidiate (6) */
  val sub  = "b_1101_1101_1001_0000".U /* Subtracts contents of x7 and x6 into x3 */
  val ldi  = "b_1010_0101_0010_0001".U /* Loads 0xa5 into register x1 */

  /* Ins2 */
  val add2  = "b_0000_0110_1010_0000".U /* Adds contents of x2 and x1 into x6, funct1 set to 1*/
  val addi2 = "b_0011_1110_1000_1000".U /* Adds contents of x2 and imm (15) into x1 */
  val lw2   = "b_1011_1110_1000_1010".U /* Loads word at imm(x2) into x1 */
  val lli2  = "b_1101_1111_1100_1010".U /* Loads lower 8-bit imm into x1 */
  val sw2   = "b_1100_1010_0111_1011".U /* Stores word in x1 into imm(x2) */

  /* Tests */
  test(new PassthroughGenerator(8)) { c =>
    for (i <- 0 until 100) {
      c.io.in.poke(i.U)     // Set our input to value 0
      c.io.out.expect(i.U)  // Assert that the output correctly has 0
      c.clock.step(1)
      // println(s"Print during testing: Input is ${c.io.out.peek()}")
    }
  }

  test(new RegisterFile) { c=>
    /* Registers */
    val x0 = 0.U
    val t0 = 3.U
    val t1 = 4.U
    val a0 = 7.U

    /* Data to store */
    var x = rnd.nextInt(65535)
    var y = rnd.nextInt(65535)
    var z = rnd.nextInt(65535)

    /* Write random value to register */
    c.io.data.poke(x.U)
    c.io.dst.poke(t0)
    c.io.dst_en.poke(true.B)
    c.clock.step(1)
    c.io.data.poke(y.U)
    c.io.dst.poke(t1)
    c.io.dst_en.poke(true.B)
    c.clock.step(1)
    c.io.data.poke(z.U)
    c.io.dst.poke(a0)
    c.io.dst_en.poke(true.B)
    c.clock.step(1)

    /* Check Registers */
    c.io.dst_en.poke(false.B)
    c.io.dst.poke(t0)
    c.io.data.poke(z.U)
    c.io.addr0.poke(t0)
    c.io.addr1.poke(t1)
    c.io.out.src0.expect(x.U)
    c.io.out.src1.expect(y.U)
    c.io.addr0.poke(a0)
    c.io.out.src0.expect(z.U)
    c.clock.step(1)

    /* Verify output is still the same after clock */
    c.io.out.src0.expect(z.U)
    c.io.out.src1.expect(y.U)

    /* Verify x0 is always zero even after on data write */
    c.io.data.poke(x.U)
    c.io.dst.poke(x0)
    c.io.dst_en.poke(true.B)
    c.clock.step(1)
    c.io.addr0.poke(x0)
    c.io.out.src0.expect(0.U)
  }

  test(new ALU) { c =>
    /* Add */
    c.io.ctl.poke(Control.ALU_ADD)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(4321.U)
    c.io.data.out.expect(5555.U)

    /* Sub */
    c.io.ctl.poke(Control.ALU_SUB)
    c.io.src0.poke(4321.U)
    c.io.src1.poke(1234.U)
    c.io.data.out.expect(3087.U)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(4321.U)
    c.io.data.out.expect("b_1111_0011_1111_0001".U) /* 2's compliment */

    /* And */
    c.io.ctl.poke(Control.ALU_AND)
    c.io.src0.poke("b_1111_1111".U)
    c.io.src1.poke("b_0000_1111".U)
    c.io.data.out.expect("b_1111".U)

    /* Or */
    c.io.ctl.poke(Control.ALU_OR)
    c.io.src0.poke("b_1010_1010".U)
    c.io.src1.poke("b_0101_0101".U)
    c.io.data.out.expect("b_1111_1111".U)

    /* Xor */
    c.io.ctl.poke(Control.ALU_XOR)
    c.io.src0.poke("b_0010_1011".U)
    c.io.src1.poke("b_0001_0111".U)
    c.io.data.out.expect("b_0011_1100".U)

    /* Not */
    c.io.ctl.poke(Control.ALU_NOT)
    c.io.src0.poke( "b_1010_1010_1010_1010".U)
    c.io.data.out.expect("b_0101_0101_0101_0101".U)

    /* Shift Left */
    c.io.ctl.poke(Control.ALU_SLL)
    c.io.src0.poke("b_0010_1011".U)
    c.io.src1.poke(1.U)
    c.io.data.out.expect("b_0101_0110".U)
    c.io.src1.poke(2.U)
    c.io.data.out.expect("b_1010_1100".U)

    /* Shift Right */
    c.io.ctl.poke(Control.ALU_SRL)
    c.io.src0.poke("b_0010_1011".U)
    c.io.src1.poke(1.U)
    c.io.data.out.expect("b_001_0101".U)
    c.io.src1.poke(2.U)
    c.io.data.out.expect("b_00_1010".U)

    /* EQ */
    c.io.ctl.poke(Control.ALU_EQ)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(true.B)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1235.U)
    c.io.data.br.expect(false.B)

    /* NE */
    c.io.ctl.poke(Control.ALU_NE)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(false.B)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1235.U)
    c.io.data.br.expect(true.B)

    /* GE */
    c.io.ctl.poke(Control.ALU_GEU)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(true.B)
    c.io.src0.poke(1235.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(true.B)
    c.io.src0.poke(1233.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(false.B)
    c.io.ctl.poke(Control.ALU_GE)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(true.B)
    c.io.src0.poke(1235.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(true.B)
    c.io.src0.poke("b_1111_1011_0010_1111".U)
    c.io.src1.poke("b_1111_1011_0010_1110".U)
    c.io.data.br.expect(true.B)
    c.io.src0.poke("b_0111_1011_0010_1111".U)
    c.io.src1.poke("b_1111_1011_0010_1110".U)
    c.io.data.br.expect(true.B)
    c.io.src0.poke("b_1111_1011_0010_1110".U)
    c.io.src1.poke("b_1111_1011_0010_1111".U)
    c.io.data.br.expect(false.B)
    c.io.src0.poke("b_1111_1011_0010_1110".U)
    c.io.src1.poke("b_0111_1011_0010_1111".U)
    c.io.data.br.expect(false.B)

    /* LT */
    c.io.ctl.poke(Control.ALU_LTU)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(false.B)
    c.io.src0.poke(1235.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(false.B)
    c.io.src0.poke(1233.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(true.B)
    c.io.ctl.poke(Control.ALU_LT)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(false.B)
    c.io.src0.poke(1235.U)
    c.io.src1.poke(1234.U)
    c.io.data.br.expect(false.B)
    c.io.src0.poke("b_1111_1011_0010_1111".U)
    c.io.src1.poke("b_1111_1011_0010_1110".U)
    c.io.data.br.expect(false.B)
    c.io.src0.poke("b_0111_1011_0010_1111".U)
    c.io.src1.poke("b_1111_1011_0010_1110".U)
    c.io.data.br.expect(false.B)
    c.io.src0.poke("b_1111_1011_0010_1110".U)
    c.io.src1.poke("b_1111_1011_0010_1111".U)
    c.io.data.br.expect(true.B)
    c.io.src0.poke("b_1111_1011_0010_1110".U)
    c.io.src1.poke("b_0111_1011_0010_1111".U)
    c.io.data.br.expect(true.B)
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

  test(new TestCore) { c =>
    for ((ins, i) <- TestPrograms.program1.zipWithIndex) {
      c.io.ins.poke(ins.ins())
      println(s"${i}: ${ins.desc()} = {${ins.out()} === ${c.io.out.peek()}}")
      c.io.out.expect(ins.out())
      c.clock.step(1)
    }

    for ((ins, i) <- TestPrograms.program2.zipWithIndex) {
      c.io.ins.poke(ins.ins())
      println(s"${i}: ${ins.desc()} = {${ins.out()} === ${c.io.out.peek()}}")
      c.io.out.expect(ins.out())
      c.clock.step(1)
    }

    for ((ins, i) <- TestPrograms.program3.zipWithIndex) {
      c.io.ins.poke(ins.ins())
      println(s"${i}: ${ins.desc()} = {${ins.out()} === ${c.io.out.peek()}}")
      c.io.out.expect(ins.out())
      c.clock.step(1)
    }
  }

  println("SUCCESS!!")
}