package com.purplekingdomgames.indigo.runtime

/**
  * This is the dumbest logger ever.
  * We're just logging to the Browsers console, I just wanted a few standard headers with the message.
  */
object Logger {

  private val INFO: String  = "INFO"
  private val ERROR: String = "ERROR"
  private val DEBUG: String = "DEBUG"

  private val errorLogs: scalajs.js.Array[String] = new scalajs.js.Array[String]()
  private val debugLogs: scalajs.js.Array[String] = new scalajs.js.Array[String]()

  private def formatMessage(level: String, message: String): String =
    s"""[${System.currentTimeMillis()}] [$level] [Indigo] $message"""

  private val infoString: String => Unit = message => println(formatMessage(INFO, message))

  private val errorString: String => Unit = message => println(formatMessage(ERROR, message))

  private val errorOnceString: String => Unit = message =>
    if (!errorLogs.iterator.contains(message)) {
      errorLogs.push(message)
      println(formatMessage(ERROR, message))
  }

  private val debugString: String => Unit = message => println(formatMessage(DEBUG, message))

  private val debugOnceString: String => Unit = message =>
    if (!debugLogs.iterator.contains(message)) {
      debugLogs.push(message)
      println(formatMessage(DEBUG, message))
  }

  def info[A](value: A)(implicit show: Show[A]): Unit =
    infoString(show.show(value))

  def error[A](value: A)(implicit show: Show[A]): Unit =
    errorString(show.show(value))

  def errorOnce[A](value: A)(implicit show: Show[A]): Unit =
    errorOnceString(show.show(value))

  def debug[A](value: A)(implicit show: Show[A]): Unit =
    debugString(show.show(value))

  def debugOnce[A](value: A)(implicit show: Show[A]): Unit =
    debugOnceString(show.show(value))

}
