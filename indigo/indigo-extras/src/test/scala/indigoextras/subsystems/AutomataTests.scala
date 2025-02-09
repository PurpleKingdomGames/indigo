package indigoextras.subsystems

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.Point
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.LayerKey
import indigo.shared.scenegraph.RenderNode
import indigo.shared.scenegraph.SceneNode
import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalReader
import indigo.shared.time.Seconds

import scalajs.js

class AutomataTests extends munit.FunSuite {

  import indigoextras.subsystems.FakeSubSystemFrameContext._

  val eventInstance =
    MyCullEvent("Hello, I'm dead.")

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("test")

  val graphic = Graphic(0, 0, 10, 10, Material.Bitmap(AssetName("fish")))

  val onCull: AutomatonSeedValues => List[GlobalEvent] =
    _ => List(eventInstance)

  val automaton: Automaton =
    Automaton(
      AutomatonNode.Fixed(graphic),
      Seconds(1)
    ).withOnCullEvent(onCull)
      .withModifier(ModiferFunctions.signal)

  val layerKey =
    LayerKey("test layer")

  val automata: Automata[Unit] =
    Automata(poolKey, automaton, layerKey)

  val startingState: AutomataState =
    automata
      .update(context(1), AutomataState(0, js.Array()))(AutomataEvent.Spawn(poolKey, Point.zero, None, None))
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
    def drawAt(time: Seconds): Graphic[?] = {
      val ctx = context(1, time, time)

      val nextState =
        automata
          .update(ctx, startingState)(AutomataEvent.Update(poolKey))
          .unsafeGet

      automata
        .present(ctx, nextState)
        .unsafeGet
        .layers
        .find(l => l.hasKey(layerKey))
        .get
        .toBatch
        .head
        .nodes
        .collect { case g: Graphic[_] => g }
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

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def toRenderNode(node: SceneNode): RenderNode[?] =
    node match {
      case r: RenderNode[_] => r
      case _                => throw new Exception("Wasn't a render node")
    }

  test("AutomatonNode.one of") {
    val nodeList: NonEmptyList[SceneNode] =
      NonEmptyList(
        graphic.moveTo(0, 0),
        graphic.moveTo(0, 10),
        graphic.moveTo(0, 20)
      )

    val nodes =
      AutomatonNode.OneOf(nodeList)

    assertEquals(toRenderNode(nodes.giveNode(0, Dice.loaded(0))).position.y, graphic.moveTo(0, 0).y)
    assertEquals(toRenderNode(nodes.giveNode(0, Dice.loaded(1))).position.y, graphic.moveTo(0, 10).y)
    assertEquals(toRenderNode(nodes.giveNode(0, Dice.loaded(2))).position.y, graphic.moveTo(0, 20).y)

    val dice = Dice.Sides.MaxInt(0)

    assertEquals(
      (0 to 100).toList.forall { _ =>
        val g = toRenderNode(nodes.giveNode(0, dice)).position.y
        nodeList.toList.map(n => toRenderNode(n).position.y).contains(g)
      },
      true
    )

  }

  test("AutomatonNode.cycle") {
    val nodeList: NonEmptyList[SceneNode] =
      NonEmptyList(
        graphic.moveTo(0, 0),
        graphic.moveTo(0, 10),
        graphic.moveTo(0, 20)
      )

    val nodes =
      AutomatonNode.Cycle(nodeList)

    assertEquals(toRenderNode(nodes.giveNode(0, Dice.loaded(0))).position.y, graphic.moveTo(0, 0).y)
    assertEquals(toRenderNode(nodes.giveNode(1, Dice.loaded(0))).position.y, graphic.moveTo(0, 10).y)
    assertEquals(toRenderNode(nodes.giveNode(2, Dice.loaded(0))).position.y, graphic.moveTo(0, 20).y)
    assertEquals(toRenderNode(nodes.giveNode(3, Dice.loaded(0))).position.y, graphic.moveTo(0, 0).y)
    assertEquals(toRenderNode(nodes.giveNode(4, Dice.loaded(0))).position.y, graphic.moveTo(0, 10).y)
    assertEquals(toRenderNode(nodes.giveNode(5, Dice.loaded(0))).position.y, graphic.moveTo(0, 20).y)
    assertEquals(toRenderNode(nodes.giveNode(6, Dice.loaded(0))).position.y, graphic.moveTo(0, 0).y)
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

    val signal: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
      SignalReader { case (seed, sceneGraphNode) =>
        makePosition(seed).map { position =>
          AutomatonUpdate(
            sceneGraphNode match {
              case g: Graphic[_] =>
                Batch(g.moveTo(position))

              case _ =>
                Batch.empty
            },
            Batch.empty
          )
        }
      }

  }

}

final case class MyCullEvent(message: String) extends GlobalEvent
