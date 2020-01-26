package pirate

import indigo._
import indigoexts.subsystems._

final case class CloudsSubSystem(screenWidth: Int, bigCloudPosition: Double, verticalCenter: Int, clouds: List[Cloud], lastSpawn: Millis) extends SubSystem {

  type EventType = FrameTick

  val eventFilter: GlobalEvent => Option[FrameTick] = {
    case FrameTick => Some(FrameTick)
    case _         => None
  }

  def update(gameTime: GameTime, dice: Dice): FrameTick => Outcome[CloudsSubSystem] = {
    case FrameTick if gameTime.running - lastSpawn > Millis(3000) =>
      val newCloud =
        CloudsSubSystem.spawnCloud(dice, screenWidth + dice.roll(30))

      Outcome(
        this.copy(
          bigCloudPosition = nextBigCloudPosition(gameTime),
          clouds = CloudsSubSystem.updateClouds(gameTime, newCloud :: clouds),
          lastSpawn = gameTime.running
        )
      )

    case FrameTick =>
      Outcome(
        this.copy(
          bigCloudPosition = nextBigCloudPosition(gameTime),
          clouds = CloudsSubSystem.updateClouds(gameTime, clouds)
        )
      )
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(drawBigClouds)
      .addGameLayerNodes(clouds.map(c => c.graphic.moveTo(c.position)))

  def report: String = "Clouds SubSystem"

  def nextBigCloudPosition(gameTime: GameTime): Double =
    if (bigCloudPosition <= 0) Assets.Clouds.bigCloudsWidth.toDouble else bigCloudPosition - (3d * gameTime.delta.value)

  def drawBigClouds: List[Graphic] =
    List(
      Assets.Clouds.bigCloudsGraphic.moveTo(bigCloudPosition.toInt - Assets.Clouds.bigCloudsWidth, verticalCenter),
      Assets.Clouds.bigCloudsGraphic.moveTo(bigCloudPosition.toInt, verticalCenter),
      Assets.Clouds.bigCloudsGraphic.moveTo(bigCloudPosition.toInt + Assets.Clouds.bigCloudsWidth, verticalCenter)
    )

}

object CloudsSubSystem {

  def init(screenWidth: Int): CloudsSubSystem =
    CloudsSubSystem(screenWidth, 0, 181, Nil, Millis.zero)

  def updateClouds(gameTime: GameTime, current: List[Cloud]): List[Cloud] =
    current.filterNot(_.offScreen).map(_.update(gameTime))

  def spawnCloud(dice: Dice, initialX: Int): Cloud =
    Cloud(
      Point(initialX, dice.roll(70) + 10),
      dice.roll(4) * 32,
      chooseCloud(dice.roll(3))
    )

  def chooseCloud(index: Int): Graphic =
    index match {
      case 1 => Assets.Clouds.cloudGraphic1
      case 2 => Assets.Clouds.cloudGraphic2
      case 3 => Assets.Clouds.cloudGraphic3
    }

}

final case class Cloud(position: Point, moveBy: Int, graphic: Graphic) {
  def update(gameTime: GameTime): Cloud = {
    val next = this.position - Point((moveBy.toDouble * gameTime.delta.value).toInt, 0)

    this.copy(position = next)
  }

  def offScreen: Boolean =
    position.x < -graphic.bounds.width
}
