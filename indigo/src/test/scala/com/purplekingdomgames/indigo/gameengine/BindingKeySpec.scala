package com.purplekingdomgames.indigo.gameengine

import org.scalatest.{FunSpec, Matchers}

class BindingKeySpec extends FunSpec with Matchers {

  describe("Binding keys") {

    it("should generate unique keys on each request") {

      BindingKey.generate shouldNot be(BindingKey.generate)

    }

    it("should be able to generate a key") {

      withClue("Length should be 16 but was " + BindingKey.generate.value.length) {
        BindingKey.generate.value.length should be(16)
      }

      withClue("value should match ^[0-9a-zA-Z]$ but was something like " + BindingKey.generate.value) {
        BindingKey.generate.value.matches("""^[0-9a-zA-Z]{16}$""") should be(true)
      }

    }

  }

}
