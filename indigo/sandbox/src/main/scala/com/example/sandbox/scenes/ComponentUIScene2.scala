package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*

object ComponentUIScene2 extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  val name: SceneName =
    SceneName("ComponentUI scene 2")

  val modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepLatest

  val viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    case ChangeValue(value) =>
      Outcome(model.copy(num = value))

    case e =>
      val ctx = UIContext(context.toContext)

      model.button.update(ctx)(e).map { b =>
        model.copy(button = b)
      }

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    val ctx = UIContext(context.toContext)

    model.button
      .present(ctx)
      .map(l => SceneUpdateFragment(l))
