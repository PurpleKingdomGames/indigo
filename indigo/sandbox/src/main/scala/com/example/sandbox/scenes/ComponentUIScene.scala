package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.shared.subsystems.SubSystemContext.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*

object ComponentUIScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  val name: SceneName =
    SceneName("ComponentUI scene")

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
      val ctx =
        UIContext(context.toContext.forSubSystems.copy(reference = model.num), Size(1), 1)
      summon[Component[ComponentGroup[Int], Int]].updateModel(ctx, model.components)(e).map { cl =>
        model.copy(components = cl)
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
    model.components
      .present(UIContext(context.toContext.forSubSystems.copy(reference = 0), Size(1), 1))
      .map {
        case l: Layer.Stack =>
          SceneUpdateFragment(
            Constants.LayerKeys.game -> Layer.Stack(
              l.layers.map {
                case l: Layer.Content => l.withMagnification(1)
                case l                => l
              }
            )
          )

        case l: Layer.Content =>
          SceneUpdateFragment.empty
      }

final case class ChangeValue(value: Int) extends GlobalEvent
