package com.purplekingdomgames.indigo.runtime

/**
  * This is the dumbest logger ever.
  * We're just logging to the Browsers console, I just wanted a few standard headers with the message.
  */
object IndigoLogger {

  private val INFO: String  = "INFO"
  private val ERROR: String = "ERROR"
  private val DEBUG: String = "DEBUG"

  private val errorLogs: scalajs.js.Array[String] = new scalajs.js.Array[String]()
  private val debugLogs: scalajs.js.Array[String] = new scalajs.js.Array[String]()

  private def formatMessage(level: String, message: String): String =
    s"""[${System.currentTimeMillis()}] [$level] [Indigo] $message"""

  private val consoleLogString: String => Unit = message => println(message)

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

  def consoleLog[A](valueA: A)(implicit showA: Show[A]): Unit =
    consoleLogString(s"${showA.show(valueA)}")
  def consoleLog[A, B](valueA: A, valueB: B)(implicit showA: Show[A], showB: Show[B]): Unit =
    consoleLogString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def consoleLog[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: Show[A], showB: Show[B], showC: Show[C]): Unit =
    consoleLogString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def consoleLog[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D]): Unit =
    consoleLogString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def consoleLog[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D], showE: Show[E]): Unit =
    consoleLogString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def info[A](valueA: A)(implicit showA: Show[A]): Unit =
    infoString(s"${showA.show(valueA)}")
  def info[A, B](valueA: A, valueB: B)(implicit showA: Show[A], showB: Show[B]): Unit =
    infoString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def info[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: Show[A], showB: Show[B], showC: Show[C]): Unit =
    infoString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def info[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D]): Unit =
    infoString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def info[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D], showE: Show[E]): Unit =
    infoString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def error[A](valueA: A)(implicit showA: Show[A]): Unit =
    errorString(s"${showA.show(valueA)}")
  def error[A, B](valueA: A, valueB: B)(implicit showA: Show[A], showB: Show[B]): Unit =
    errorString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def error[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: Show[A], showB: Show[B], showC: Show[C]): Unit =
    errorString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def error[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D]): Unit =
    errorString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def error[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D], showE: Show[E]): Unit =
    errorString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def errorOnce[A](valueA: A)(implicit showA: Show[A]): Unit =
    errorOnceString(s"${showA.show(valueA)}")
  def errorOnce[A, B](valueA: A, valueB: B)(implicit showA: Show[A], showB: Show[B]): Unit =
    errorOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def errorOnce[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: Show[A], showB: Show[B], showC: Show[C]): Unit =
    errorOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def errorOnce[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D]): Unit =
    errorOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def errorOnce[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D], showE: Show[E]): Unit =
    errorOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def debug[A](valueA: A)(implicit showA: Show[A]): Unit =
    debugString(s"${showA.show(valueA)}")
  def debug[A, B](valueA: A, valueB: B)(implicit showA: Show[A], showB: Show[B]): Unit =
    debugString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def debug[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: Show[A], showB: Show[B], showC: Show[C]): Unit =
    debugString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def debug[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D]): Unit =
    debugString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def debug[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D], showE: Show[E]): Unit =
    debugString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def debugOnce[A](valueA: A)(implicit showA: Show[A]): Unit =
    debugOnceString(s"${showA.show(valueA)}")
  def debugOnce[A, B](valueA: A, valueB: B)(implicit showA: Show[A], showB: Show[B]): Unit =
    debugOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def debugOnce[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: Show[A], showB: Show[B], showC: Show[C]): Unit =
    debugOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def debugOnce[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D]): Unit =
    debugOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def debugOnce[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: Show[A], showB: Show[B], showC: Show[C], showD: Show[D], showE: Show[E]): Unit =
    debugOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

}
