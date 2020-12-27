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
  /* New Tester Framework */
  test(new PassthroughGenerator(8)) { c =>
    for (i <- 0 until 100) {
      c.io.in.poke(i.U)     // Set our input to value 0
      c.io.out.expect(i.U)  // Assert that the output correctly has 0
      c.clock.step(1)
      // println(s"Print during testing: Input is ${c.io.out.peek()}")
    }
  }

  val rnd = scala.util.Random;

  /* Ths instruction does not exist in the ISA */
  val invalid_ins = "b_1110_1101".U

  /* Arithmetic Operations */
  val add  = "b_0000_0011".U
  val sub  = "b_0000_1001".U

  test(new ControlSignals(8)) { c=>
    /* Test ADD */
    c.io.ins.poke(add)
    c.io.alu.expect(Control.ALU_ADD)
    c.io.bus.expect(Control.BUS_ALU_OUT)
    c.io.reg.expect(Control.REG_A)
    c.io.flg.expect(Control.FLG_STORE_ZC)

    /* Test SUB */
    c.io.ins.poke(sub)
    c.io.alu.expect(Control.ALU_SUB)
    c.io.bus.expect(Control.BUS_ALU_OUT)
    c.io.reg.expect(Control.REG_A)
    c.io.flg.expect(Control.FLG_STORE_ZC)

    /* Test Invalid */
    c.io.ins.poke(invalid_ins)
    c.io.alu.expect(Control.ALU_XXX)
    c.io.bus.expect(Control.BUS_XXX)
    c.io.reg.expect(Control.REG_NONE)
    c.io.flg.expect(Control.FLG_XXX)
  }

  test(new TestALU(8)) { c=>
    c.io.ins.poke(add)

    /* Test ALU Int Addition No Carry */
    var x = rnd.nextInt(127)
    var y = rnd.nextInt(128)
    c.io.inA.poke(x.U)
    c.io.inB.poke(y.U)
    c.io.out.expect((x+y).U)
    c.io.zero.expect(false.B)
    c.io.carry.expect(false.B)

    /* Test ALU Int Addition Carry */
    x = rnd.nextInt(127) + 128
    y = rnd.nextInt(128) + 128
    c.io.inA.poke(x.U)
    c.io.inB.poke(y.U)
    c.io.out.expect((x+y-256).U)
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
    x = 128
    y = rnd.nextInt(127)
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
    y = rnd.nextInt(255) + 1
    c.io.inA.poke(x.U)
    c.io.inB.poke(y.U)
    c.io.out.expect((x-y+256).U)
    c.io.zero.expect(false.B)
    c.io.carry.expect(true.B)
  }

  test(new RegisterFile(8)) { c=>
    var x = rnd.nextInt(255)
    var y = rnd.nextInt(255)
    var z = rnd.nextInt(255)

    /* Write random value to both A and B */
    c.io.ctl.poke(Control.REG_AB)
    c.io.data.poke(x.U)
    c.clock.step(1)
    c.io.outA.expect(x.U)
    c.io.outB.expect(x.U)

    /* Only change A */
    c.io.ctl.poke(Control.REG_A)
    c.io.data.poke(y.U)
    c.io.outA.expect(x.U)
    c.clock.step(1)
    c.io.outA.expect(y.U)
    c.io.outB.expect(x.U)

    /* Only change B */
    c.io.ctl.poke(Control.REG_B)
    c.io.data.poke(z.U)
    c.io.outB.expect(x.U)
    c.clock.step(1)
    c.io.outA.expect(y.U)
    c.io.outB.expect(z.U)

    /* Make sure none doesn't enable memory */
    c.io.ctl.poke(Control.REG_NONE)
    c.io.data.poke(x.U)
    c.clock.step(1)
    c.io.outA.expect(y.U)
    c.io.outB.expect(z.U)
  }

  println("SUCCESS!!")
}