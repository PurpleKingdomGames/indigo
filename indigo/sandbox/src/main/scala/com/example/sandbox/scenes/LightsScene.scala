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
          DirectionLight(RGBA.Cyan.withAmount(0.1), RGBA.Cyan.withAmount(1.5), Radians.zero),
          PointLight.default
            .withSpecular(RGBA.White.withAmount(2.0))
            .moveTo(context.mouse.position)
            .withAttenuation(40),
          SpotLight.default
            .withColor(RGBA.Yellow)
            .moveTo(Point(10))
            .rotateBy(Radians.fromDegrees(135))
            .withSpecular(RGBA(1, 1, 0.3, 2.0))
            .withAttenuation(200)
            .withAngle(Radians.fromDegrees(45))
            .withNear(5)
            .withFar(200),
          PointLight.default
            .withColor(RGBA.Red)
            .withSpecular(RGBA.Red)
            .withAttenuation(60)
            .moveTo(
              Signal
                .Orbit(context.startUpData.viewportCenter, 80, Radians(0))
                .affectTime(0.3)
                .at(context.running)
                .toPoint
            ),
          PointLight.default
            .withColor(RGBA.Green)
            .withSpecular(RGBA.Green)
            .withAttenuation(60)
            .moveTo(
              Signal
                .Orbit(context.startUpData.viewportCenter, 80, Radians(Radians.TAU.value / 3))
                .affectTime(0.3)
                .at(context.running)
                .toPoint
            ),
          PointLight.default
            .withColor(RGBA.Blue)
            .withSpecular(RGBA.Blue)
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
