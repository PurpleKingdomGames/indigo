package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*

object TextBoxScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("textbox")

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

  val hello: TextBox =
    TextBox("Hello!", 200, 100)
      .modifyStyle(_.withColor(RGBA.Magenta).modifyStroke(_.withWidth(Pixels(3)).withColor(RGBA.Cyan)))

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    val tb = TextBox("Indigo... with fonts?", 200, 30)
      .withColor(RGBA.White)
      .withFontFamily(FontFamily(SandboxAssets.pixelFont.toString))
      .moveTo(50, 50)

    Outcome(
      SceneUpdateFragment(
        Layer(
          tb.moveTo(0, 0),
          Graphic(
            Rectangle(0, 0, 40, 40),
            LightingAssets.junctionBoxMaterialOn.modifyLighting(_ => LightingModel.Unlit)
          ).moveTo(10, 10),
          Shape.Box(context.services.bounds.get(tb), Fill.None).withStroke(Stroke(1, RGBA.Cyan)),
          tb.bold,
          tb.moveTo(50, 65),
          tb.moveTo(50, 80)
            .withFontFamily(FontFamily.cursive)
            .withFontSize(Pixels(16))
            .withStroke(TextStroke(RGBA.Red, Pixels(1))),
          hello
            .modifyStyle(_.withSize(Pixels(20)))
            .moveTo(Signal.Orbit(Point(180, 70), 20).affectTime(0.25).at(context.frame.time.running).toPoint),
          model.dude.dude.sprite.play(),
          tb.moveTo(50, 120).alignLeft,
          tb.moveTo(50, 135).alignCenter.withFontSize(Pixels(8)),
          tb.moveTo(50, 150).alignRight,
          Shape
            .Box(Rectangle(50, 120, tb.size.width, 14 * 3), Fill.None)
            .withStroke(Stroke(1, RGBA.Cyan))
        )
      )
    )

}
