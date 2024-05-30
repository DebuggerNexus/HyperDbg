/**
 * @file
 *   send_error.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Send an indication of invalid packet (in interpreter)
 * @details
 * @version 0.1
 * @date
 *   2024-05-04
 *
 * @copyright
 *   This project is released under the GNU Public License v3.
 */
package hwdbg.communication.interpreter

import chisel3._
import chisel3.util.{switch, is}
import circt.stage.ChiselStage

import hwdbg.configs._

class InterpreterSendError(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    bramDataWidth: Int
) extends Module {

  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal
    val lastError = Input(UInt(bramDataWidth.W)) // input last error

    //
    // Sending singals
    //
    val noNewDataSender = Output(Bool()) // should sender finish sending buffers or not?
    val dataValidOutput = Output(Bool()) // should sender send next buffer or not?
    val sendingData = Output(UInt(bramDataWidth.W)) // data to be sent to the debugger

  })

  //
  // Output pins
  //
  val noNewDataSender = WireInit(false.B)
  val dataValidOutput = WireInit(false.B)
  val sendingData = WireInit(0.U(bramDataWidth.W))

  //
  // Apply the chip enable signal
  //
  when(io.en === true.B) {

    //
    // Set the version
    //
    sendingData := io.lastError

    //
    // Sending the version in one clock cycle
    //
    noNewDataSender := true.B
    dataValidOutput := true.B

  }

  // ---------------------------------------------------------------------

  //
  // Connect output pins
  //
  io.noNewDataSender := noNewDataSender
  io.dataValidOutput := dataValidOutput
  io.sendingData := sendingData

}

object InterpreterSendError {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      bramDataWidth: Int
  )(
      en: Bool,
      lastError: UInt
  ): (Bool, Bool, UInt) = {

    val interpreterSendError = Module(
      new InterpreterSendError(
        debug,
        bramDataWidth
      )
    )

    val noNewDataSender = Wire(Bool())
    val dataValidOutput = Wire(Bool())
    val sendingData = Wire(UInt(bramDataWidth.W))

    //
    // Configure the input signals
    //
    interpreterSendError.io.en := en
    interpreterSendError.io.lastError := lastError

    //
    // Configure the output signals
    //
    noNewDataSender := interpreterSendError.io.noNewDataSender
    dataValidOutput := interpreterSendError.io.dataValidOutput
    sendingData := interpreterSendError.io.sendingData

    //
    // Return the output result
    //
    (
      noNewDataSender,
      dataValidOutput,
      sendingData
    )
  }
}
