package cpu

import chisel3._

object main extends App {
  chisel3.Driver.execute(args, () => new CPU(8))
}