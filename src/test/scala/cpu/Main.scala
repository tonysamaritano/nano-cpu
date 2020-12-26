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
      val invalid_ins = "b_1110_1101".U

      val add = "b_0000_0111".U
      val sub = "b_0000_1101".U

      /* Test ALU */
      poke(c.io.ins, add)
      expect(c.io.add, Control.ALU_ADD)
      poke(c.io.ins, sub)
      expect(c.io.add, Control.ALU_SUB)
      poke(c.io.ins, invalid_ins)
      expect(c.io.add, Control.ALU_XXX)
    }
  }
  assert(insResult)

  println("SUCCESS!!")
}