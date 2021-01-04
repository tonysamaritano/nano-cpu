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

  println("SUCCESS!!")
}