package indigo.runtime

import indigo.shared.EqualTo._
import utest._
import indigo.TestFail._

object GameContextTests extends TestSuite {

  val tests: Tests =
    Tests {
      "creating and destroying IIO monads" - {

        "should allow creation of an IIO" - {
          GameContext.pure(10).unsafeRun ==> 10
        }

        "should allow delayed construction of an IIO" - {
          GameContext.delay(10).unsafeRun() ==> 10
        }

        "should not evaluate code on creation of an IIO if delayed" - {
          val e = new Exception("test")

          GameContext.delay(throw e).attemptRun ==> Left(e)
        }

        "should be able to recover from errors" - {
          GameContext.pure(10).recover(GameContext.pure(50)).unsafeRun() ==> 10
          GameContext.raiseError(new Exception("BOOM")).recover(GameContext.pure(50)).unsafeRun() ==> 50
        }

      }

      "Functor operations" - {

        "should be a functor" - {
          GameContext.pure(10).map(_ * 10).unsafeRun() ==> 100
        }

        "should be a monad" - {
          GameContext.pure(10).flatMap(i => GameContext.pure(i * 10)).unsafeRun() ==> 100
        }

        "should be flattenable" - {
          GameContext.pure(GameContext.pure("hello")).flatten ==> GameContext.pure("hello")
        }

      }

      "Laws: Pure" - {

        // Left identity: return a >>= f ≡ f a
        "should respect left identity" - {
          val a = 10
          val f = (i: Int) => GameContext.pure(i)
          assert(GameContext.areEqual(GameContext.pure(a).flatMap(f), f(a)))
        }

        // Right identity: m >>= return ≡ m
        "should respect right identity" - {
          val m = GameContext.pure(2)
          assert(GameContext.areEqual(m.flatMap(x => GameContext.pure[Int](x)), m))
        }

        // Associativity: (m >>= f) >>= g ≡ m >>= (\x -> f x >>= g)
        "should respect associativity identity" - {
          val m = GameContext.pure(3)
          val f = (i: Int) => GameContext.pure(s"$i")
          val g = (s: String) => GameContext.pure(s.length > 1)

          assert(GameContext.areEqual(m.flatMap(f).flatMap(g), m.flatMap((x: Int) => f(x).flatMap(g))))
        }

      }

      "evaluation" - {

        "should not evaluate a delayed action" - {

          val result: GameContext[Int] =
            for {
              a <- GameContext.delay(10)
              b <- GameContext.delay(a * 10)
              c <- GameContext.delay(b / 2)
            } yield c

          result match {
            case GameContext.Pure(_) =>
              println("Expected delay, got pure")
              fail("error")

            case GameContext.RaiseError(_) =>
              println("Expected delay, got raise error")
              fail("error")

            case GameContext.Delay(thunk) =>
              thunk() ==> 50
          }

        }

        "should preserve error messages (flatMap)" - {

          val result: GameContext[Int] =
            for {
              a <- GameContext.delay(10)
              b <- GameContext.raiseError[Int](new Exception(a.toString))
              c <- GameContext.delay(b / 2)
            } yield c

          result match {
            case GameContext.Pure(_) =>
              println("Expected delay, got pure")
              fail("error")

            case GameContext.RaiseError(e) =>
              e.getMessage ==> "10"

            case GameContext.Delay(_) =>
              println("Expected error, got delay")
              fail("error")
          }

        }

        "should preserve error messages (map)" - {

          val f: Int => Int = i => {
            throw new Exception(i.toString)
          }

          val result: GameContext[Int] =
            GameContext.pure(10).map[Int](f)

          result match {
            case GameContext.Pure(_) =>
              println("Expected error, got pure")
              fail("error")

            case GameContext.RaiseError(e) =>
              e.getMessage ==> "10"

            case GameContext.Delay(_) =>
              println("Expected error, got delay")
              fail("error")
          }

        }

      }
    }

}
