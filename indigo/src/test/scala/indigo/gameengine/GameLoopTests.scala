package indigo.gameengine

import indigo.shared.events._
import indigo.shared.Outcome
import utest._
import indigo.shared.time.{GameTime, Millis}

object GameLoopTests extends TestSuite {

  val gameTime: GameTime =
    GameTime(Millis(1000), Millis(100), GameTime.FPS(100))

  val tests: Tests =
    Tests {
      "Processing model updates" - {

        "should be able to process simple model updates based on an event" - {

          val ges: GlobalEventStream =
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
            GameLoop.processModelUpdateEvents(gameTime, TestGameModel("bob"), gameEvents, signals, update, ges)

          val expected: TestGameModel =
            TestGameModel("fred")

          actual._1 ==> expected

        }

        "should be able to process a model update that emits a global event" - {

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
                Outcome(model.copy(name = name), List(ShowName("show: " + name)))

              case _ =>
                Outcome(model)
            }

          val actual: (TestGameModel, FrameInputEvents) =
            GameLoop.processModelUpdateEvents(gameTime, TestGameModel("bob"), gameEvents, signals, update, ges)

          val expected: TestGameModel =
            TestGameModel("teddy")

          actual._1 ==> expected
          ges.collect ==> List(ShowName("show: teddy"))

        }

      }
    }

}

final case class TestGameModel(name: String)
final case class ChangeName(to: String) extends GlobalEvent
final case class ShowName(name: String) extends GlobalEvent
