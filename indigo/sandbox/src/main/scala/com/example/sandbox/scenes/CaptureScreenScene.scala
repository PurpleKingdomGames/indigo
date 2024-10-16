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
import indigo.shared.datatypes.RGB.Green
import indigo.shared.scenegraph.Shape
import indigo.syntax.*
import org.scalajs.dom.document

object CaptureScreenScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  val uiKey        = BindingKey("ui")
  val defaultKey   = BindingKey("default")
  val dudeCloneId  = CloneId("Dude")
  val clippingRect = Rectangle(25, 25, 150, 100)

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("captureScreen")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] = {
    case MouseEvent.Click(x, y) if x >= 250 && x <= 266 && y >= 165 && y <= 181 =>
      // Open a window with the captured image
      println(context.captureScreen(Batch(uiKey)).getDataUrl)
      println(context.captureScreen(clippingRect).getDataUrl)
      Outcome(model)
    case _ => Outcome(model)
  }

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        uiKey -> Layer(
          Graphic(Rectangle(0, 0, 16, 16), Material.Bitmap(SandboxAssets.cameraIcon)).moveTo(250, 165),
          Shape.Box(clippingRect, Fill.None, Stroke(1, RGBA.SlateGray))
        ),
        defaultKey -> Layer(gameLayer(model, viewModel))
      )
    )

  def gameLayer(currentState: SandboxGameModel, viewModel: SandboxViewModel): Batch[SceneNode] =
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
