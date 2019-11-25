package indigoexamples

import indigo._
import indigoexts.subsystems.automata._

object Score {

  final case class ScoreAmount(value: String) extends AutomatonPayload

  def automataSubSystem(fontKey: FontKey): Automata =
    Automata(
      AutomataPoolKey("points"),
      Automaton(
        Text("0", 0, 0, 1, fontKey).alignCenter,
        Millis(1500)
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
      seed => Signal.fixed(seed.timeAliveDelta.toDouble / seed.lifeSpan.toDouble)

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

    val tintSF: SignalFunction[Double, Tint] =
      SignalFunction { multiplier =>
        Tint(1, 0, 0, multiplier)
      }

    val newPosition: AutomatonSeedValues => Signal[Point] =
      seed => Signal.product(multiplierS(seed), spawnPositionS(seed)) |> positionSF

    val newAlpha: (AutomatonSeedValues, Text) => Signal[Double] =
      (seed, sceneGraphNode) => Signal.product(multiplierS(seed), renderableS(sceneGraphNode)) |> alphaSF

    val newTint: AutomatonSeedValues => Signal[Tint] =
      seed => multiplierS(seed) |> tintSF

    val signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (seed, sceneGraphNode) =>
        sceneGraphNode match {
          case t: Text =>
            seed.payload match {
              case Some(ScoreAmount(score)) =>
                Signal.triple(newPosition(seed), newAlpha(seed, t), newTint(seed)).map {
                  case (position, alpha, tint) =>
                    AutomatonUpdate.withNodes(
                      t.moveTo(position)
                        .withAlpha(alpha)
                        .withTint(tint)
                        .withText(score)
                    )
                }
              case _ =>
                Signal.fixed(AutomatonUpdate.withNodes(sceneGraphNode))
            }

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)
        }

  }

}
