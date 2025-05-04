package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*

object LightsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("lights")

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

  val graphic: Graphic[Material.Bitmap] =
    Graphic(Rectangle(0, 0, 40, 40), LightingAssets.junctionBoxMaterialOn)
      .withCrop(0, 0, 275, 200)
      .modifyMaterial(_.withFillType(FillType.Tile))

  val graphic2: Graphic[Material.ImageEffects] =
    graphic
      .withRef(20, 20)
      .moveTo((Point(550, 400) / 2 / 2) + Point(10))
      .withMaterial(
        LightingAssets.junctionBoxEffects
          .withOverlay(Fill.Color(RGBA.Magenta))
      )

  val shape =
    Shape
      .Polygon(
        Fill.LinearGradient(Point(0), RGBA.Magenta, Point(45), RGBA.Cyan),
        Stroke(4, RGBA.Black.withAlpha(0.75))
      )(
        Point(10, 10),
        Point(20, 70),
        Point(90, 90),
        Point(70, 20)
      )
      .moveTo(175, 10)
      .withLighting(LightingModel.Lit.flat)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    val centerPoint: Point =
      Point(550, 400) / 2 / 2

    Outcome(
      SceneUpdateFragment(graphic, graphic2, shape)
        .withMagnification(2)
        .withLights(
          AmbientLight(RGBA.Blue.withAlpha(0.1)),
          DirectionLight(RGBA.Cyan.withAmount(0.1), RGBA.Cyan, Radians.zero),
          PointLight.default
            .withSpecular(RGBA.White)
            .moveTo(context.frame.input.mouse.position)
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
                .Orbit(centerPoint, 80, Radians(0))
                .affectTime(0.1)
                .at(context.frame.time.running)
                .toPoint
            )
            .modifyFalloff(_.withRange(0, 50)),
          PointLight.default
            .withColor(RGBA.Green)
            .withSpecular(RGBA.Green.mix(RGBA.White))
            .withIntensity(1)
            .moveTo(
              Signal
                .Orbit(centerPoint, 80, Radians(Radians.TAU.toDouble / 3))
                .affectTime(0.1)
                .at(context.frame.time.running)
                .toPoint
            )
            .modifyFalloff(_.withRange(0, 50)),
          PointLight.default
            .withColor(RGBA.Blue)
            .withSpecular(RGBA.Blue.mix(RGBA.White))
            .withIntensity(1)
            .moveTo(
              Signal
                .Orbit(centerPoint, 80, Radians(Radians.TAU.toDouble / 3 * 2))
                .affectTime(0.1)
                .at(context.frame.time.running)
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
