package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxAssets

object LightsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("lights")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 40, 40), 1, LightingAssets.junctionBoxMaterialOn)
      .withRef(20, 20)

  val grid: List[Graphic] = {
    val rows    = 4
    val columns = 6
    val offset  = Point(0)

    (0 to rows).toList.flatMap { row =>
      (0 to columns).toList.map { column =>
        graphic.withRef(Point(0)).moveTo(Point(column * 40, row * 40) + offset)
      }
    }
  }

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayer(grid)
        .withMagnification(2)
        .withLights(
          AmbientLight(RGBA.Blue.withAlpha(0.2)),
          DirectionLight(0.1, RGB.Cyan, 0.8, RGB.Cyan, 1.5, Radians(-0.75)),
          PointLight.default
            .withSpecularColor(RGB.White)
            .withSpecularPower(2.0)
            .moveTo(context.mouse.position)
            .withAttenuation(40),
          SpotLight.default
            .withColor(RGB.Yellow)
            .moveTo(Point(10))
            .rotateBy(Radians.fromDegrees(45))
            .withHeight(1)
            .withPower(1.0)
            .withSpecularColor(RGB(1, 1, 0.3))
            .withSpecularPower(2)
            .withAttenuation(300),
          PointLight.default
            .withColor(RGB.Red)
            .withSpecularColor(RGB.Red)
            .withSpecularPower(1.0)
            .withAttenuation(60)
            .moveTo(
              Signal
                .Orbit(context.startUpData.viewportCenter, 80, Radians(0))
                .affectTime(0.3)
                .at(context.running)
                .toPoint
            ),
          PointLight.default
            .withColor(RGB.Green)
            .withSpecularColor(RGB.Green)
            .withSpecularPower(1.0)
            .withAttenuation(60)
            .moveTo(
              Signal
                .Orbit(context.startUpData.viewportCenter, 80, Radians(Radians.TAU.value / 3))
                .affectTime(0.3)
                .at(context.running)
                .toPoint
            ),
          PointLight.default
            .withColor(RGB.Blue)
            .withSpecularColor(RGB.Blue)
            .withSpecularPower(1.0)
            .withAttenuation(60)
            .moveTo(
              Signal
                .Orbit(context.startUpData.viewportCenter, 80, Radians(Radians.TAU.value / 3 * 2))
                .affectTime(0.3)
                .at(context.running)
                .toPoint
            )
        )
    )

}

object LightingAssets {

  val junctionBoxMaterialOn: Material.Lit =
    Material.Lit(
      SandboxAssets.junctionBoxAlbedo,
      SandboxAssets.junctionBoxEmission,
      SandboxAssets.junctionBoxNormal,
      SandboxAssets.junctionBoxRoughness
    )

}
