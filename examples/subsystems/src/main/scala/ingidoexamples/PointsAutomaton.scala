package ingidoexamples

import indigo._
import indigoexts.automaton._

object PointsAutomaton {

  val positionModifier: (GameTime, AutomatonSeedValues, Point) => Point =
    (_, seed, _) => seed.spawnedAt + Point(0, -(30 * (seed.timeAliveDelta / seed.lifeSpan)).toInt)

  def automataSubSystem(fontKey: FontKey): AutomataFarm =
    AutomataFarm.empty.add(
      TextAutomaton(
        AutomataPoolKey("points"),
        Text("10", 0, 0, 1, fontKey).alignCenter,
        AutomataLifeSpan(1000),
        List(
          AutomataModifier.MoveTo(positionModifier)
        )
      )
    )

  def spawnEvent(position: Point) =
    AutomataEvent.Spawn(AutomataPoolKey("points"), position)

}
