package com.purplekingdomgames.indigo.util

/**
  * This is the dumbest logger ever.
  * We're just logging to the Browsers console, I just wanted a few standard headers with the message.
  */
object Logger {

  private val INFO: String = "INFO"
  private val ERROR: String = "ERROR"

  private val errorLogs: scalajs.js.Array[String] = new scalajs.js.Array[String]()

  private def formatMessage(level: String, message: String): String =
    s"""[${System.currentTimeMillis()}] [$level] [Indigo] $message"""

  val info: String => Unit = message =>
    println(formatMessage(INFO, message))

  val error: String => Unit = message =>
    println(formatMessage(ERROR, message))

  val errorOnce: String => Unit = message =>
    if(!errorLogs.iterator.contains(message)) {
      errorLogs.push(message)
      println(formatMessage(ERROR, message))
    }

}
