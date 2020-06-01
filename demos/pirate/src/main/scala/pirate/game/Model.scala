package pirate.game

import indigo._

import pirate.init.Assets

final case class Model(screenDimensions: Rectangle, pirateState: PirateState, facingRight: Boolean, position: Point, soundLastPlayed: Seconds, lastRespawn: Seconds) {
  val pirateIsSafe: Boolean = Model.navRegion.isPointWithin(position)
}

object Model {

  val navRegion: Rectangle       = Rectangle(137, 0, 338, 272)
  val beat: Seconds               = Seconds(0.25)
  val walkDistancePerSecond: Int = 128
  val fallDistancePerSecond: Int = 300

  def initialModel(screenDimensions: Rectangle): Model =
    Model(screenDimensions, PirateState.Falling, true, Point(screenDimensions.horizontalCenter, 0), Seconds.zero, Seconds.zero)

  def update(gameTime: GameTime, model: Model, inputState: InputState): GlobalEvent => Outcome[Model] = {
    case FrameTick if model.pirateIsSafe && model.position.y == Model.navRegion.bottom - 1 =>
      convertStateToModel(gameTime, InputMapper(inputState), model)

    case FrameTick =>
      convertStateToModel(gameTime, model.pirateState, model)

    case _ =>
      Outcome(model)
  }

  def convertStateToModel(gameTime: GameTime, nextState: PirateState, model: Model): Outcome[Model] = {
    val walkSpeed: Int = (walkDistancePerSecond.toDouble * gameTime.delta.value).toInt
    val fallSpeed: Int = (fallDistancePerSecond.toDouble * gameTime.delta.value).toInt

    nextState match {
      // Landed
      case PirateState.Falling if model.pirateIsSafe && model.position.y + fallSpeed >= Model.navRegion.bottom =>
        Outcome(model.copy(pirateState = PirateState.Idle, position = Point(model.position.x, Model.navRegion.bottom - 1)))

      // Fall off the bottom of the screen
      case PirateState.Falling if model.position.y > model.screenDimensions.height + 50 =>
        Outcome(model.copy(position = Point(model.screenDimensions.horizontalCenter, 20), lastRespawn = gameTime.running))
          .addGlobalEvents(PlaySound(Assets.Sounds.respawnSound, Volume.Max))

      // Otherwise, fall normally
      case PirateState.Falling =>
        Outcome(model.copy(position = model.position + Point(0, fallSpeed)))

      // Move left while on the ground
      case PirateState.MoveLeft if model.pirateIsSafe =>
        val (walkingSound, soundLastPlayed) = updateWalkSound(gameTime, model)

        Outcome(
          model.copy(
            pirateState = nextState,
            facingRight = false,
            position = model.position - Point(walkSpeed, 0),
            soundLastPlayed = soundLastPlayed
          )
        ).addGlobalEvents(walkingSound)

      // Moving left but not safe = Falling
      case PirateState.MoveLeft =>
        Outcome(
          model.copy(
            pirateState = PirateState.Falling,
            facingRight = false
          )
        )

      // Move left while on the ground
      case PirateState.MoveRight if model.pirateIsSafe =>
        val (walkingSound, soundLastPlayed) = updateWalkSound(gameTime, model)

        Outcome(
          model.copy(
            pirateState = nextState,
            facingRight = true,
            position = model.position + Point(walkSpeed, 0),
            soundLastPlayed = soundLastPlayed
          )
        ).addGlobalEvents(walkingSound)

      // Moving right but not safe = Falling
      case PirateState.MoveRight =>
        Outcome(
          model.copy(
            pirateState = PirateState.Falling,
            facingRight = true
          )
        )

      // Reset state
      case PirateState.Idle =>
        Outcome(model.copy(pirateState = nextState))
    }
  }

  def updateWalkSound(gameTime: GameTime, model: Model): (List[GlobalEvent], Seconds) =
    if (gameTime.running > model.soundLastPlayed + Model.beat) {
      (List(PlaySound(Assets.Sounds.walkSound, Volume(0.5d))), gameTime.running)
    } else (Nil, model.soundLastPlayed)
}
