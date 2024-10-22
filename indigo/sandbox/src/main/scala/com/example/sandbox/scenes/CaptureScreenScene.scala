package com.example.sandbox.scenes

import com.example.sandbox.DudeDown
import com.example.sandbox.DudeIdle
import com.example.sandbox.DudeLeft
import com.example.sandbox.DudeRight
import com.example.sandbox.DudeUp
import com.example.sandbox.Fonts
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.shared.assets.AssetTypePrimitive
import indigo.shared.datatypes.RGB.Green
import indigo.shared.scenegraph.Shape
import indigo.syntax.*
import org.scalajs.dom.document

object CaptureScreenScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = CaptureScreenSceneViewModel

  val uiKey        = BindingKey("ui")
  val defaultKey   = BindingKey("default")
  val dudeCloneId  = CloneId("Dude")
  val clippingRect = Rectangle(25, 25, 150, 100)

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, CaptureScreenSceneViewModel] =
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
      viewModel: CaptureScreenSceneViewModel
  ): GlobalEvent => Outcome[CaptureScreenSceneViewModel] = {
    case MouseEvent.Click(x, y) if x >= 250 && x <= 266 && y >= 165 && y <= 181 =>
      // Open a window with the captured image
      val image1 = context.captureScreen(Batch(uiKey)).toAsset
      val image2 = context.captureScreen(clippingRect.expand(Size(1, 1))).toAsset
      println(image1.toString())
      Outcome(viewModel)
        .addGlobalEvents(
          AssetEvent.LoadAssetBatch(Set(image1, image2), BindingKey("captureScreen"), true)
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
      viewModel: CaptureScreenSceneViewModel
  ): Outcome[SceneUpdateFragment] =
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
                  Rectangle(0, 0, context.startUpData.gameViewport.width, context.startUpData.gameViewport.height),
                  Material.Bitmap(image1)
                ),
                Graphic(
                  clippingRect.moveTo(0, 0),
                  Material.Bitmap(image2)
                )
              )
            case _ => Batch.empty
          })
        ),
        defaultKey -> Layer(gameLayer(model, viewModel))
      )
    )

  def gameLayer(currentState: SandboxGameModel, viewModel: CaptureScreenSceneViewModel): Batch[SceneNode] =
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

  final case class CaptureScreenSceneViewModel(
      screenshot1: Option[AssetName],
      screenshot2: Option[AssetName],
      offset: Point
  )
