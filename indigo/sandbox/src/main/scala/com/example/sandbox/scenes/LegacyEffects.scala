package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxAssets

object LegacyEffectsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("legacy effects")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, SandboxAssets.junctionBoxMaterial)
      .withRef(20, 20)

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] = {
    val viewCenter: Point = context.startUpData.viewportCenter + Point(0, -25)

    Outcome(
      SceneUpdateFragment(
        graphic
          .moveTo(viewCenter),
        //   .withTint(RGBA.Magenta),
        graphic
          .moveTo(viewCenter)
          .moveBy(-60, 0),
        // .withOverlay(Overlay.Color(RGBA.Magenta.withAmount(0.75))),
        graphic
          .moveTo(viewCenter)
          .moveBy(-30, 0),
        // .withOverlay(
        // Overlay.LinearGradiant(Point.zero, RGBA.Magenta, Point(64, 64), RGBA.Cyan.withAmount(0.5))
        // ),
        graphic
          .moveTo(viewCenter)
          .moveBy(30, 0),
        // .withBorder(Border(RGBA.Yellow, Thickness.Thick, Thickness.None)),
        graphic
          .moveTo(viewCenter)
          .moveBy(60, 0),
        // .withBorder(Border(RGBA.Red, Thickness.None, Thickness.Thick)),
        graphic
          .moveTo(viewCenter)
          .moveBy(-60, 50),
        // .withBorder(Border(RGBA(1.0, 0.5, 0.0, 1.0), Thickness.Thick, Thickness.Thick)),
        graphic
          .moveTo(viewCenter)
          .moveBy(0, 50),
        // .withGlow(Glow(RGBA.Green, 2.0, 0.0)),
        graphic
          .moveTo(viewCenter)
          .moveBy(-30, 50),
        // .withGlow(Glow(RGBA.Blue, 0.0, 2.0)),
        graphic
          .moveTo(viewCenter)
          .moveBy(30, 50),
        // .withGlow(Glow(RGBA.Cyan, 2.0, 2.0)),
        graphic
          .moveTo(viewCenter)
          .withRef(32, 32)
          .moveBy(48, 39)
        // .withAlpha(0.5)
        // .flipHorizontal(true)
        // .flipVertical(true)
      )
    )
  }

}
