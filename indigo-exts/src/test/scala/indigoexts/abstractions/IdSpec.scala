package indigoexts.abstractions

import org.scalatest.{FunSpec, Matchers}

class IdSpec extends FunSpec with Matchers {

  describe("The Id Monad") {

    it("should be constructable") {
      Id(12) === Id(12) shouldEqual true
      Id(4) === Id(4) shouldEqual true
      Id.pure("hello") === Id("hello") shouldEqual true
    }

    it("should be mappable") {
      Id(4).map(_ * 5) === Id(20) shouldEqual true
    }

    it("should be flattenable") {
      Id.flatten(Id(Id(4))) === Id(4) shouldEqual true
    }

    it("should be flatMappable") {
      Id(3).flatMap(i => Id(i + i)) === Id(6) shouldEqual true
    }

  }

}
