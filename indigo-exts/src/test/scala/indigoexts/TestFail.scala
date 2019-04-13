package indigoexts

object TestFail {
  def fail(message: String): Unit =
    throw new java.lang.AssertionError(message)
}
