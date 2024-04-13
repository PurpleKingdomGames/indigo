package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._

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
            4,
            LightingAssets.junctionBoxMaterialOn.modifyLighting(_ => LightingModel.Unlit)
          ).moveTo(10, 10),
          Shape.Box(context.findBounds(tb).getOrElse(Rectangle.zero), Fill.None).withStroke(Stroke(1, RGBA.Cyan)),
          tb.withDepth(Depth(3)).bold,
          tb.moveTo(50, 65).withDepth(Depth(3)),
          tb.moveTo(50, 80)
            .withDepth(Depth(3))
            .withFontFamily(FontFamily.cursive)
            .withFontSize(Pixels(16))
            .withStroke(TextStroke(RGBA.Red, Pixels(1))),
          hello
            .modifyStyle(_.withSize(Pixels(20)))
            .moveTo(Signal.Orbit(Point(180, 70), 20).affectTime(0.25).at(context.running).toPoint)
            .withDepth(Depth(2)),
          model.dude.dude.sprite.play().withDepth(Depth(1)),
          tb.moveTo(50, 120).withDepth(Depth(3)).alignLeft,
          tb.moveTo(50, 135).withDepth(Depth(3)).alignCenter.withFontSize(Pixels(8)),
          tb.moveTo(50, 150).withDepth(Depth(3)).alignRight,
          Shape
            .Box(Rectangle(50, 120, tb.size.width, 14 * 3), Fill.None)
            .withStroke(Stroke(1, RGBA.Cyan))
        )
      )
    )

}
