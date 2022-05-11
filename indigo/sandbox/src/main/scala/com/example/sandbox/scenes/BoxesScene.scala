package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._

object BoxesScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("boxes")

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

  val boxStroke  = Stroke(1, RGBA.Black)
  val lineStroke = Stroke(1, RGBA.Cyan)
  val fill       = Fill.Color(RGBA.Zero)

  def makeLine(dice: Dice): Shape.Line =
    if dice.rollBoolean then
      // horizonal
      val y = 5 + dice.roll(290)
      Shape.Line(Point(5 + dice.roll(435), y), Point(5 + dice.roll(435), y), lineStroke)
    else
      // vertical
      val x = 5 + dice.roll(435)
      Shape.Line(Point(x, dice.roll(290)), Point(x, dice.roll(290)), lineStroke)

  def makeBox(dice: Dice): Shape.Box =
    Shape.Box(
      Rectangle(Point(5 + dice.roll(435), 5 + dice.roll(290)), Size(dice.roll(100), dice.roll(100))),
      fill,
      boxStroke
    )

  val shapes: Batch[Shape[_]] =
    val d = Dice.fromSeed(0)
    Batch.fromList(
      (1 to 30).toList.flatMap { _ =>
        List(makeLine(d), makeBox(d))
      }
    )

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment(Layer(shapes).withMagnification(1)))
