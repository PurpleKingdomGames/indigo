package indigoexamples

import indigo._
import indigoextras.subsystems._

object Score {

  final case class ScoreAmount(value: String) extends AutomatonPayload

  def automataSubSystem(fontKey: FontKey): Automata =
    Automata(
      AutomataPoolKey("points"),
      Automaton(
        AutomatonNode.Fixed(Text("0", 0, 0, 1, fontKey, Material.ImageEffects(FontStuff.fontName)).alignCenter),
        Seconds(1.5d)
      ).withModifier(ModiferFunctions.signal)
    )

  def spawnEvent(position: Point, dice: Dice): AutomataEvent =
    AutomataEvent.Spawn(AutomataPoolKey("points"), position, None, Some(ScoreAmount(generatePoints(dice))))

  def generateLocation(viewportSize: Point, dice: Dice): Point =
    Point(dice.roll(viewportSize.x - 50) + 25, dice.roll(viewportSize.y - 50) + 25)

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
          sceneGraphNode.material match {
            case m: Material.ImageEffects =>
              m.alpha * multiplier

            case _ =>
              1.0
          }
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

    val signal: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
      SignalReader {
        case (seed, sceneGraphNode) =>
          sceneGraphNode match {
            case t: Text =>
              seed.payload match {
                case Some(ScoreAmount(score)) =>
                  Signal.triple(newPosition(seed), newAlpha(seed, t), newTint(seed)).map {
                    case (position, alpha, tint) =>
                      AutomatonUpdate(
                        t.moveTo(position)
                          .withText(score)
                          .modifyMaterial {
                            case m: Material.ImageEffects =>
                              m.withAlpha(alpha)
                                .withTint(tint)
                            case m => m
                          }
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

}
