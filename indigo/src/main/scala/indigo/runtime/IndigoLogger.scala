package indigo.runtime

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

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private val errorOnceString: String => Unit = message =>
    if (!errorLogs.iterator.contains(message)) {
      errorLogs.push(message)
      println(formatMessage(ERROR, message))
  }

  private val debugString: String => Unit = message => println(formatMessage(DEBUG, message))

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private val debugOnceString: String => Unit = message =>
    if (!debugLogs.iterator.contains(message)) {
      debugLogs.push(message)
      println(formatMessage(DEBUG, message))
  }

  def consoleLog[A](valueA: A)(implicit showA: AsString[A]): Unit =
    consoleLogString(s"${showA.show(valueA)}")
  def consoleLog[A, B](valueA: A, valueB: B)(implicit showA: AsString[A], showB: AsString[B]): Unit =
    consoleLogString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def consoleLog[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C]): Unit =
    consoleLogString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def consoleLog[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D]): Unit =
    consoleLogString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def consoleLog[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: AsString[A],
                                                                                       showB: AsString[B],
                                                                                       showC: AsString[C],
                                                                                       showD: AsString[D],
                                                                                       showE: AsString[E]): Unit =
    consoleLogString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def info[A](valueA: A)(implicit showA: AsString[A]): Unit =
    infoString(s"${showA.show(valueA)}")
  def info[A, B](valueA: A, valueB: B)(implicit showA: AsString[A], showB: AsString[B]): Unit =
    infoString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def info[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C]): Unit =
    infoString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def info[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D]): Unit =
    infoString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def info[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D], showE: AsString[E]): Unit =
    infoString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def error[A](valueA: A)(implicit showA: AsString[A]): Unit =
    errorString(s"${showA.show(valueA)}")
  def error[A, B](valueA: A, valueB: B)(implicit showA: AsString[A], showB: AsString[B]): Unit =
    errorString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def error[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C]): Unit =
    errorString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def error[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D]): Unit =
    errorString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def error[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D], showE: AsString[E]): Unit =
    errorString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def errorOnce[A](valueA: A)(implicit showA: AsString[A]): Unit =
    errorOnceString(s"${showA.show(valueA)}")
  def errorOnce[A, B](valueA: A, valueB: B)(implicit showA: AsString[A], showB: AsString[B]): Unit =
    errorOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def errorOnce[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C]): Unit =
    errorOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def errorOnce[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D]): Unit =
    errorOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def errorOnce[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: AsString[A],
                                                                                      showB: AsString[B],
                                                                                      showC: AsString[C],
                                                                                      showD: AsString[D],
                                                                                      showE: AsString[E]): Unit =
    errorOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def debug[A](valueA: A)(implicit showA: AsString[A]): Unit =
    debugString(s"${showA.show(valueA)}")
  def debug[A, B](valueA: A, valueB: B)(implicit showA: AsString[A], showB: AsString[B]): Unit =
    debugString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def debug[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C]): Unit =
    debugString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def debug[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D]): Unit =
    debugString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def debug[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D], showE: AsString[E]): Unit =
    debugString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

  def debugOnce[A](valueA: A)(implicit showA: AsString[A]): Unit =
    debugOnceString(s"${showA.show(valueA)}")
  def debugOnce[A, B](valueA: A, valueB: B)(implicit showA: AsString[A], showB: AsString[B]): Unit =
    debugOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}")
  def debugOnce[A, B, C](valueA: A, valueB: B, valueC: C)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C]): Unit =
    debugOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}")
  def debugOnce[A, B, C, D](valueA: A, valueB: B, valueC: C, valueD: D)(implicit showA: AsString[A], showB: AsString[B], showC: AsString[C], showD: AsString[D]): Unit =
    debugOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}")
  def debugOnce[A, B, C, D, E](valueA: A, valueB: B, valueC: C, valueD: D, valueE: E)(implicit showA: AsString[A],
                                                                                      showB: AsString[B],
                                                                                      showC: AsString[C],
                                                                                      showD: AsString[D],
                                                                                      showE: AsString[E]): Unit =
    debugOnceString(s"${showA.show(valueA)}, ${showB.show(valueB)}, ${showC.show(valueC)}, ${showD.show(valueD)}, ${showE.show(valueE)}")

}
