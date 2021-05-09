package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel

object TextBoxScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("textbox")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  val fishcakes: TextBox =
    TextBox("Fishcakes", 200, 100)
      .modifyStyle(_.withColor(RGBA.Magenta).modifyStroke(_.withWidth(Pixels(3)).withColor(RGBA.Cyan)))

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    val tb = TextBox("Indigo... with fonts?", 200, 100)
      .modifyStyle(
        _.withColor(RGBA.White)
          .modifyStroke(_.withColor(RGBA.Red).withWidth(Pixels(0)))
      )
      .moveTo(50, 50)

    Outcome(
      SceneUpdateFragment(
        Layer(
          Graphic(
            Rectangle(0, 0, 40, 40),
            4,
            LightingAssets.junctionBoxMaterialOn.modifyLighting(_ => LightingModel.Unlit)
          ).moveTo(10, 10),
          Shape.Box(context.findBounds(tb).getOrElse(Rectangle.zero), Fill.Color(RGBA.Cyan.withAlpha(0.5))),
          tb.withDepth(Depth(3)).modifyStyle(_.bold),
          tb.moveTo(50, 65).withDepth(Depth(3)),
          tb.moveTo(50, 80)
            .withDepth(Depth(3))
            .modifyStyle(_.withFontFamily(FontFamily.cursive)),
          fishcakes
            .moveTo(Signal.Orbit(Point(70, 70), 20).affectTime(0.25).at(context.running).toPoint)
            .withDepth(Depth(2)),
          model.dude.dude.sprite.play().withDepth(Depth(1))
        )
      )
    )

}
