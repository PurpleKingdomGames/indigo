package indigo.shared.temporal

import utest._
import indigo.shared.time.Seconds

object SignalFunctionTests extends TestSuite {

  val f: Int => String =
    (i: Int) => "count: " + i.toString

  val g: String => Boolean =
    (s: String) => s.length > 10

  val x: Int => Boolean =
    (i: Int) => i > 10

  val tests: Tests =
    Tests {

      "lift / apply / arr (construction)" - {
        (Signal.fixed(10) |> SignalFunction(f)).at(Seconds.zero) ==> "count: 10"
        (Signal.fixed(20) |> SignalFunction.arr(f)).at(Seconds.zero) ==> "count: 20"
        (Signal.fixed(30) |> SignalFunction.lift(f)).at(Seconds.zero) ==> "count: 30"
      }

      "andThen / >>>" - {
        (Signal.fixed(10) |> (SignalFunction(f) andThen SignalFunction(g))).at(Seconds.zero) ==> false
        (Signal.fixed(10000) |> (SignalFunction(f) >>> SignalFunction(g))).at(Seconds.zero) ==> true
      }

      "parallel / &&& / and" - {
        (Signal.fixed(100) |> (SignalFunction(f) and SignalFunction(x))).at(Seconds.zero) ==> ("count: 100", true)
        (Signal.fixed(1) |> (SignalFunction(f) &&& SignalFunction(x))).at(Seconds.zero) ==> ("count: 1", false)
      }

      "SignalFunctions" - {
        "should be able to compose signal functions" - {
          val f = SignalFunction.lift((i: Int) => s"$i")
          val g = SignalFunction.lift((s: String) => s.length < 2)

          val h: SignalFunction[Int, Boolean] = f andThen g

          h.run(Signal.fixed(1)).at(Seconds.zero) ==> true
          h.run(Signal.fixed(1000)).at(Seconds.zero) ==> false
        }

        "should be able to run signal functions and parallel" - {
          val f = SignalFunction.lift((i: Int) => s"$i")
          val g = SignalFunction.lift((i: Int) => i < 10)

          val h: SignalFunction[Int, (String, Boolean)] = f and g

          h.run(Signal.fixed(1)).at(Seconds.zero) ==> ("1", true)
          h.run(Signal.fixed(1000)).at(Seconds.zero) ==> ("1000", false)
        }
      }

      "Fuller example" - {
        val makeRange: SignalFunction[Boolean, List[Int]] =
          SignalFunction { p =>
            val num = if (p) 10 else 5
            (1 to num).toList
          }

        val chooseCatsOrDogs: SignalFunction[Boolean, String] =
          SignalFunction(p => if (p) "dog" else "cat")

        val howManyPets: SignalFunction[(List[Int], String), List[String]] =
          SignalFunction {
            case (l, str) =>
              l.map(_.toString() + " " + str)
          }

        val signal = Signal.Pulse(Seconds(1))

        val signalFunction = (makeRange &&& chooseCatsOrDogs) >>> howManyPets

        val actual1   = (signal |> signalFunction).at(Seconds.zero)
        val expected1 = List("1 dog", "2 dog", "3 dog", "4 dog", "5 dog", "6 dog", "7 dog", "8 dog", "9 dog", "10 dog")
        actual1 ==> expected1

        val actual2   = (signal |> signalFunction).at(Seconds(1))
        val expected2 = List("1 cat", "2 cat", "3 cat", "4 cat", "5 cat")
        actual2 ==> expected2
      }

    }

}
