package indigo.runtime

import indigo.shared.EqualTo._
import org.scalatest.{FunSpec, Matchers}

class GameContextSpec extends FunSpec with Matchers {

  describe("creating and destroying IIO monads") {

    it("should allow creation of an IIO") {
      GameContext.pure(10).unsafeRun shouldEqual 10
    }

    it("should allow delayed construction of an IIO") {
      GameContext.delay(10).unsafeRun() shouldEqual 10
    }

    it("should not evaluate code on creation of an IIO if delayed") {
      val e = new Exception("test")

      GameContext.delay(throw e).attemptRun shouldEqual Left(e)
    }

    it("should be able to recover from errors") {
      GameContext.pure(10).recover(GameContext.pure(50)).unsafeRun() shouldEqual 10
      GameContext.raiseError(new Exception("BOOM")).recover(GameContext.pure(50)).unsafeRun() shouldEqual 50
    }

  }

  describe("Functor operations") {

    it("should be a functor") {
      GameContext.pure(10).map(_ * 10).unsafeRun() shouldEqual 100
    }

    it("should be a monad") {
      GameContext.pure(10).flatMap(i => GameContext.pure(i * 10)).unsafeRun() shouldEqual 100
    }

    it("should be flattenable") {
      GameContext.pure(GameContext.pure("hello")).flatten shouldEqual GameContext.pure("hello")
    }

  }

  describe("Laws: Pure") {

    // Left identity: return a >>= f ≡ f a
    it("should respect left identity") {
      val a = 10
      val f = (i: Int) => GameContext.pure(i)
      assert(GameContext.areEqual(GameContext.pure(a).flatMap(f), f(a)))
    }

    // Right identity: m >>= return ≡ m
    it("should respect right identity") {
      val m = GameContext.pure(2)
      assert(GameContext.areEqual(m.flatMap(x => GameContext.pure[Int](x)), m))
    }

    // Associativity: (m >>= f) >>= g ≡ m >>= (\x -> f x >>= g)
    it("should respect associativity identity") {
      val m = GameContext.pure(3)
      val f = (i: Int) => GameContext.pure(s"$i")
      val g = (s: String) => GameContext.pure(s.length > 1)

      assert(GameContext.areEqual(m.flatMap(f).flatMap(g), m.flatMap((x: Int) => f(x).flatMap(g))))
    }

  }

  describe("evaluation") {

    it("should not evaluate a delayed action") {

      val result: GameContext[Int] =
        for {
          a <- GameContext.delay(10)
          b <- GameContext.delay(a * 10)
          c <- GameContext.delay(b / 2)
        } yield c

      result match {
        case GameContext.Pure(_) =>
          fail("Expected delay, got pure")

        case GameContext.RaiseError(_) =>
          fail("Expected delay, got raise error")

        case GameContext.Delay(thunk) =>
          thunk() shouldEqual 50
      }

    }

    it("should preserve error messages (flatMap)") {

      val result: GameContext[Int] =
        for {
          a <- GameContext.delay(10)
          b <- GameContext.raiseError[Int](new Exception(a.toString))
          c <- GameContext.delay(b / 2)
        } yield c

      result match {
        case GameContext.Pure(_) =>
          fail("Expected delay, got pure")

        case GameContext.RaiseError(e) =>
          e.getMessage shouldEqual "10"

        case GameContext.Delay(_) =>
          fail("Expected error, got delay")
      }

    }

    it("should preserve error messages (map)") {

      val f: Int => Int = i => {
        throw new Exception(i.toString)
      }

      val result: GameContext[Int] =
        GameContext.pure(10).map[Int](f)

      result match {
        case GameContext.Pure(_) =>
          fail("Expected error, got pure")

        case GameContext.RaiseError(e) =>
          e.getMessage shouldEqual "10"

        case GameContext.Delay(_) =>
          fail("Expected error, got delay")
      }

    }

  }

}
