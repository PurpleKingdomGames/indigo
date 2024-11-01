package example.game

import cats.effect.IO
import indigo._
import indigo.scenes._
import tyrian.TyrianSubSystem

final case class MyAwesomeGame(tyrianSubSystem: TyrianSubSystem[IO, String, Unit], clockwise: Boolean)
    extends IndigoGame[Unit, Unit, Unit, Unit]:

  def initialScene(bootData: Unit): Option[SceneName] =
    None

  def scenes(bootData: Unit): NonEmptyList[Scene[Unit, Unit, Unit]] =
    NonEmptyList(GameScene(clockwise))

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit, Unit]] =
    val gameViewport =
      (flags.get("width"), flags.get("height")) match {
        case (Some(w), Some(h)) =>
          GameViewport(w.toInt, h.toInt)

        case _ =>
          GameViewport(300, 300)
      }

    Outcome(
      BootResult
        .noData(
          GameConfig.default
            .withViewport(gameViewport)
            .noResize
        )
        .withSubSystems(tyrianSubSystem)
    )

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: Unit, model: Unit): Outcome[Unit] =
    Outcome(())

  def setup(
      bootData: Unit,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def updateModel(
      context: FrameContext[Unit],
      model: Unit
  ): GlobalEvent => Outcome[Unit] =
    case tyrianSubSystem.TyrianEvent.Receive(msg) =>
      IndigoLogger.consoleLog(s"(Indigo) from tyrian: [${tyrianSubSystem.indigoGameId.getOrElse(" ")}] " + msg)
      val e =
        if clockwise then tyrianSubSystem.send(msg.reverse)
        else tyrianSubSystem.send(msg + "_" + msg)

      Outcome(model)
        .addGlobalEvents(e)

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: FrameContext[Unit],
      model: Unit,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[Unit],
      model: Unit,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
