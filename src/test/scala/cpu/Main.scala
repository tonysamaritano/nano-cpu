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

  test(new ControlSignals(archWidth)) { c=>
    /* Test ADD */
    c.io.ins.poke(add)
    c.io.alu.expect(Control.ALU_ADD)
    c.io.bus.expect(Control.BUS_ALU_OUT)
    c.io.flg.expect(Control.FLG_STORE_ZC)
    c.io.reg.expect(Control.REG_WB_EN)
    c.io.dst.expect("b_011".U)
    c.io.src0.expect("b_110".U)
    c.io.src1.expect("b_111".U)

    /* Test SUB */
    c.io.ins.poke(sub)
    c.io.alu.expect(Control.ALU_SUB)
    c.io.bus.expect(Control.BUS_ALU_OUT)
    c.io.flg.expect(Control.FLG_STORE_ZC)
    c.io.reg.expect(Control.REG_WB_EN)
    c.io.dst.expect("b_011".U)
    c.io.src0.expect("b_111".U)
    c.io.src1.expect("b_110".U)

    /* Test Invalid */
    c.io.ins.poke(invalid_ins)
    c.io.alu.expect(Control.ALU_XXX)
    c.io.bus.expect(Control.BUS_XXX)
    c.io.flg.expect(Control.FLG_XXX)
    c.io.reg.expect(Control.REG_WB_OFF)
    c.io.dst.expect(0.U)
    c.io.src0.expect(0.U)
    c.io.src1.expect(0.U)
  }

  test(new TestALU(archWidth)) { c=>
    c.io.ins.poke(add)

    /* Test ALU Int Addition No Carry */
    var x = rnd.nextInt(32767)
    var y = rnd.nextInt(32768)
    c.io.inA.poke(x.U)
    c.io.inB.poke(y.U)
    c.io.out.expect((x+y).U)
    c.io.zero.expect(false.B)
    c.io.carry.expect(false.B)

    /* Test ALU Int Addition Carry */
    x = rnd.nextInt(32768) + 32768
    y = rnd.nextInt(32768) + 32768
    c.io.inA.poke(x.U)
    c.io.inB.poke(y.U)
    c.io.out.expect((x+y-65536).U)
    c.io.zero.expect(false.B)
    c.io.carry.expect(true.B)

    /* Test ALU Int Addition Zero */
    c.io.inA.poke(0.U)
    c.io.inB.poke(0.U)
    c.io.out.expect(0.U)
    c.io.zero.expect(true.B)
    c.io.carry.expect(false.B)

    c.io.ins.poke(sub)

    /* Test ALU Int Subtraction No Carry */
    x = 32768
    y = rnd.nextInt(32767)
    c.io.inA.poke(x.U)
    c.io.inB.poke(y.U)
    c.io.out.expect((x-y).U)
    c.io.zero.expect(false.B)
    c.io.carry.expect(false.B)

    /* Sub Zero */
    c.io.inA.poke(1.U)
    c.io.inB.poke(1.U)
    c.io.out.expect(0.U)
    c.io.zero.expect(true.B)
    c.io.carry.expect(false.B)

    /* Sub Borrow */
    x = 0
    y = rnd.nextInt(65535) + 1
    c.io.inA.poke(x.U)
    c.io.inB.poke(y.U)
    c.io.out.expect((x-y+65536).U)
    c.io.zero.expect(false.B)
    c.io.carry.expect(true.B)
  }

  test(new RegisterFile(Instructions.REGFILE_SIZE, archWidth)) { c=>
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
    c.io.out0.expect(x.U)
    c.io.out1.expect(y.U)
    c.io.addr0.poke(a0)
    c.io.out0.expect(z.U)
    c.clock.step(1)

    /* Verify output is still the same after clock */
    c.io.out0.expect(z.U)
    c.io.out1.expect(y.U)

    /* Verify x0 is always zero even after on data write */
    c.io.data.poke(x.U)
    c.io.dst.poke(x0)
    c.io.dst_en.poke(true.B)
    c.clock.step(1)
    c.io.addr0.poke(x0)
    c.io.out0.expect(0.U)
  }

  test(new TestIntegration(archWidth, Instructions.REGFILE_SIZE)) { c=>
    /* Write some data into the registers */
    c.io.we.poke(true.B)
    for (i <- 1 until Instructions.REGFILE_SIZE)
    {
      c.io.data.poke(i.U)
      c.io.addr.poke(i.U)
      c.clock.step(1)
    }
    c.io.we.poke(false.B)

    /* Verify that we've added data to the Registers correctly
     *
     * Add 0 + x1 into x0. Should evalute to 1 on the bus */
    for (i <- 1 until Instructions.REGFILE_SIZE) {
      // val addzero = "b_0000_0100_0000_0000".U
      var ins = (i << 10)
      c.io.ins.poke(ins.U)
      c.clock.step(1)
      c.io.out.expect(i.U)
    }

    /* Verify that the addition instruction works */
    c.io.ins.poke(add)
    c.clock.step(1)
    c.io.out.expect(13.U) // x6 + x7 = 13

    /* Check that it was written back */
    var ins = 3 << 10
    c.io.ins.poke(ins.U)
    c.clock.step(1)
    c.io.out.expect(13.U) // x3 = 13

    /* Verify that the addition instruction works */
    c.io.ins.poke(sub)
    c.clock.step(1)
    c.io.out.expect(1.U) // x7 - x6 = 1

    /* Check that it was written back */
    c.io.ins.poke(ins.U)
    c.clock.step(1)
    c.io.out.expect(1.U) // x3 = 1

    /* Verify that the addition instruction works */
    c.io.ins.poke(addi)
    c.clock.step(1)
    c.io.out.expect(12.U) // x6 + 7 = 13

    /* Check that it was written back */
    c.io.ins.poke(ins.U)
    c.clock.step(1)
    c.io.out.expect(12.U) // x3 = 13
  }

  test(new ImmGen(archWidth)) { c =>
    c.io.ins.poke(addi)
    c.io.ctr.poke(Control.IMM_3BR2)
    c.io.imm.expect(6.U(archWidth.W))

    c.io.ins.poke(ldi)
    c.io.ctr.poke(Control.IMM_8BR2)
    c.io.imm.expect("h_a5".U(archWidth.W))
  }

  test(new ImmGen2) { c =>
    c.io.ins.poke(addi2)
    c.io.ctl.poke(Control2.IMM_RI)
    c.io.imm.expect("b_1111".U(Instructions2.WORD_SIZE.W))

    /* Add ins is using src1 = 001 and func1 = 1, so those are Cat'ed
     * to form 3 */
    c.io.ins.poke(add2)
    c.io.ctl.poke(Control2.IMM_RI)
    c.io.imm.expect("b_0011".U(Instructions2.WORD_SIZE.W))

    /* Load word uses 5 bits for the immediate */
    c.io.ins.poke(lw2)
    c.io.ctl.poke(Control2.IMM_I5)
    c.io.imm.expect("b_1_1111".U(Instructions2.WORD_SIZE.W))

    /* Load lower imm uses 8 bits for the immediate */
    c.io.ins.poke(lli2)
    c.io.ctl.poke(Control2.IMM_U)
    c.io.imm.expect("b_1111_1111".U(Instructions2.WORD_SIZE.W))

    /* Store word uses both srcX registers */
    c.io.ins.poke(sw2)
    c.io.ctl.poke(Control2.IMM_S)
    c.io.imm.expect("b_1_1111".U(Instructions2.WORD_SIZE.W))
  }

  test(new ALU2) { c =>
    /* Add */
    c.io.ctl.poke(Control2.ALU_ADD)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(4321.U)
    c.io.out.expect(5555.U)

    /* Sub */
    c.io.ctl.poke(Control2.ALU_SUB)
    c.io.src0.poke(4321.U)
    c.io.src1.poke(1234.U)
    c.io.out.expect(3087.U)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(4321.U)
    c.io.out.expect("b_1111_0011_1111_0001".U) /* 2's compliment */

    /* And */
    c.io.ctl.poke(Control2.ALU_AND)
    c.io.src0.poke("b_1111_1111".U)
    c.io.src1.poke("b_0000_1111".U)
    c.io.out.expect("b_1111".U)

    /* Or */
    c.io.ctl.poke(Control2.ALU_OR)
    c.io.src0.poke("b_1010_1010".U)
    c.io.src1.poke("b_0101_0101".U)
    c.io.out.expect("b_1111_1111".U)

    /* Xor */
    c.io.ctl.poke(Control2.ALU_XOR)
    c.io.src0.poke("b_0010_1011".U)
    c.io.src1.poke("b_0001_0111".U)
    c.io.out.expect("b_0011_1100".U)

    /* Not */
    c.io.ctl.poke(Control2.ALU_NOT)
    c.io.src0.poke( "b_1010_1010_1010_1010".U)
    c.io.out.expect("b_0101_0101_0101_0101".U)

    /* Shift Left */
    c.io.ctl.poke(Control2.ALU_SLL)
    c.io.src0.poke("b_0010_1011".U)
    c.io.src1.poke(1.U)
    c.io.out.expect("b_0101_0110".U)
    c.io.src1.poke(2.U)
    c.io.out.expect("b_1010_1100".U)

    /* Shift Right */
    c.io.ctl.poke(Control2.ALU_SRL)
    c.io.src0.poke("b_0010_1011".U)
    c.io.src1.poke(1.U)
    c.io.out.expect("b_001_0101".U)
    c.io.src1.poke(2.U)
    c.io.out.expect("b_00_1010".U)

    /* EQ */
    c.io.ctl.poke(Control2.ALU_EQ)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.flags.expect(1.U)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1235.U)
    c.io.flags.expect(0.U)

    /* GE */
    c.io.ctl.poke(Control2.ALU_GEU)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.flags.expect(1.U)
    c.io.src0.poke(1235.U)
    c.io.src1.poke(1234.U)
    c.io.flags.expect(2.U)
    c.io.src0.poke(1233.U)
    c.io.src1.poke(1234.U)
    c.io.flags.expect(0.U)
    c.io.ctl.poke(Control2.ALU_GE)
    c.io.src0.poke(1234.U)
    c.io.src1.poke(1234.U)
    c.io.flags.expect(1.U)
    c.io.src0.poke(1235.U)
    c.io.src1.poke(1234.U)
    c.io.flags.expect(2.U)
    c.io.src0.poke("b_1111_1011_0010_1111".U)
    c.io.src1.poke("b_1111_1011_0010_1110".U)
    c.io.flags.expect(2.U)
    c.io.src0.poke("b_0111_1011_0010_1111".U)
    c.io.src1.poke("b_1111_1011_0010_1110".U)
    c.io.flags.expect(2.U)
    c.io.src0.poke("b_1111_1011_0010_1110".U)
    c.io.src1.poke("b_1111_1011_0010_1111".U)
    c.io.flags.expect(0.U)
    c.io.src0.poke("b_1111_1011_0010_1110".U)
    c.io.src1.poke("b_0111_1011_0010_1111".U)
    c.io.flags.expect(0.U)
  }

  test(new TestIntegration2) { c =>
    /* Add immediate (15) and x0 to x1 to store some value */
    var ins = "b_0011_1110_0000_1000".U /* Adds contents of x2 and imm (15) into x1 */
    c.io.ins.poke(ins)
    c.io.out.expect("b_1111".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Adds contents of x0 and x1 into x0 to check output of x1 */
    ins = "b_0000_0000_0100_0000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_1111".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Shift x1 by 4 to increase the value by 2^4 and store back in x1 */
    ins = "b_1010_1000_0100_1001".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_1111_0000".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Add 1 to x1 and store in x2*/
    ins = "b_0010_0010_0101_0000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_1111_0001".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Adds contents of x0 and x2 into x0 to check output of x2 */
    ins = "b_0000_0000_1000_0000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_1111_0001".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Add x1 and x2 and store in x3 */
    ins = "b_0000_1000_0101_1000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b1_1110_0001".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Adds contents of x0 and x3 into x0 to check output of x3 */
    ins = "b_0000_0000_1100_0000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b1_1110_0001".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Divide x1 by 2 using a right shift and store in x4 */
    ins = "b_1100_0000_0110_0001".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_111_1000".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Adds contents of x0 and x4 into x0 to check output of x4 */
    ins = "b_0000_0001_0000_0000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_111_1000".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Subtract x3 by x4 and store result in x5 */
    ins = "b_0101_0000_1110_1000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_1_0110_1001".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Subtract x4 by x3 and store result in x6 */
    ins = "b_0100_1101_0011_0000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_1111_1110_1001_0111".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)

    /* Adds contents of x0 and x6 into x0 to check output of x6 */
    ins = "b_0000_0001_1000_0000".U
    c.io.ins.poke(ins)
    c.io.out.expect("b_1111_1110_1001_0111".U(Instructions2.WORD_SIZE.W))
    c.clock.step(1)
  }

  println("SUCCESS!!")
}