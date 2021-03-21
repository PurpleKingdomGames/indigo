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
    Graphic(Rectangle(0, 0, 40, 40), 1, SandboxAssets.junctionBoxMaterial)
      .withRef(20, 20)

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] = {
    val viewCenter: Point = context.startUpData.viewportCenter + Point(0, -25)

    Outcome(
      SceneUpdateFragment(
        graphic // tint
          .moveTo(viewCenter)
          .moveBy(0, -40),
        //   .withTint(RGBA.Magenta),
        graphic // alpha
          .moveTo(viewCenter)
          .moveBy(-60, -40),
        // .withOverlay(Overlay.Color(RGBA.Magenta.withAmount(0.75))),
        graphic // saturation
          .moveTo(viewCenter)
          .moveBy(-30, -40),
        // .withOverlay(
        // Overlay.LinearGradiant(Point.zero, RGBA.Magenta, Point(64, 64), RGBA.Cyan.withAmount(0.5))
        // ),
        graphic //color overlay
          .moveTo(viewCenter)
          .moveBy(30, -40),
        // .withBorder(Border(RGBA.Yellow, Thickness.Thick, Thickness.None)),
        graphic // linear gradient overlay
          .moveTo(viewCenter)
          .moveBy(60, -40),
        // .withBorder(Border(RGBA.Red, Thickness.None, Thickness.Thick)),
        graphic // radial gradient overlay
          .moveTo(viewCenter)
          .moveBy(-60, 10),
        // .withBorder(Border(RGBA(1.0, 0.5, 0.0, 1.0), Thickness.Thick, Thickness.Thick)),
        graphic // inner glow
          .moveTo(viewCenter)
          .moveBy(0, 10),
        // .withGlow(Glow(RGBA.Green, 2.0, 0.0)),
        graphic // outer glow
          .moveTo(viewCenter)
          .moveBy(-30, 10),
        // .withGlow(Glow(RGBA.Blue, 0.0, 2.0)),
        graphic // inner border
          .moveTo(viewCenter)
          .moveBy(30, 60),
        graphic // outer border
          .moveTo(viewCenter)
          .moveBy(60, 60),
        graphic // rotate & scale
          .moveTo(viewCenter)
          .moveBy(30, 10)
          .rotateBy(Radians(0.2))
          .scaleBy(1.25, 1.25),
        graphic // flipped
          .moveTo(viewCenter)
          .moveBy(60, 10)
          .flipHorizontal(true)
          .flipVertical(true)
      )
    )
  }

}
