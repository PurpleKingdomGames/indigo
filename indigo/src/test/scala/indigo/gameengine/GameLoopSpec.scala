package indigo.gameengine

import indigo.gameengine.events._
import org.scalatest.{FunSpec, Matchers}

class GameLoopSpec extends FunSpec with Matchers {

  val gameTime: GameTime = new GameTime(GameTime.Millis(1000), GameTime.Millis(100), GameTime.FPS(100))

  describe("Processing model updates") {

    it("should be able to process simple model updates based on an event") {

      implicit val ges: GlobalEventStream =
        new GlobalEventStream {
          def pushLoopEvent(e: GlobalEvent): Unit   = ()
          def pushGlobalEvent(e: GlobalEvent): Unit = ()
          def collect: List[GlobalEvent]            = Nil
        }

      val gameEvents: List[GlobalEvent] =
        List(
          ChangeName("fred")
        )

      val signals: Signals =
        Signals.default

      val update: (GameTime, TestGameModel) => GlobalEvent => Outcome[TestGameModel] =
        (_, model) => {
          case ChangeName(name) =>
            Outcome(model.copy(name = name))

          case _ =>
            Outcome(model)
        }

      val actual: (TestGameModel, FrameInputEvents) =
        GameLoop.processModelUpdateEvents(gameTime, TestGameModel("bob"), gameEvents, signals, update)

      val expected: TestGameModel =
        TestGameModel("fred")

      actual._1 shouldEqual expected

    }

    it("should be able to process a model update that emits a global and an InFrame event") {

      implicit val ges: GlobalEventStream =
        new GlobalEventStream {
          var l: List[GlobalEvent] = Nil

          def pushLoopEvent(e: GlobalEvent): Unit = ()
          def pushGlobalEvent(e: GlobalEvent): Unit = {
            l = l :+ e
            ()
          }
          def collect: List[GlobalEvent] = l
        }

      val gameEvents: List[GlobalEvent] =
        List(
          ChangeName("teddy")
        )

      val signals: Signals =
        Signals.default

      val update: (GameTime, TestGameModel) => GlobalEvent => Outcome[TestGameModel] =
        (_, model) => {
          case ChangeName(name) =>
            Outcome(model.copy(name = name), List(ShowName("show: " + name)), List(PresentName(name)))

          case _ =>
            Outcome(model)
        }

      val actual: (TestGameModel, FrameInputEvents) =
        GameLoop.processModelUpdateEvents(gameTime, TestGameModel("bob"), gameEvents, signals, update)

      val expected: TestGameModel =
        TestGameModel("teddy")

      actual._1 shouldEqual expected
      actual._2.inFrameEvents shouldEqual List(PresentName("teddy"))
      ges.collect shouldEqual List(ShowName("show: teddy"))

    }

  }

}

final case class TestGameModel(name: String)
final case class ChangeName(to: String)    extends GlobalEvent
final case class ShowName(name: String)    extends GlobalEvent
final case class PresentName(name: String) extends InFrameEvent
