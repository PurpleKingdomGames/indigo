package com.purplekingdomgames.indigoexts.lenses

import org.scalatest.{FunSpec, Matchers}

class LensSpec extends FunSpec with Matchers {

  val sample: A =
    A(B(10), C(D("hello")))

  val lensAB: Lens[A, B] =
    Lens(
      (a: A) => a.b,
      (a: A, b: B) => a.copy(b = b)
    )

  val lensAC: Lens[A, C] =
    Lens(
      (a: A) => a.c,
      (a: A, c: C) => a.copy(c = c)
    )

  val lensCD: Lens[C, D] =
    Lens(
      (c: C) => c.d,
      (c: C, d: D) => c.copy(d = d)
    )

  val lensD: Lens[D, String] =
    Lens(
      (d: D) => d.s,
      (d: D, s: String) => d.copy(s = s)
    )

  describe("Getting") {

    it("should be able to get a sub-object") {
      lensAB.get(sample) shouldEqual B(10)
    }

    it("should be able to get a more deeply nested sub-object with lens composition") {
      (lensAC andThen lensCD andThen lensD).get(sample) shouldEqual "hello"
    }

  }

  describe("Setting") {

    it("should be able to set a sub-object") {
      lensAB.set(sample, B(20)) shouldEqual sample.copy(b = B(20))
    }

    it("should be able to set then get") {
      lensAB.get(lensAB.set(sample, B(50))) shouldEqual B(50)
    }

    it("should be able to set a more deeply nested sub-object with lens composition") {
      val lens: Lens[A, String] = lensAC andThen lensCD andThen lensD
      val res1                  = lens.set(sample, "world")

      res1.c.d.s shouldEqual "world"
      lens.get(res1) shouldEqual "world"
    }

  }

}

case class A(b: B, c: C)
case class B(i: Int)
case class C(d: D)
case class D(s: String)
