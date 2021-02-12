package indigoextras.subsystems

import indigo.shared.scenegraph.Graphic
import indigo.shared.events.GlobalEvent
import indigo.shared.dice.Dice
import indigo.shared.datatypes.Point
import indigo.shared.assets.AssetName
import indigo.shared.materials.StandardMaterial
import indigo.shared.time.Seconds
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.temporal.{Signal, SignalReader}
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.BindingKey

class AutomataTests extends munit.FunSuite {

  import indigoextras.subsystems.FakeSubSystemFrameContext._

  val eventInstance =
    MyCullEvent("Hello, I'm dead.")

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("test")

  val graphic = Graphic(0, 0, 10, 10, 1, StandardMaterial.Bitmap(AssetName("fish")))

  val onCull: AutomatonSeedValues => List[GlobalEvent] =
    _ => List(eventInstance)

  val automaton: Automaton =
    Automaton(
      AutomatonNode.Fixed(graphic),
      Seconds(1)
    ).withOnCullEvent(onCull)
      .withModifier(ModiferFunctions.signal)

  val layerKey =
    BindingKey("test layer")

  val automata: Automata =
    Automata(poolKey, automaton, layerKey)

  val startingState: AutomataState =
    automata
      .update(context(1), AutomataState(0, Nil))(AutomataEvent.Spawn(poolKey, Point.zero, None, None))
      .unsafeGet

  test("Starting state should contain 1 automaton") {

    val expected =
      SpawnedAutomaton(
        graphic,
        ModiferFunctions.signal,
        onCull,
        new AutomatonSeedValues(
          Point.zero,
          Seconds(0),
          Seconds(1),
          1, // comes from the fake frame context
          None
        )
      )

    assertEquals(startingState.totalSpawned, 1L)
    assertEquals(startingState.pool.length, 1)
    assertEquals(startingState.pool.head, expected)
  }

  test("should move a particle with a modifier signal") {

    import ModiferFunctions._

    // Test the signal
    val seed = new AutomatonSeedValues(Point.zero, Seconds.zero, Seconds(1), 0, None)

    assertEquals(makePosition(seed).at(Seconds(0)), Point(0, 0))
    assertEquals(makePosition(seed).at(Seconds(0.5)), Point(0, -15))
    assertEquals(makePosition(seed).at(Seconds(1)), Point(0, -30))

    // Test the automaton
    def drawAt(time: Seconds): Graphic = {
      val ctx = context(1, time, time)

      val nextState =
        automata
          .update(ctx, startingState)(AutomataEvent.Update(poolKey))
          .unsafeGet

      automata
        .present(ctx, nextState)
        .unsafeGet
        .layers
        .find(l => l.key.contains(layerKey))
        .get
        .nodes
        .collect { case g: Graphic => g }
        .head
    }

    assertEquals(drawAt(Seconds(0)).position, Point(0, 0))
    assertEquals(drawAt(Seconds(0.5)).position, Point(0, -15))
    assertEquals(drawAt(Seconds(0.9)).position, Point(0, -27))
  }

  test("culling an automaton should result in an event") {

    // 1 ms over the lifespan, so should be culled
    val outcome =
      automata
        .update(context(1, Seconds(1)), startingState)(AutomataEvent.Update(poolKey))

    assertEquals(outcome.unsafeGet.totalSpawned, 1L)
    assertEquals(outcome.unsafeGet.pool.length, 0)
    assertEquals(outcome.unsafeGlobalEvents.head, eventInstance)
  }

  test("KillAll should... kill all the automatons.") {

    // At any time, KillAll, should remove all automatons without trigger cull events.
    val outcome =
      automata
        .update(context(1, Seconds(0)), startingState)(AutomataEvent.KillAll(poolKey))

    assertEquals(outcome.unsafeGet.totalSpawned, 1L)
    assertEquals(outcome.unsafeGet.pool.isEmpty, true)
    assertEquals(outcome.unsafeGlobalEvents.isEmpty, true)
  }

  test("AutomatonNode.fixed") {
    val node =
      AutomatonNode.Fixed(graphic).giveNode(0, Dice.loaded(0))

    assertEquals(node, graphic)
  }

  test("AutomatonNode.one of") {
    val nodeList: NonEmptyList[SceneGraphNode] =
      NonEmptyList(
        graphic.moveTo(0, 0),
        graphic.moveTo(0, 10),
        graphic.moveTo(0, 20)
      )

    val nodes =
      AutomatonNode.OneOf(nodeList)

    assertEquals(nodes.giveNode(0, Dice.loaded(0)).position.y, graphic.moveTo(0, 0).y)
    assertEquals(nodes.giveNode(0, Dice.loaded(1)).position.y, graphic.moveTo(0, 10).y)
    assertEquals(nodes.giveNode(0, Dice.loaded(2)).position.y, graphic.moveTo(0, 20).y)

    val dice = Dice.Sides.MaxInt(0)

    assertEquals(
      (0 to 100).toList.forall { _ =>
        val g = nodes.giveNode(0, dice).position.y
        nodeList.toList.map(_.position.y).contains(g)
      },
      true
    )

  }

  test("AutomatonNode.cycle") {
    val nodeList: NonEmptyList[SceneGraphNode] =
      NonEmptyList(
        graphic.moveTo(0, 0),
        graphic.moveTo(0, 10),
        graphic.moveTo(0, 20)
      )

    val nodes =
      AutomatonNode.Cycle(nodeList)

    assertEquals(nodes.giveNode(0, Dice.loaded(0)).position.y, graphic.moveTo(0, 0).y)
    assertEquals(nodes.giveNode(1, Dice.loaded(0)).position.y, graphic.moveTo(0, 10).y)
    assertEquals(nodes.giveNode(2, Dice.loaded(0)).position.y, graphic.moveTo(0, 20).y)
    assertEquals(nodes.giveNode(3, Dice.loaded(0)).position.y, graphic.moveTo(0, 0).y)
    assertEquals(nodes.giveNode(4, Dice.loaded(0)).position.y, graphic.moveTo(0, 10).y)
    assertEquals(nodes.giveNode(5, Dice.loaded(0)).position.y, graphic.moveTo(0, 20).y)
    assertEquals(nodes.giveNode(6, Dice.loaded(0)).position.y, graphic.moveTo(0, 0).y)
  }

  object ModiferFunctions {

    val makePosition: AutomatonSeedValues => Signal[Point] =
      seed =>
        Signal { time =>
          seed.spawnedAt +
            Point(
              0,
              -(30d * seed.progression(time)).toInt
            )
        }

    val signal: SignalReader[(AutomatonSeedValues, SceneGraphNode), AutomatonUpdate] =
      SignalReader {
        case (seed, sceneGraphNode) =>
          makePosition(seed).map { position =>
            AutomatonUpdate(
              sceneGraphNode match {
                case g: Graphic =>
                  List(g.moveTo(position))

                case _ =>
                  Nil
              },
              Nil
            )
          }
      }

  }

}

final case class MyCullEvent(message: String) extends GlobalEvent
