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
      }
      println(s"Print during testing: Input is ${peek(c.io.out)}")
    }
  }
  assert(testResult)   // Scala Code: if testResult == false, will throw an error
  println("SUCCESS!!")

  val counterResult = Driver(() => new Counter(8)) {
    c => new PeekPokeTester(c) {

      // Set Vlaue in counter to 33
      val offset = 33;
      poke(c.io.en, 0)
      poke(c.io.set, 1)
      poke(c.io.in, offset)
      step(1)

      for (i <- 0 until 100)
      {
        poke(c.io.in, 0)
        poke(c.io.set, 0)
        poke(c.io.en, 1)
        expect(c.io.out, i+offset)
        // println(s"Print during testing: Output is ${peek(c.io.out)}")
        step(1)
      }
    }
  }

  /* Prints module to string */
  // val verilogString = (new chisel3.stage.ChiselStage).emitVerilog(new Counter(8))
  // println(verilogString)

  val registerResult = Driver(() => new Register(8)) {
    c => new PeekPokeTester(c) {
      poke(c.io.en, 1)
      poke(c.io.in, 0)
      step(1)

      for (i <- 1 until 100)
      {
        poke(c.io.en, i % 2 == 0)
        poke(c.io.in, i)
        step(1)
        if (i % 2 == 0) {
          expect(c.io.out, i)
        } else {
          expect(c.io.out, i-1)
        }
      }


      poke(c.io.en, 1)
      for (i <- 0 until 255)
      {
        poke(c.io.in, i)
        step(1)
        expect(c.io.out, i)
      }
    }
  }

  // val memResult = Driver(() => new Memory(8, 256)) {
  //   c => new PeekPokeTester(c) {
  //     for (i <- 0 until 100) {
  //       poke(c.io.write, true)
  //       poke(c.io.addr, i)
  //       poke(c.io.dataIn, i)
  //       expect(c.io.data, i)
  //       // println(s"${peek(c.io.addr)} Din: ${peek(c.io.dataIn)} Dout is ${peek(c.io.data)}")
  //       step(1)
  //     }

  //     for (i <- 0 until 100) {
  //       poke(c.io.write, false)
  //       poke(c.io.addr, i)
  //       poke(c.io.dataIn, i+25)
  //       expect(c.io.data, i)
  //       // println(s"${peek(c.io.addr)} Din: ${peek(c.io.dataIn)} Dout is ${peek(c.io.data)}")
  //       step(1)
  //     }
  //   }
  // }

  val programMem = Driver(() => new ProgramMemory(8, 256)) {
    c => new PeekPokeTester(c) {
      for (i <- 0 until 5) {
        poke(c.io.addr, i)
        expect(c.io.data, ProgramMemory.program(i))
        println(s"${peek(c.io.addr)}: ${peek(c.io.data)}")
        step(1)
      }
    }
  }

  val insDecoderTest = Driver(() => new InsDecoder(4, 4)) {
    c => new PeekPokeTester(c) {
      for (i <- 0 until ProgramMemory.program.length) {
        poke(c.io.ins, ProgramMemory.program(i))
        println(s"Opcode: ${peek(c.io.opcode)} Data: ${peek(c.io.data)}")
        step(1)
      }
    }
  }

  val fetchStageTest = Driver(() => new FetchStage(8)) {
    c => new PeekPokeTester(c) {
      for (i <- 0 until 10) {
        /* Load every other ins */
        poke(c.io.halt, i%2==0)
        // expect(c.io.data, ProgramMemory.program(i/2))
        println(s"Ins: ${peek(c.io.data)} Opcode: ${peek(c.io.opcode)}")
        step(1)
      }
    }
  }

  val cpuTest = Driver(() => new CPU(8)) {
    c => new PeekPokeTester(c) {
      poke(c.io.halt, false.B)
      for (i <- 0 until ProgramMemory.program.length) {
        println(s"Result: ${peek(c.io.res)}")
        step(1)
      }
    }
  }

  /* Prints module to string */
  val verilogString = (new chisel3.stage.ChiselStage).emitVerilog(new CPU(8))
  // println(verilogString)
}