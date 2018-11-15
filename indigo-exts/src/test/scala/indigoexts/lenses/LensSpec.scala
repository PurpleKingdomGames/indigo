package indigoexts.lenses

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

  describe("Constructors") {
    it("should have a simple identity function when there's nothing to do") {
      Lens.identity.get(B(10)) shouldEqual B(10)
    }

    it("should have allow you to keep the original") {
      Lens.keepOriginal[D].get(D("hi")) shouldEqual D("hi")
      Lens.keepOriginal[D].set(D("hi"), D("fish")) shouldEqual D("hi")
    }

    it("should have allow you to keep the latest") {
      Lens.keepLatest[D].get(D("hi")) shouldEqual D("hi")
      Lens.keepLatest[D].set(D("hi"), D("fish")) shouldEqual D("fish")
    }

    it("should allow you to define a fixed lens with a constant B") {
      Lens.fixed[C, D](D("hi")).get(C(D("x"))) shouldEqual D("hi")
      Lens.fixed[C, D](D("hi")).set(C(D("x")), D("y")) shouldEqual C(D("x"))
    }
  }

  describe("Getting") {

    it("should be able to get a sub-object") {
      lensAB.get(sample) shouldEqual B(10)
    }

    it("should be able to get a more deeply nested sub-object with lens composition") {
      (lensAC andThen lensCD >=> lensD).get(sample) shouldEqual "hello"
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

  describe("Lens Laws") {

    val lens: Lens[B, Int] =
      Lens(
        b => b.i,
        (b, ii) => b.copy(i = ii)
      )

    it("must be true, that getting and setting a value back, changes nothing.") {
      val x = B(100)

      lens.set(x, lens.get(x)) shouldEqual x
    }

    it("must be true, that setting and then getting returns what I set.") {
      lens.get(lens.set(B(100), 50)) shouldEqual 50
    }

    it("must be true, that setting twice and then getting returns the last value (no history)") {
      lens.get(lens.set(lens.set(B(100), 50), 25)) shouldEqual 25
    }

  }

}

case class A(b: B, c: C)
case class B(i: Int)
case class C(d: D)
case class D(s: String)
