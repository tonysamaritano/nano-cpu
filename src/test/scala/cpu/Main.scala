// See README.md for license details.

package cpu

import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

/**
  * This provides an alternate way to run tests, by executing then as a main
  * From sbt (Note: the test: prefix is because this main is under the test package hierarchy):
  * {{{
  * test:runMain cpu.main
  * }}}
  */
object main extends App {
  val testResult = Driver(() => new PassthroughGenerator(8)) {
    c => new PeekPokeTester(c) {
      for (i <- 0 until 100) {
        poke(c.io.in, i)     // Set our input to value 0
        expect(c.io.out, i)  // Assert that the output correctly has 0
        // println(s"Print during testing: Input is ${peek(c.io.out)}")
      }
    }
  }
  assert(testResult)   // Scala Code: if testResult == false, will throw an error

  val insResult = Driver(() => new ControlSignals(8)) {
    c => new PeekPokeTester(c) {
      /* Ths instruction does not exist in the ISA */
      val invalid_ins = "b_1110_1101".U

      /* Arithmetic Operations */
      val add  = "b_0000_0011".U
      val sub  = "b_0000_1001".U

      /* Test ALU */
      poke(c.io.ins, add)
      expect(c.io.alu, Control.ALU_ADD)
      poke(c.io.ins, sub)
      expect(c.io.alu, Control.ALU_SUB)
      poke(c.io.ins, invalid_ins)
      expect(c.io.alu, Control.ALU_XXX)
    }
  }
  assert(insResult)

  val aluResult = Driver(() => new TestALU(8)) {
    c => new PeekPokeTester(c) {
      /* Ths instruction does not exist in the ISA */
      val invalid_ins = "b_1110_1101".U

      /* Arithmetic Operations */
      val add  = "b_0000_0011".U
      val sub  = "b_0000_1001".U

      /* Test ALU Int Addition No Carry */
      var x = rnd.nextInt(127)
      var y = rnd.nextInt(128)
      poke(c.io.ins, add)
      poke(c.io.inA, x)
      poke(c.io.inB, y)
      expect(c.io.out, x+y)
      expect(c.io.zero, 0)
      expect(c.io.carry, 0)

      /* Test ALU Int Addition Zero */
      poke(c.io.ins, add)
      poke(c.io.inA, 0)
      poke(c.io.inB, 0)
      expect(c.io.out, 0)
      expect(c.io.zero, 1)
      expect(c.io.carry, 0)

      /* Test ALU Int Addition Carry */
      x = rnd.nextInt(127) + 128
      y = rnd.nextInt(128) + 127
      poke(c.io.ins, add)
      poke(c.io.inA, x)
      poke(c.io.inB, y)
      expect(c.io.out, x+y - 256)
      expect(c.io.zero, 0)
      expect(c.io.carry, 1)

      /* Test ALU Int Subtraction */
      x = 255
      y = rnd.nextInt(254)
      poke(c.io.ins, sub)
      poke(c.io.inA, x)
      poke(c.io.inB, y)
      expect(c.io.out, x-y)
      expect(c.io.zero, 0)
      expect(c.io.carry, 0)

      /* Test ALU Int Subtraction Zero */
      poke(c.io.ins, sub)
      poke(c.io.inA, 1)
      poke(c.io.inB, 1)
      expect(c.io.out, 0)
      expect(c.io.zero, 1)
      expect(c.io.carry, 0)

      /* Test ALU Int Subtraction Borrow */
      x = 0
      y = rnd.nextInt(254) + 1
      poke(c.io.ins, sub)
      poke(c.io.inA, x)
      poke(c.io.inB, y)
      expect(c.io.out, x-y + 256)
      expect(c.io.zero, 0)
      expect(c.io.carry, 1)
    }
  }
  assert(aluResult)

  println("SUCCESS!!")
}