package pirate.scenes.level

import indigo.*
import indigo.physics.*

import pirate.core.Assets
import pirate.core.LevelDataStore
import pirate.scenes.level.model.PirateState
import pirate.scenes.level.model.Pirate
import pirate.scenes.level.model.LevelModel
import pirate.scenes.level.viewmodel.LevelViewModel
import pirate.scenes.level.viewmodel.PirateViewState
import pirate.generated.Assets.*

object LevelView:

  def draw(
      gameTime: GameTime,
      model: LevelModel.Ready,
      viewModel: LevelViewModel.Ready,
      captain: Sprite[Material.ImageEffects],
      levelDataStore: Option[LevelDataStore]
  ): SceneUpdateFragment =
    Level.draw(levelDataStore) |+| {
      model.world.findByTag("pirate").headOption match
        case Some(collider: Collider.Box[_]) =>
          PirateCaptain.draw(
            gameTime,
            model.pirate,
            collider,
            viewModel.pirateViewState,
            captain,
            viewModel.worldToScreenSpace
          )

        case _ =>
          // Shouldn't happen.
          SceneUpdateFragment.empty
    } // |+| showColliderDebug(model.world, viewModel.worldToScreenSpace)

  def showColliderDebug(world: World[String], toScreenSpace: Vertex => Vertex): SceneUpdateFragment =
    SceneUpdateFragment(
      world.present {
        case Collider.Circle(_, bounds, _, _, _, _, _, _, _) =>
          // Won't get here, no circles.
          Shape.Circle(
            toScreenSpace(bounds.position).toPoint,
            bounds.radius.toInt,
            Fill.None,
            Stroke(1, RGBA.Green)
          )

        case Collider.Box(_, bounds, _, _, _, _, _, _, _) =>
          Shape.Box(
            BoundingBox(
              toScreenSpace(bounds.position),
              toScreenSpace(bounds.size)
            ).toRectangle,
            Fill.None,
            Stroke(1, RGBA.Green)
          )
      }
    )

  object Level:

    def draw(levelDataStore: Option[LevelDataStore]): SceneUpdateFragment =
      levelDataStore
        .map { levelAssets =>
          SceneUpdateFragment.empty
            .addLayer(
              Layer(
                BindingKey("background"),
                Batch(Graphic(Rectangle(0, 0, 640, 360), 50, assets.static.bgMaterial)) ++
                  drawWater(levelAssets.waterReflections)
              )
            )
            .addLayer(
              Layer(BindingKey("game"), drawForeground(levelAssets))
            )
            .withAudio(
              assets.sounds.bgmusicSceneAudio
            )
        }
        .getOrElse(SceneUpdateFragment.empty)

    def drawWater(waterReflections: Sprite[Material.Bitmap]): Batch[SceneNode] =
      Batch(
        waterReflections.play(),
        waterReflections.moveBy(150, 30).play(),
        waterReflections.moveBy(-100, 60).play()
      )

    def drawForeground(assets: LevelDataStore): Batch[SceneNode] =
      Batch(
        assets.flag.play(),
        assets.helm.play(),
        Assets.Trees.tallTrunkGraphic.moveTo(420, 236),
        Assets.Trees.leftLeaningTrunkGraphic.moveTo(100, 286),
        Assets.Trees.rightLeaningTrunkGraphic.moveTo(25, 166),
        assets.backTallPalm.moveTo(420, 226).changeCycle(CycleLabel("P Back")).play(),
        assets.palm.moveTo(397, 204).play(),
        assets.palm.moveTo(77, 251).play(),
        assets.palm.moveTo(37, 120).play(),
        Assets.Static.chestGraphic.moveTo(380, 288),
        assets.terrain
      )

  object PirateCaptain:

    def draw(
        gameTime: GameTime,
        pirate: Pirate,
        collider: Collider.Box[String],
        pirateViewState: PirateViewState,
        captain: Sprite[Material.ImageEffects],
        toScreenSpace: Vertex => Vertex
    ): SceneUpdateFragment =
      SceneUpdateFragment.empty
        .addLayer(
          Layer(
            BindingKey("game"),
            respawnEffect(
              gameTime,
              pirate.lastRespawn,
              updatedCaptain(pirate, collider, pirateViewState, captain, toScreenSpace)
            )
          )
        )

    val respawnFlashSignal: Seconds => Signal[(Boolean, Boolean)] =
      lastRespawn => Signal(_ < lastRespawn + Seconds(1.2)) |*| Signal.Pulse(Seconds(0.1))

    val captainWithAlpha
        : Sprite[Material.ImageEffects] => SignalFunction[(Boolean, Boolean), Sprite[Material.ImageEffects]] =
      captain =>
        SignalFunction {
          case (false, _) =>
            captain

          case (true, true) =>
            captain
              .modifyMaterial(_.withAlpha(1))

          case (true, false) =>
            captain
              .modifyMaterial(_.withAlpha(0))
        }

    def respawnEffect(
        gameTime: GameTime,
        lastRespawn: Seconds,
        captain: Sprite[Material.ImageEffects]
    ): Sprite[Material.ImageEffects] =
      (respawnFlashSignal(lastRespawn) |> captainWithAlpha(captain)).at(gameTime.running)

    def updatedCaptain(
        pirate: Pirate,
        collider: Collider.Box[String],
        pirateViewState: PirateViewState,
        captain: Sprite[Material.ImageEffects],
        toScreenSpace: Vertex => Vertex
    ): Sprite[Material.ImageEffects] =
      val onScreenBounds =
        BoundingBox(
          toScreenSpace(collider.position),
          toScreenSpace(collider.bounds.size)
        )

      val position =
        Vertex(onScreenBounds.center.x, onScreenBounds.bottom).toPoint

      pirate.state match
        case PirateState.Idle if pirateViewState.facingRight =>
          captain
            .moveTo(position)
            .changeCycle(CycleLabel("Idle"))
            .play()

        case PirateState.Idle =>
          captain
            .moveTo(position)
            .flipHorizontal(true)
            .moveBy(-20, 0)
            .changeCycle(CycleLabel("Idle"))
            .play()

        case PirateState.MoveLeft =>
          captain
            .moveTo(position)
            .flipHorizontal(true)
            .moveBy(-20, 0)
            .changeCycle(CycleLabel("Run"))
            .play()

        case PirateState.MoveRight =>
          captain
            .moveTo(position)
            .changeCycle(CycleLabel("Run"))
            .play()

        case PirateState.FallingRight =>
          captain
            .moveTo(position)
            .changeCycle(CycleLabel("Fall"))
            .play()

        case PirateState.FallingLeft =>
          captain
            .moveTo(position)
            .flipHorizontal(true)
            .moveBy(-20, 0)
            .changeCycle(CycleLabel("Fall"))
            .play()

        case PirateState.JumpingRight =>
          captain
            .moveTo(position)
            .changeCycle(CycleLabel("Jump"))
            .play()

        case PirateState.JumpingLeft =>
          captain
            .moveTo(position)
            .flipHorizontal(true)
            .moveBy(-20, 0)
            .changeCycle(CycleLabel("Jump"))
            .play()
