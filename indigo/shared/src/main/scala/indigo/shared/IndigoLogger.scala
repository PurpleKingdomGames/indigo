package indigo.shared

import scala.collection.mutable.ArrayBuffer

/**
  * A very, very simple logger that logs to the Browsers console with a few standard headers and the log message.
  */
object IndigoLogger {

  private val INFO: String  = "INFO"
  private val ERROR: String = "ERROR"
  private val DEBUG: String = "DEBUG"

  private val errorLogs: ArrayBuffer[String] = new ArrayBuffer[String]()
  private val debugLogs: ArrayBuffer[String] = new ArrayBuffer[String]()

  private def formatMessage(level: String, message: String): String =
    s"""[$level] [Indigo] $message"""

  private val consoleLogString: String => Unit = message => println(message)

  private val infoString: String => Unit = message => println(formatMessage(INFO, message))

  private val errorString: String => Unit = message => println(formatMessage(ERROR, message))

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private val errorOnceString: String => Unit = message =>
    if (!errorLogs.contains(message)) {
      errorLogs += message
      println(formatMessage(ERROR, message))
    }

  private val debugString: String => Unit = message => println(formatMessage(DEBUG, message))

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private val debugOnceString: String => Unit = message =>
    if (!debugLogs.contains(message)) {
      debugLogs += message
      println(formatMessage(DEBUG, message))
    }

  def consoleLog(messages: String*): Unit =
    consoleLogString(messages.toList.mkString(", "))

  def info(messages: String*): Unit =
    infoString(messages.toList.mkString(", "))

  def error(messages: String*): Unit =
    errorString(messages.toList.mkString(", "))

  def errorOnce(messages: String*): Unit =
    errorOnceString(messages.toList.mkString(", "))

  def debug(messages: String*): Unit =
    debugString(messages.toList.mkString(", "))

  def debugOnce(messages: String*): Unit =
    debugOnceString(messages.toList.mkString(", "))

}
