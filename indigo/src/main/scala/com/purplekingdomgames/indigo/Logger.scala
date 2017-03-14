package com.purplekingdomgames.indigo

/**
  * This is the dumbest logger ever.
  * We're just logging to the Browsers console, I just wanted a few standard headers with the message.
  */
object Logger {

  private val formatMessage: String => String = message =>
    s"""[${System.currentTimeMillis()}] [Indigo] $message"""

  val info: String => Unit = message =>
    println(formatMessage(message))

}
