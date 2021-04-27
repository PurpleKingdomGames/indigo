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
        .addLayer(
          grid :+
            graphic
              .moveTo(context.startUpData.viewportCenter + Point(10))
              .withMaterial(
                LightingAssets.junctionBoxEffects
                  .withOverlay(Fill.Color(RGBA.Magenta))
              ) :+
            Shape
              .Polygon(Fill.LinearGradient(Point(0), RGBA.Magenta, Point(45), RGBA.Cyan), Stroke(4, RGBA.Black.withAlpha(0.75)))(
                Point(10, 10),
                Point(20, 70),
                Point(90, 90),
                Point(70, 20)
              )
              .moveTo(175, 10)
              .withLighting(LightingModel.Lit.flat)
        )
        .withMagnification(2)
        .withLights(
          AmbientLight(RGBA.Blue.withAlpha(0.1)),
          DirectionLight(RGBA.Cyan.withAmount(0.1), RGBA.Cyan, Radians.zero),
          PointLight.default
            .withSpecular(RGBA.White)
            .moveTo(context.mouse.position)
            .withColor(RGBA.Red.mix(RGBA.White, 0.1))
            .withIntensity(2)
            .withFalloff(Falloff.smoothQuadratic.withRange(0, 80)),
          SpotLight.default
            .withColor(RGBA.Yellow)
            .moveTo(Point(10))
            .rotateBy(Radians.fromDegrees(135))
            .withSpecular(RGBA(1, 1, 0.3, 2.0))
            .withIntensity(1)
            .withAngle(Radians.fromDegrees(45))
            .modifyFalloff(_ => Falloff.smoothLinear.withRange(5, 200)),
          PointLight.default
            .withColor(RGBA.Red)
            .withSpecular(RGBA.Red.mix(RGBA.White))
            .withIntensity(1)
            .moveTo(
              Signal
                .Orbit(context.startUpData.viewportCenter, 80, Radians(0))
                .affectTime(0.1)
                .at(context.running)
                .toPoint
            )
            .modifyFalloff(_.withRange(0, 50)),
          PointLight.default
            .withColor(RGBA.Green)
            .withSpecular(RGBA.Green.mix(RGBA.White))
            .withIntensity(1)
            .moveTo(
              Signal
                .Orbit(context.startUpData.viewportCenter, 80, Radians(Radians.TAU.toDouble / 3))
                .affectTime(0.1)
                .at(context.running)
                .toPoint
            )
            .modifyFalloff(_.withRange(0, 50)),
          PointLight.default
            .withColor(RGBA.Blue)
            .withSpecular(RGBA.Blue.mix(RGBA.White))
            .withIntensity(1)
            .moveTo(
              Signal
                .Orbit(context.startUpData.viewportCenter, 80, Radians(Radians.TAU.toDouble / 3 * 2))
                .affectTime(0.1)
                .at(context.running)
                .toPoint
            )
            .modifyFalloff(_.withRange(0, 50))
        )
    )

}

object LightingAssets {

  val junctionBoxMaterialOn: Material.Bitmap =
    Material.Bitmap(
      SandboxAssets.junctionBoxAlbedo,
      LightingModel.Lit(
        SandboxAssets.junctionBoxEmission,
        SandboxAssets.junctionBoxNormal,
        SandboxAssets.junctionBoxRoughness
      )
    )

  val junctionBoxEffects: Material.ImageEffects =
    Material.ImageEffects(
      SandboxAssets.junctionBoxAlbedo,
      LightingModel.Lit(
        SandboxAssets.junctionBoxEmission,
        SandboxAssets.junctionBoxNormal,
        SandboxAssets.junctionBoxRoughness
      )
    )

}
