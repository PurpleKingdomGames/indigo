package pirate.scenes.level.subsystems

import indigo.*
import indigoextras.subsystems.*
import pirate.core.Assets

/*
The `CloudsSubSystem` does two things:
1. Directly manages the constantly rolling big clouds on the horizon;
2. Emits periodic events telling the cloud automata system to spawn
   a new small cloud.
 */
object CloudsSubSystem:

  val verticalCenter: Int = 181
  val scrollSpeed: Double = 3.0d

  def apply(screenWidth: Int): SubSystem =
    SubSystem[FrameTick, CloudsState](
      SubSystemId("clouds"),
      eventFilter,
      Outcome(CloudsState.initial),
      update(screenWidth),
      render
    )

  lazy val eventFilter: GlobalEvent => Option[FrameTick] = {
    case FrameTick => Some(FrameTick)
    case _         => None
  }

  def update(screenWidth: Int): (SubSystemFrameContext, CloudsState) => FrameTick => Outcome[CloudsState] =
    (context, model) => {
      case FrameTick if context.gameTime.running - model.lastSpawn > Seconds(3.0) =>
        Outcome(
          CloudsState(
            bigCloudPosition = nextBigCloudPosition(
              context.gameTime,
              model.bigCloudPosition,
              Assets.Clouds.bigCloudsWidth
            ),
            lastSpawn = context.gameTime.running
          )
        ).addGlobalEvents(
          AutomataEvent.Spawn(
            CloudsAutomata.poolKey,
            generateSmallCloudStartPoint(screenWidth, context.dice),
            generateSmallCloudLifeSpan(context.dice),
            None
          )
        )

      case FrameTick =>
        Outcome(
          model.copy(
            bigCloudPosition = nextBigCloudPosition(
              context.gameTime,
              model.bigCloudPosition,
              Assets.Clouds.bigCloudsWidth
            )
          )
        )
    }

  val render: (SubSystemFrameContext, CloudsState) => Outcome[SceneUpdateFragment] =
    (_, model) =>
      Outcome(
        SceneUpdateFragment.empty
          .addLayer(
            Layer(
              BindingKey("big clouds"),
              Assets.Clouds.bigCloudsGraphic
                .moveTo(
                  model.bigCloudPosition.toInt - Assets.Clouds.bigCloudsWidth,
                  verticalCenter
                ),
              Assets.Clouds.bigCloudsGraphic
                .moveTo(
                  model.bigCloudPosition.toInt,
                  verticalCenter
                ),
              Assets.Clouds.bigCloudsGraphic
                .moveTo(
                  model.bigCloudPosition.toInt + Assets.Clouds.bigCloudsWidth,
                  verticalCenter
                )
            )
          )
      )

  def generateSmallCloudStartPoint(screenWidth: Int, dice: Dice): Point =
    Point(screenWidth + dice.roll(30), dice.roll(100) + 10)

  def generateSmallCloudLifeSpan(dice: Dice): Option[Seconds] =
    Some(Millis(((dice.roll(10) + 10) * 1000).toLong).toSeconds)

  def nextBigCloudPosition(gameTime: GameTime, bigCloudPosition: Double, assetWidth: Int): Double =
    if bigCloudPosition <= 0.0d then assetWidth.toDouble
    else bigCloudPosition - (scrollSpeed * gameTime.delta.toDouble)

final case class CloudsState(bigCloudPosition: Double, lastSpawn: Seconds)
object CloudsState:
  val initial: CloudsState =
    CloudsState(0, Seconds.zero)
