package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*

object ManyEventHandlers extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("many event handlers")

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

  val coords: Batch[Point] =
    // more - 2,800 @ 59fps
    val offset = 4
    Batch.fromIndexedSeq(
      (0 until 70).flatMap { x =>
        (0 until 40).map { y =>
          Point(x, y) * offset
        }
      }
    )
  // more - 1080 @ 59fps
  // val offset = 8
  // (0 until 40).flatMap { x =>
  //   (0 until 27).map { y =>
  //     Point(x, y) * offset
  //   }
  // }.toList
  // normal - 792 @ 40fps
  // val offset = 16
  // (0 until 33).flatMap { x =>
  //   (0 until 24).map { y =>
  //     Point(x, y) * offset
  //   }
  // }.toList

  def sprites(dude: Sprite[Material.ImageEffects]): Batch[Sprite[Material.ImageEffects]] =
    coords.map(pt => dude.moveTo(pt))

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          sprites(model.dude.dude.sprite.withRef(Point.zero).moveTo(Point.zero))
        ).withMagnification(1)
      )
    )
