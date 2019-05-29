package ingidoexamples

import indigo._
import indigoexts.subsystems.automata._

object Score {

  final case class ScoreAmount(value: String) extends AutomatonPayload

  def automataSubSystem(fontKey: FontKey): Automata =
    Automata.empty
      .add(
        Automaton(
          AutomataPoolKey("points"),
          Text("0", 0, 0, 1, fontKey).alignCenter,
          Millis(1500)
        ).withModifier(ModiferFunctions.signal)
      )

  def spawnEvent(position: Point, dice: Dice): AutomataEvent =
    AutomataEvent.Spawn(AutomataPoolKey("points"), position, None, Some(ScoreAmount(generatePoints(dice))))

  def generateLocation(config: GameConfig, dice: Dice): Point =
    Point(dice.roll(config.viewport.width - 50) + 25, dice.roll(config.viewport.height - 50) + 25)

  def generatePoints(dice: Dice): String =
    (dice.roll(10) * 100).toString + "!"

  object ModiferFunctions {

    val multiplierS: AutomatonSeedValues => Signal[Double] =
      seed => Signal.fixed(seed.timeAliveDelta.toDouble / seed.lifeSpan.toDouble)

    val spawnPositionS: AutomatonSeedValues => Signal[Point] =
      seed => Signal.fixed(seed.spawnedAt)

    val renderableS: Renderable => Signal[Renderable] =
      renderable => Signal.fixed(renderable)

    val positionSF: SignalFunction[(Double, Point), Point] =
      SignalFunction {
        case (multiplier, spawnedAt) =>
          spawnedAt + Point(0, -(30 * multiplier).toInt)
      }

    val alphaSF: SignalFunction[(Double, Renderable), Double] =
      SignalFunction {
        case (multiplier, renderable) =>
          renderable.effects.alpha * multiplier
      }

    val tintSF: SignalFunction[Double, Tint] =
      SignalFunction { multiplier =>
        Tint(1 * multiplier, 0, 0)
      }

    val newPosition: AutomatonSeedValues => Signal[Point] =
      seed => Signal.product(multiplierS(seed), spawnPositionS(seed)) |> positionSF

    val newAlpha: (AutomatonSeedValues, Renderable) => Signal[Double] =
      (seed, renderable) => Signal.product(multiplierS(seed), renderableS(renderable)) |> alphaSF

    val newTint: AutomatonSeedValues => Signal[Tint] =
      seed => multiplierS(seed) |> tintSF

    val signal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] =
      (seed, renderable) =>
        seed.payload match {
          case Some(ScoreAmount(score)) =>
            Signal.triple(newPosition(seed), newAlpha(seed, renderable), newTint(seed)).map {
              case (position, alpha, tint) =>
                renderable match {
                  case t: Text =>
                    SceneUpdateFragment.empty.addGameLayerNodes(
                      t.moveTo(position)
                        .withAlpha(alpha)
                        .withTint(tint)
                        .withText(score)
                    )

                  case r =>
                    SceneUpdateFragment.empty.addGameLayerNodes(r)
                }
            }
          case _ =>
            Signal.fixed(SceneUpdateFragment.empty.addGameLayerNodes(renderable))
        }

  }

}
