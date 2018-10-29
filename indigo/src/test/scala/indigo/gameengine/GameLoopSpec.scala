package indigo.gameengine
//import indigo.gameengine.audio.IAudioPlayer
import indigo.gameengine.events.{FrameEvent, GameEvent, GlobalEventStream}
//import indigo.gameengine.scenegraph.{SceneAudio, Volume}
import org.scalatest.{FunSpec, Matchers}

class GameLoopSpec extends FunSpec with Matchers {

//  val audioPlayer: IAudioPlayer =
//    new IAudioPlayer {
//      def playSound(assetRef: String, volume: Volume): Unit = ()
//      def playAudio(sceneAudio: SceneAudio): Unit           = ()
//    }

  val gameTime: GameTime = GameTime(1000, 100, 10)

  describe("Processing model updates") {

    it("should be able to process simple model updates based on an event") {

      implicit val ges: GlobalEventStream =
        new GlobalEventStream {
          def pushGameEvent(e: GameEvent): Unit  = ()
          def pushViewEvent(e: FrameEvent): Unit = ()
          def collect: List[GameEvent]           = Nil
        }

      val gameEvents: List[GameEvent] =
        List(
          ChangeName("fred")
        )

      val update: (GameTime, TestGameModel) => GameEvent => UpdatedModel[TestGameModel] =
        (_, model) => {
          case ChangeName(name) =>
            UpdatedModel(model.copy(name = name), Nil)

          case _ =>
            UpdatedModel(model, Nil)
        }

      val actual: TestGameModel =
        GameLoop.processModelUpdateEvents(gameTime, TestGameModel("bob"), gameEvents, update)

      val expected: TestGameModel =
        TestGameModel("fred")

      actual shouldEqual expected

    }

    it("should be able to process a model update that emits an event") {

      implicit val ges: GlobalEventStream =
        new GlobalEventStream {
          var l: List[GameEvent] = Nil

          def pushGameEvent(e: GameEvent): Unit = ()
          def pushViewEvent(e: FrameEvent): Unit = {
            l = l :+ e
            ()
          }
          def collect: List[GameEvent] = l
        }

      val gameEvents: List[GameEvent] =
        List(
          ChangeName("teddy")
        )

      val update: (GameTime, TestGameModel) => GameEvent => UpdatedModel[TestGameModel] =
        (_, model) => {
          case ChangeName(name) =>
            UpdatedModel(model.copy(name = name), List(ShowName("show: " + name)))

          case _ =>
            UpdatedModel(model, Nil)
        }

      val actual: TestGameModel =
        GameLoop.processModelUpdateEvents(gameTime, TestGameModel("bob"), gameEvents, update)

      val expected: TestGameModel =
        TestGameModel("teddy")

      actual shouldEqual expected
      ges.collect shouldEqual List(ShowName("show: teddy"))

    }

  }

}

case class TestGameModel(name: String)
case class ChangeName(to: String) extends FrameEvent
case class ShowName(name: String) extends FrameEvent
