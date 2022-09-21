package indigo.shared.temporal

import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.time.Seconds

class SignalFunctionTests extends munit.FunSuite {

  val f: Int => String =
    (i: Int) => "count: " + i.toString

  val g: String => Boolean =
    (s: String) => s.length > 10

  val x: Int => Boolean =
    (i: Int) => i > 10

  test("lift / apply / arr (construction)") {
    assertEquals((Signal.fixed(10) |> SignalFunction(f)).at(Seconds.zero), "count: 10")
    assertEquals((Signal.fixed(20) |> SignalFunction.arr(f)).at(Seconds.zero), "count: 20")
    assertEquals((Signal.fixed(30) |> SignalFunction.lift(f)).at(Seconds.zero), "count: 30")
  }

  test("andThen / >>>") {
    assertEquals((Signal.fixed(10) |> (SignalFunction(f) andThen SignalFunction(g))).at(Seconds.zero), false)
    assertEquals((Signal.fixed(10000) |> (SignalFunction(f) >>> SignalFunction(g))).at(Seconds.zero), true)
  }

  test("parallel / &&& / and") {
    assertEquals(
      (Signal.fixed(100) |> (SignalFunction(f) and SignalFunction(x))).at(Seconds.zero),
      ("count: 100", true)
    )
    assertEquals((Signal.fixed(1) |> (SignalFunction(f) &&& SignalFunction(x))).at(Seconds.zero), ("count: 1", false))
  }

  test("SignalFunctions should be able to compose signal functions") {
    val f = SignalFunction.lift((i: Int) => s"$i")
    val g = SignalFunction.lift((s: String) => s.length < 2)

    val h: SignalFunction[Int, Boolean] = f andThen g

    assertEquals(h.run(Signal.fixed(1)).at(Seconds.zero), true)
    assertEquals(h.run(Signal.fixed(1000)).at(Seconds.zero), false)
  }

  test("SignalFunctions should be able to run signal functions and parallel") {
    val f = SignalFunction.lift((i: Int) => s"$i")
    val g = SignalFunction.lift((i: Int) => i < 10)

    val h: SignalFunction[Int, (String, Boolean)] = f and g

    assertEquals(h.run(Signal.fixed(1)).at(Seconds.zero), ("1", true))
    assertEquals(h.run(Signal.fixed(1000)).at(Seconds.zero), ("1000", false))
  }

  test("Fuller example") {
    val makeRange: SignalFunction[Boolean, List[Int]] =
      SignalFunction { p =>
        val num = if (p) 10 else 5
        (1 to num).toList
      }

    val chooseCatsOrDogs: SignalFunction[Boolean, String] =
      SignalFunction(p => if (p) "dog" else "cat")

    val howManyPets: SignalFunction[(List[Int], String), List[String]] =
      SignalFunction { case (l, str) =>
        l.map(_.toString() + " " + str)
      }

    val signal = Signal.Pulse(Seconds(1))

    val signalFunction = (makeRange &&& chooseCatsOrDogs) >>> howManyPets

    val actual1   = (signal |> signalFunction).at(Seconds.zero)
    val expected1 = List("1 dog", "2 dog", "3 dog", "4 dog", "5 dog", "6 dog", "7 dog", "8 dog", "9 dog", "10 dog")
    assertEquals(actual1, expected1)

    val actual2   = (signal |> signalFunction).at(Seconds(1))
    val expected2 = List("1 cat", "2 cat", "3 cat", "4 cat", "5 cat")
    assertEquals(actual2, expected2)
  }

  import indigo.shared.temporal.SignalFunction as SF
  import indigo.syntax.*

  test("lerp") {
    val sf = Signal.Time |> SF.lerp(10.seconds)

    assert(sf.at(-1.seconds) == -0.1)
    assert(sf.at(0.seconds) == 0.0)
    assert(sf.at(1.seconds) == 0.1)
    assert(sf.at(2.seconds) == 0.2)
    assert(sf.at(3.seconds) == 0.3)
    assert(sf.at(4.seconds) == 0.4)
    assert(sf.at(5.seconds) == 0.5)
    assert(sf.at(6.seconds) == 0.6)
    assert(sf.at(7.seconds) == 0.7)
    assert(sf.at(8.seconds) == 0.8)
    assert(sf.at(9.seconds) == 0.9)
    assert(sf.at(10.seconds) == 1.0)
    assert(sf.at(11.seconds) == 1.1)
  }

  test("easeIn") {
    val sf = Signal.Time |> SF.easeIn(10.seconds)

    assert(clue(round(sf.at(0.seconds))) == clue(0.0))
    assert(clue(round(sf.at(1.seconds))) == clue(0.01))
    assert(clue(round(sf.at(2.seconds))) == clue(0.04))
    assert(clue(round(sf.at(3.seconds))) == clue(0.09))
    assert(clue(round(sf.at(4.seconds))) == clue(0.16))
    assert(clue(round(sf.at(5.seconds))) == clue(0.25))
    assert(clue(round(sf.at(6.seconds))) == clue(0.36))
    assert(clue(round(sf.at(7.seconds))) == clue(0.48))
    assert(clue(round(sf.at(8.seconds))) == clue(0.64))
    assert(clue(round(sf.at(9.seconds))) == clue(0.81))
    assert(clue(round(sf.at(10.seconds))) == clue(1.0))
  }

