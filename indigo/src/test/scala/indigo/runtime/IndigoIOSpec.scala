package indigo.runtime

import indigo.IndigoEq._
import org.scalatest.{FunSpec, Matchers}

class IndigoIOSpec extends FunSpec with Matchers {

  describe("creating and destroying IIO monads") {

    it("should allow creation of an IIO") {
      IndigoIO.pure(10).unsafeRun shouldEqual 10
    }

    it("should allow delayed construction of an IIO") {
      IndigoIO.delay(10).unsafeRun() shouldEqual 10
    }

    it("should not evaluate code on creation of an IIO if delayed") {
      val e = new Exception("test")

      IndigoIO.delay(throw e).attemptRun shouldEqual Left(e)
    }

    it("should be able to recover from errors") {
      IndigoIO.pure(10).recover(IndigoIO.pure(50)).unsafeRun() shouldEqual 10
      IndigoIO.raiseError(new Exception("BOOM")).recover(IndigoIO.pure(50)).unsafeRun() shouldEqual 50
    }

  }

  describe("Functor operations") {

    it("should be a functor") {
      IndigoIO.pure(10).map(_ * 10).unsafeRun() shouldEqual 100
    }

    it("should be a monad") {
      IndigoIO.pure(10).flatMap(i => IndigoIO.pure(i * 10)).unsafeRun() shouldEqual 100
    }

    it("should be flattenable") {
      IndigoIO.pure(IndigoIO.pure("hello")).flatten shouldEqual IndigoIO.pure("hello")
    }

  }

  describe("Laws: Pure") {

    // Left identity: return a >>= f ≡ f a
    it("should respect left identity") {
      val a = 10
      val f = (i: Int) => IndigoIO.pure(i)
      assert(IndigoIO.areEqual(IndigoIO.pure(a).flatMap(f), f(a)))
    }

    // Right identity: m >>= return ≡ m
    it("should respect right identity") {
      val m = IndigoIO.pure(2)
      assert(IndigoIO.areEqual(m.flatMap(x => IndigoIO.pure[Int](x)), m))
    }

    // Associativity: (m >>= f) >>= g ≡ m >>= (\x -> f x >>= g)
    it("should respect associativity identity") {
      val m = IndigoIO.pure(3)
      val f = (i: Int) => IndigoIO.pure(s"$i")
      val g = (s: String) => IndigoIO.pure(s.length > 1)

      assert(IndigoIO.areEqual(m.flatMap(f).flatMap(g), m.flatMap((x: Int) => f(x).flatMap(g))))
    }

  }

  describe("evaluation") {

    it("should not evaluate a delayed action") {

      val result: IndigoIO[Int] =
        for {
          a <- IndigoIO.delay(10)
          b <- IndigoIO.delay(a * 10)
          c <- IndigoIO.delay(b / 2)
        } yield c

      result match {
        case IndigoIO.Pure(_) =>
          fail("Expected delay, got pure")

        case IndigoIO.RaiseError(_) =>
          fail("Expected delay, got raise error")

        case IndigoIO.Delay(thunk) =>
          thunk() shouldEqual 50
      }

    }

    it("should preserve error messages (flatMap)") {

      val result: IndigoIO[Int] =
        for {
          a <- IndigoIO.delay(10)
          b <- IndigoIO.raiseError[Int](new Exception(a.toString))
          c <- IndigoIO.delay(b / 2)
        } yield c

      result match {
        case IndigoIO.Pure(_) =>
          fail("Expected delay, got pure")

        case IndigoIO.RaiseError(e) =>
          e.getMessage shouldEqual "10"

        case IndigoIO.Delay(_) =>
          fail("Expected error, got delay")
      }

    }

    it("should preserve error messages (map)") {

      val f: Int => Int = i => {
        throw new Exception(i.toString)
      }

      val result: IndigoIO[Int] =
        IndigoIO.pure(10).map[Int](f)

      result match {
        case IndigoIO.Pure(_) =>
          fail("Expected error, got pure")

        case IndigoIO.RaiseError(e) =>
          e.getMessage shouldEqual "10"

        case IndigoIO.Delay(_) =>
          fail("Expected error, got delay")
      }

    }

  }

}
