package indigo.shared.temporal

import utest._
import indigo.shared.time.Millis

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
        (Signal.fixed(10) |> SignalFunction(f)).at(Millis.zero) ==> "count: 10"
        (Signal.fixed(20) |> SignalFunction.arr(f)).at(Millis.zero) ==> "count: 20"
        (Signal.fixed(30) |> SignalFunction.lift(f)).at(Millis.zero) ==> "count: 30"
      }

      "andThen / >>>" - {
        (Signal.fixed(10) |> (SignalFunction(f) andThen SignalFunction(g))).at(Millis.zero) ==> false
        (Signal.fixed(10000) |> (SignalFunction(f) >>> SignalFunction(g))).at(Millis.zero) ==> true
      }

      "parallel / &&& / and" - {
        (Signal.fixed(100) |> (SignalFunction(f) and SignalFunction(x))).at(Millis.zero) ==> ("count: 100", true)
        (Signal.fixed(1) |> (SignalFunction(f) &&& SignalFunction(x))).at(Millis.zero) ==> ("count: 1", false)
      }

    }

}
