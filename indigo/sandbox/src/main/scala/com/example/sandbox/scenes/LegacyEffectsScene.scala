package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigoextras.effectmaterials.Border
import indigoextras.effectmaterials.Glow
import indigoextras.effectmaterials.LegacyEffects
import indigoextras.effectmaterials.Thickness

object LegacyEffectsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("legacy effects")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  val graphic: Graphic[LegacyEffects] =
    Graphic(Rectangle(0, 0, 40, 40), SandboxAssets.junctionBoxEffectsMaterial)
      .withRef(20, 20)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] = {
    val viewCenter: Point = context.startUpData.viewportCenter + Point(0, -25)

    Outcome(
      SceneUpdateFragment(
        graphic // tint - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(0, -40)
          .modifyMaterial(_.withTint(RGBA.Red)),
        graphic // alpha - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(-60, -40)
          .modifyMaterial(_.withAlpha(0.5)),
        graphic // saturation - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(-30, -40)
          .modifyMaterial(_.withSaturation(0.0)),
        graphic // color overlay - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(30, -40)
          .modifyMaterial(_.withOverlay(Fill.Color(RGBA.Magenta.withAmount(0.75)))),
        graphic // linear gradient overlay - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(60, -40)
          .modifyMaterial(
            _.withOverlay(Fill.LinearGradient(Point.zero, RGBA.Magenta, Point(40), RGBA.Cyan.withAmount(0.5)))
          ),
        graphic // radial gradient overlay - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(-60, 10)
          .modifyMaterial(
            _.withOverlay(Fill.RadialGradient(Point(20), 10, RGBA.Magenta.withAmount(0.5), RGBA.Cyan.withAmount(0.25)))
          ),
        graphic // inner glow
          .moveTo(viewCenter)
          .moveBy(0, 10)
          .modifyMaterial(_.withGlow(Glow(RGBA.Green, 2.0, 0.0))),
        graphic // outer glow
          .moveTo(viewCenter)
          .moveBy(-30, 10)
          .modifyMaterial(_.withGlow(Glow(RGBA.Blue, 0.0, 2.0))),
        graphic // inner border
          .moveTo(viewCenter)
          .moveBy(30, 60)
          .modifyMaterial(_.withBorder(Border(RGBA(1.0, 0.5, 0.0, 1.0), Thickness.Thick, Thickness.None))),
        graphic // outer border
          .moveTo(viewCenter)
          .moveBy(60, 60)
          .modifyMaterial(_.withBorder(Border(RGBA.Yellow, Thickness.None, Thickness.Thick))),
        graphic // rotate & scale - standard transform
          .moveTo(viewCenter)
          .moveBy(30, 10)
          .rotateBy(Radians(0.2))
          .scaleBy(1.25, 1.25),
        graphic // flipped - standard transform
          .moveTo(viewCenter)
          .moveBy(60, 10)
          .flipHorizontal(true)
          .flipVertical(true)
      )
    )
  }

}