  test("easeOut") {
    val sf = Signal.Time |> SF.easeOut(10.seconds)

    assert(clue(round(sf.at(0.seconds))) == clue(0.0))
    assert(clue(round(sf.at(1.seconds))) == clue(0.18))
    assert(clue(round(sf.at(2.seconds))) == clue(0.35))
    assert(clue(round(sf.at(3.seconds))) == clue(0.51))
    assert(clue(round(sf.at(4.seconds))) == clue(0.64))
    assert(clue(round(sf.at(5.seconds))) == clue(0.75))
    assert(clue(round(sf.at(6.seconds))) == clue(0.84))
    assert(clue(round(sf.at(7.seconds))) == clue(0.9))
    assert(clue(round(sf.at(8.seconds))) == clue(0.96))
    assert(clue(round(sf.at(9.seconds))) == clue(0.99))
    assert(clue(round(sf.at(10.seconds))) == clue(1.0))
  }

  test("easeInOut") {
    val sf = Signal.Time |> SF.easeInOut(10.seconds)

    assert(clue(round(sf.at(0.seconds))) == clue(0.0))
    assert(clue(round(sf.at(1.seconds))) == clue(0.02))
    assert(clue(round(sf.at(2.seconds))) == clue(0.09))
    assert(clue(round(sf.at(3.seconds))) == clue(0.2))
    assert(clue(round(sf.at(4.seconds))) == clue(0.34))
    assert(clue(round(sf.at(5.seconds))) == clue(0.49))
    assert(clue(round(sf.at(6.seconds))) == clue(0.65))
    assert(clue(round(sf.at(7.seconds))) == clue(0.79))
    assert(clue(round(sf.at(8.seconds))) == clue(0.9))
    assert(clue(round(sf.at(9.seconds))) == clue(0.97))
    assert(clue(round(sf.at(10.seconds))) == clue(1.0))
  }

  test("wrap") {
    val sf = Signal.Time |> SF.wrap(10.seconds)

    assert(sf.at(0.seconds) == 0.seconds)
    assert(sf.at(5.seconds) == 5.seconds)
    assert(sf.at(10.seconds) == 0.seconds)
    assert(sf.at(15.seconds) == 5.seconds)
    assert(sf.at(20.seconds) == 0.seconds)
    assert(sf.at(21.seconds) == 1.seconds)
  }

  test("clamp") {
    val sf = Signal.Time |> SF.clamp(2.seconds, 5.seconds)

    assert(sf.at(0.seconds) == 2.seconds)
    assert(sf.at(2.seconds) == 2.seconds)
    assert(sf.at(3.seconds) == 3.seconds)
    assert(sf.at(4.seconds) == 4.seconds)
    assert(sf.at(5.seconds) == 5.seconds)
    assert(sf.at(6.seconds) == 5.seconds)
    assert(sf.at(10.seconds) == 5.seconds)
  }

  test("step") {
    val sf = Signal.Time |> SF.step(10.seconds)

    assert(sf.at(0.seconds) == false)
    assert(sf.at(5.seconds) == false)
    assert(sf.at(10.seconds) == true)
    assert(sf.at(15.seconds) == true)
    assert(sf.at(20.seconds) == true)
  }

  test("sin") {
    val sf = Signal.Time |> SF.sin

    assert(sf.at(-1.seconds) == 0.0d)
    assert(sf.at(-0.25.seconds) == -1.0d)
    assert(sf.at(0.seconds) == 0.0d)
    assert(sf.at(0.25.seconds) == 1.0d)
    assert(sf.at(1.seconds) == 0.0d)
  }

  test("cos") {
    val sf = Signal.Time |> SF.cos

    assert(sf.at(-1.seconds) == 1.0d)
    assert(sf.at(-0.5.seconds) == -1.0d)
    assert(sf.at(0.seconds) == 1.0d)
    assert(sf.at(0.5.seconds) == -1.0d)
    assert(sf.at(1.seconds) == 1.0d)
  }

  test("orbit") {
    val sf = Signal.Time |> SF.orbit(Vector2(0), 1, Radians.zero)

    assert(clue(sf.at(0.seconds)) ~== clue(Vector2(0, 1)))
    assert(clue(sf.at(0.25.seconds)) ~== clue(Vector2(1, 0)))
    assert(clue(sf.at(0.5.seconds)) ~== clue(Vector2(0, -1)))
    assert(clue(sf.at(0.75.seconds)) ~== clue(Vector2(-1, 0)))
    assert(clue(sf.at(1.0.seconds)) ~== clue(Vector2(0, 1)))
  }

  test("pulse") {
    val sf = Signal.Time |> SF.pulse(10.seconds)

    assert(sf.at(0.seconds) == true)
    assert(sf.at(1.seconds) == true)
    assert(sf.at(10.seconds) == false)
    assert(sf.at(11.seconds) == false)
    assert(sf.at(20.seconds) == true)
    assert(sf.at(23.seconds) == true)
    assert(sf.at(1234.seconds) == false)
    assert(sf.at(1243.seconds) == true)
  }

  test("smoothPulse") {
    val sf = Signal.Time |> SF.smoothPulse

    assert(round(sf.at(0.seconds)) == 0.0)
    assert(round(sf.at(0.25.seconds)) == 0.5)
    assert(round(sf.at(0.5.seconds)) == 1.0)
    assert(round(sf.at(0.75.seconds)) == 0.5)
    assert(round(sf.at(1.0.seconds)) == 0.0)
    assert(round(sf.at(1.5.seconds)) == 1.0)
    assert(round(sf.at(2.0.seconds)) == 0.0)
  }

  test("multiply") {
    val sf = Signal.Time |> SF.multiply(10.seconds)

    assert(sf.at(0.seconds) == 0.seconds)
    assert(sf.at(1.seconds) == 10.seconds)
    assert(sf.at(2.seconds) == 20.seconds)
    assert(sf.at(30.seconds) == 300.seconds)
  }

  def round(d: Double): Double =
    Math.floor(d * 100d) / 100d

}
