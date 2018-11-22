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

  describe("Modifying") {

    it("should be able to modify a value in place") {

      val lens  = lensCD andThen lensD
      val value = C(D("hello"))

      lensCD.modify(value, d => D(d.s + " there")) shouldEqual C(D("hello there"))
      lens.modify(value, _ + ", world!") shouldEqual C(D("hello, world!"))

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

  // What does this even mean? Clearly a load of rubbish.
  /*
  There's a lot wrong here.
  A lens on a collection needs to be recursive
  Or it needs to be a memberhsip lens
  Or it needs to be an optional lens
  Or it needs to be a prism.

  I'm leaving this here for now, not sure if I'll fix it as technically none of this is needed.
    ..but it is interesting.
   */
//  describe("Parametric lenses") {
//
//    it("should be able to change the type of a tuple") {
//
//      implicit val lensTupleRight: Lens[(Int, String), String] =
//        Lens(
//          p => p._2,
//          (a, b) => (a._1, b)
//        )
//      implicit val lensList: Lens[List[Int], Int] =
//        Lens(
//          p => p.head,
//          (a, b) => b :: a.drop(1)
//        )
//
//      val value: (Int, String) = (10, "foo")
//
//      withClue("String => String (tuple)") {
//        val expectedA: (Int, String) = (10, "bar")
//
//        val f: String => String                         = _ => "bar"
//        val g: ((Int, String), String) => (Int, String) = (s, b) => (s._1, b)
//
//        Lens.modifyF(value)(f, g) shouldEqual expectedA
//      }
//
//      withClue("String => Boolean (tuple)") {
//        val expectedB: (Int, Boolean) = (10, true)
//
//        val f: String => Boolean                          = _.length < 5
//        val g: ((Int, String), Boolean) => (Int, Boolean) = (s, b) => (s._1, b)
//
//        Lens.modifyF(value)(f, g) shouldEqual expectedB
//      }
//
//      withClue("String => Boolean (list)") {
//        val l: List[Int] = List(1,2,3,4)
//        val expectedC: List[Boolean] = List(false, true, false, true)
//
//        val f: Int => Boolean                          = _ % 2 == 0
//        val g: (List[Int], Boolean) => List[Boolean] = (s, b) => (s._1, b)
//
//        Lens.modifyF(l)(f, g) shouldEqual expectedC
//      }
//    }
//
//  }

}

case class A(b: B, c: C)
case class B(i: Int)
case class C(d: D)
case class D(s: String)
