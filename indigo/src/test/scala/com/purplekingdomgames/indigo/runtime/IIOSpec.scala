package com.purplekingdomgames.indigo.runtime

import org.scalatest.{FunSpec, Matchers}

class IIOSpec extends FunSpec with Matchers {

  describe("creating and destroying IIO monads") {

    it("should allow creation of an IIO") {
      IIO.pure(10).unsafeRun shouldEqual 10
    }

    it("should not evaluate code on creation of an IIO if delayed") {
      val e = new Exception("test")

      IIO.delay(throw e).attemptRun shouldEqual Left(e)
    }

  }

}
