package indigoexamples

import indigo._
import indigoextras.subsystems._

object Score {

  final case class ScoreAmount(value: String) extends AutomatonPayload

  def automataSubSystem(fontKey: FontKey): Automata =
    Automata(
      AutomataPoolKey("points"),
      Automaton(
        Text("0", 0, 0, 1, fontKey).alignCenter,
        Seconds(1.5d)
      ).withModifier(ModiferFunctions.signal),
      Automata.Layer.Game
    )

  def spawnEvent(position: Point, dice: Dice): AutomataEvent =
    AutomataEvent.Spawn(AutomataPoolKey("points"), position, None, Some(ScoreAmount(generatePoints(dice))))

  def generateLocation(config: GameConfig, dice: Dice): Point =
    Point(dice.roll(config.viewport.width - 50) + 25, dice.roll(config.viewport.height - 50) + 25)

  def generatePoints(dice: Dice): String =
    (dice.roll(10) * 100).toString + "!"

  object ModiferFunctions {

    val multiplierS: AutomatonSeedValues => Signal[Double] =
      seed => Signal(seed.progression)

    val spawnPositionS: AutomatonSeedValues => Signal[Point] =
      seed => Signal.fixed(seed.spawnedAt)

    val renderableS: Text => Signal[Text] =
      renderable => Signal.fixed(renderable)

    val positionSF: SignalFunction[(Double, Point), Point] =
      SignalFunction {
        case (multiplier, spawnedAt) =>
          spawnedAt + Point(0, -(30 * multiplier).toInt)
      }

    val alphaSF: SignalFunction[(Double, Text), Double] =
      SignalFunction {
        case (multiplier, sceneGraphNode) =>
          sceneGraphNode.effects.alpha * multiplier
      }

    val tintSF: SignalFunction[Double, RGBA] =
      SignalFunction { multiplier =>
        RGBA(1, 0, 0, multiplier)
      }

    val newPosition: AutomatonSeedValues => Signal[Point] =
      seed => Signal.product(multiplierS(seed), spawnPositionS(seed)) |> positionSF

    val newAlpha: (AutomatonSeedValues, Text) => Signal[Double] =
      (seed, sceneGraphNode) => Signal.product(multiplierS(seed), renderableS(sceneGraphNode)) |> alphaSF

    val newTint: AutomatonSeedValues => Signal[RGBA] =
      seed => multiplierS(seed) |> tintSF

    val signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (seed, sceneGraphNode) =>
        sceneGraphNode match {
          case t: Text =>
            seed.payload match {
              case Some(ScoreAmount(score)) =>
                Signal.triple(newPosition(seed), newAlpha(seed, t), newTint(seed)).map {
                  case (position, alpha, tint) =>
                    AutomatonUpdate(
                      t.moveTo(position)
                        .withAlpha(alpha)
                        .withTint(tint)
                        .withText(score)
                    )
                }
              case _ =>
                Signal.fixed(AutomatonUpdate(sceneGraphNode))
            }

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)
        }

  }

}
