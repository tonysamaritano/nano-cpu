package cpu

import chisel3._
import chisel3.experimental._

class PortControl(width : Int) extends Bundle {
  val outputValue = UInt(width.W)
  val outputEn    = Bool()
  val inputEn     = Bool()
}

abstract class Port(width : Int) extends Bundle {
  val i = new Bundle {
    val value = Input(UInt(width.W))
  }
  val o: PortControl

  def default(): Unit
  def inputPort(): UInt
  def outputPort(signal : Int): Unit
}

class BasePort(width : Int) extends Port(width) {
  val o = Output(new PortControl(width))

  def default(): Unit = {
    this.o.outputValue := 0.U
    this.o.outputEn    := false.B
    this.o.inputEn     := false.B
  }

  def inputPort(): UInt = {
    this.o.outputValue := 0.U
    this.o.outputEn    := false.B
    this.o.inputEn     := true.B
    this.i.value
  }

  def outputPort(signal : Int): Unit = {
    this.o.outputValue := signal.U
    this.o.outputEn    := true.B
    this.o.inputEn     := false.B
  }
}

object BasePortToIOF {
  def apply(port: BasePort, iof: BasePort): Unit = {
    iof <> port
  }
}

object InputPortToIOF {
  def apply(width : Int, iof: BasePort): UInt = {
    val port = Wire(new BasePort(width))
    BasePortToIOF(port, iof)
    port.inputPort()
  }
}

object OutputPortToIOF {
  def apply(width : Int, iof: BasePort, value: Int): Unit = {
    val port = Wire(new BasePort(width))
    port.outputPort(value)
    BasePortToIOF(port, iof)
  }
}

/* I geniuine don't know what I'm doing here. */
class PortModule(width : Int) extends Module {
  val io = IO(new Bundle {
    val outputEn = Input(Bool())
    val port = Analog(width.W)
  })

  val inValue = Wire(UInt(width.W))
  val value = RegInit(0.U(width.W))
}