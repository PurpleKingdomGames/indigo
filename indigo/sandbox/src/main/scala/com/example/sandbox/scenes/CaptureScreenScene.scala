package com.example.sandbox.scenes

import com.example.sandbox.DudeDown
import com.example.sandbox.DudeIdle
import com.example.sandbox.DudeLeft
import com.example.sandbox.DudeRight
import com.example.sandbox.DudeUp
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGame
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.platform.renderer.ScreenCaptureConfig
import indigo.scenes.*
import indigo.shared.assets.AssetTypePrimitive
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Shape.Box
import indigo.syntax.*

object CaptureScreenScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = ViewModel

  val uiKey        = LayerKey("ui")
  val defaultKey   = LayerKey("default")
  val dudeCloneId  = CloneId("Dude")
  val clippingRect = Rectangle(25, 25, 150, 100)

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, ViewModel] =
    Lens(
      _.captureScreenScene,
      (m, vm) => m.copy(captureScreenScene = vm)
    )

  def name: SceneName =
    SceneName("captureScreen")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] = _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] = {
    case MouseEvent.Click(x, y) if x >= 250 && x <= 266 && y >= 165 && y <= 181 =>
      val screenshots: Set[AssetType] =
        // Capture 2 screenshots, 1 of the full screen and the other of the clipping rectangle
        // These are reduced by 0.125 so that it sits a quarter size of the real screen without further scaling
        context.services.screen
          .capture(
            Batch(
              // Get the full screen and scale it
              ScreenCaptureConfig.default
                .withName("screenshot1")
                .withScale(0.5)
                .withExcludeLayers(Batch(uiKey)),
              // Get the screen inside the clipping rectangle and scale it. We don't remove the UI layer here
              ScreenCaptureConfig.default
                .withName("screenshot2")
                .withScale(0.5)
                .withCrop(clippingRect)
            )
          )
          .collect { case Right(image) => image }
          .toSet

      // Output each image data URL to the console
      screenshots.foreach(a => IndigoLogger.info(a.asInstanceOf[AssetType.Image].path.toString()))

      Outcome(viewModel)
        .addGlobalEvents(
          AssetEvent.LoadAssetBatch(screenshots, BindingKey("captureScreen"), true)
        )
    case AssetEvent.AssetBatchLoaded(key, assets, loaded) if key == BindingKey("captureScreen") && loaded =>
      (assets.headOption, assets.drop(1).headOption) match {
        case (Some(image1: AssetTypePrimitive), Some(image2: AssetTypePrimitive)) =>
          Outcome(viewModel.copy(screenshot1 = Some(image1.name), screenshot2 = Some(image2.name)))
        case _ => Outcome(viewModel)
      }
    case _ => Outcome(viewModel)
  }

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    val screenshotScale = 0.3
    val viewPort        = context.startUpData.gameViewport.size / SandboxGame.magnificationLevel
    val bigRect         = Rectangle((viewPort.width * screenshotScale).toInt, (viewPort.height * screenshotScale).toInt)
    val smallRect = Rectangle(
      0,
      0,
      (clippingRect.width * screenshotScale).toInt,
      (clippingRect.height * screenshotScale).toInt
    )
    Outcome(
      SceneUpdateFragment(
        uiKey -> Layer(
          Batch(
            Graphic(Rectangle(0, 0, 16, 16), Material.Bitmap(SandboxAssets.cameraIcon)).moveTo(250, 165),
            Shape.Box(clippingRect, Fill.None, Stroke(1, RGBA.SlateGray))
          ) ++ ((viewModel.screenshot1, viewModel.screenshot2) match {
            case (Some(image1), Some(image2)) =>
              Batch(
                Graphic(
                  Rectangle(viewPort),
                  Material.Bitmap(image1)
                ).scaleBy(Vector2(screenshotScale))
                  .moveTo(viewPort.width - (viewPort.width * screenshotScale).toInt - 5, 5),
                Box(bigRect, Fill.None, Stroke(1, RGBA.Black))
                  .moveTo(viewPort.width - (viewPort.width * screenshotScale).toInt - 5, 5),
                Graphic(
                  clippingRect,
                  Material.Bitmap(image2)
                ).scaleBy(Vector2(screenshotScale))
                  .moveTo(
                    viewPort.width - (clippingRect.width * screenshotScale).toInt - 5,
                    (viewPort.height * screenshotScale).toInt + 10
                  ),
                Box(smallRect, Fill.None, Stroke(1, RGBA.Black))
                  .moveTo(
                    viewPort.width - (clippingRect.width * screenshotScale).toInt - 5,
                    (viewPort.height * screenshotScale).toInt + 10
                  )
              )
            case _ => Batch.empty
          })
        ),
        defaultKey -> Layer(gameLayer(model, viewModel))
      )
    )

  def gameLayer(currentState: SandboxGameModel, viewModel: ViewModel): Batch[SceneNode] =
    Batch(
      currentState.dude.walkDirection match {
        case d @ DudeLeft =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeRight =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeUp =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeDown =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeIdle =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()
      },
      currentState.dude.dude.sprite
        .moveBy(8, 10)
        .moveBy(viewModel.offset)
        .modifyMaterial(
          _.withAlpha(1)
            .withTint(RGBA.Green.withAmount(0.25))
            .withSaturation(1.0)
        ),
      currentState.dude.dude.sprite
        .moveBy(8, -10)
        .modifyMaterial(_.withAlpha(0.5).withTint(RGBA.Red.withAmount(0.75))),
      CloneBatch(dudeCloneId, CloneBatchData(16, 64, Radians.zero, -1.0, 1.0))
    )

  final case class ViewModel(
      screenshot1: Option[AssetName],
      screenshot2: Option[AssetName],
      offset: Point
  )
