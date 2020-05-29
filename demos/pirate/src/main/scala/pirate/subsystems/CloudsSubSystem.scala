package pirate.subsystems

import indigo._
import indigoextras.subsystems._
import pirate.init.Assets

final case class CloudsSubSystem(screenWidth: Int, bigCloudPosition: Double, verticalCenter: Int, lastSpawn: Seconds) extends SubSystem {

  type EventType = FrameTick

  val eventFilter: GlobalEvent => Option[FrameTick] = {
    case FrameTick => Some(FrameTick)
    case _         => None
  }

  def update(context: FrameContext): FrameTick => Outcome[CloudsSubSystem] = {
    case FrameTick if context.gameTime.running - lastSpawn > Seconds(3.0) =>
      Outcome(
        this.copy(
          bigCloudPosition = nextBigCloudPosition(context.gameTime),
          lastSpawn = context.gameTime.running
        )
      ).addGlobalEvents(spawnSmallCloud(context.dice, screenWidth)) // STEP 6

    case FrameTick =>
      Outcome(this.copy(bigCloudPosition = nextBigCloudPosition(context.gameTime)))
  }

  def render(context: FrameContext): SceneUpdateFragment =
    SceneUpdateFragment.empty.addGameLayerNodes(drawBigClouds)

  private def nextBigCloudPosition(gameTime: GameTime): Double =
    if (bigCloudPosition <= 0) Assets.Clouds.bigCloudsWidth.toDouble else bigCloudPosition - (3d * gameTime.delta.value)

  private def drawBigClouds: List[Graphic] =
    List(
      makeCloud(bigCloudPosition.toInt - Assets.Clouds.bigCloudsWidth),
      makeCloud(bigCloudPosition.toInt),
      makeCloud(bigCloudPosition.toInt + Assets.Clouds.bigCloudsWidth)
    )

  private def makeCloud(xPosition: Int): Graphic =
    Assets.Clouds.bigCloudsGraphic.moveTo(xPosition, verticalCenter)

  private def spawnSmallCloud(dice: Dice, screenWidth: Int): AutomataEvent.Spawn = { // STEP 6
    val initialPosition = Point(screenWidth + dice.roll(30), dice.roll(100) + 10)

    AutomataEvent.Spawn(
      CloudsAutomata.poolKey,
      initialPosition,
      Option(Millis(((dice.roll(10) + 10) * 1000).toLong).toSeconds),
      None
    )
  }
}

object CloudsSubSystem {

  def init(screenWidth: Int): CloudsSubSystem =
    CloudsSubSystem(screenWidth, 0, 181, Seconds.zero)

}
