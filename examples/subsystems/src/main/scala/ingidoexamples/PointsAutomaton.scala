package ingidoexamples

import indigo._
import indigoexts.automaton._
import indigo.Millis
import indigoexts.temporal.{Signal, SignalFunction}
import indigo.gameengine.scenegraph.Renderable

object PointsAutomaton {

  val timeAndAutomatonSeedValues: AutomatonSeedValues => Signal[(Millis, AutomatonSeedValues)] =
    seed => Signal.Time |*| Signal.fixed(seed)

  val timeShift: SignalFunction[(Millis, AutomatonSeedValues), (Millis, AutomatonSeedValues)] =
    SignalFunction(t => (t._1 - t._2.createdAt, t._2))

  val timeToSeconds: SignalFunction[(Millis, AutomatonSeedValues), (Double, AutomatonSeedValues)] =
    SignalFunction(t => (t._1.toDouble * 0.001d, t._2))

  val positionY: SignalFunction[(Double, AutomatonSeedValues), Int] =
    SignalFunction(t => t._2.spawnedAt.y - (t._1 * 30).toInt)

  val signalPipeline: SignalFunction[(Millis, AutomatonSeedValues), Int] =
    timeShift >>> timeToSeconds >>> positionY

  val signal: AutomatonSeedValues => Signal[Int] =
    seed => timeAndAutomatonSeedValues(seed) |> signalPipeline

  val modifier: (AutomatonSeedValues, Renderable) => Signal[Outcome[Renderable]] =
    (seed, renderable) =>
      Signal.merge(signal(seed), Signal.fixed(renderable)) { (yPos, text) =>
        Outcome(text.moveTo(seed.spawnedAt.x, yPos))
      }

  def automataSubSystem(fontKey: FontKey): AutomataFarm =
    AutomataFarm.empty.add(
      Automaton(
        AutomatonPoolKey("points"),
        Text("10", 0, 0, 1, fontKey).alignCenter,
        Millis(1000)
      ).withModifier(modifier)
    )

  def spawnEvent(position: Point) =
    AutomataFarmEvent.Spawn(AutomatonPoolKey("points"), position)

}
