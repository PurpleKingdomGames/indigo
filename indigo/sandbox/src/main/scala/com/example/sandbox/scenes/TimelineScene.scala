package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
import indigo.syntax.animations.*

object TimelineScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = Unit
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, Unit] =
    Lens.unit

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("timeline")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: FrameContext[SandboxStartupData],
      model: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[SandboxStartupData],
      model: Unit,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val crate: Graphic[Material.ImageEffects] =
    Graphic(64, 64, SandboxAssets.cratesMaterial)
      .modifyMaterial(_.toImageEffects.withLighting(LightingModel.Unlit))
      .withCrop(0, 0, 32, 32)

  val move: Graphic[Material.ImageEffects] => SignalFunction[Point, Graphic[Material.ImageEffects]] = g =>
    SignalFunction(pt => g.moveTo(pt))

  val modifier: Graphic[Material.ImageEffects] => SignalFunction[Seconds, Graphic[Material.ImageEffects]] =
    g => sin >>> SignalFunction(d => (d + 1) / 2) >>> SignalFunction(d => g.modifyMaterial(_.withAlpha(d)))

  val tl: Seconds => Timeline[Graphic[Material.ImageEffects]] = delay =>
    timeline(
      layer(
        startAfter(delay),
        animate(5.seconds) {
          easeInOut >>> lerp(Point(0), Point(100)) >>> move(_)
        },
        animate(3.seconds) {
          easeInOut >>> lerp(Point(100), Point(100, 0)) >>> move(_)
        }
      ),
      layer(
        startAfter(delay),
        animate(8.seconds, modifier)
      )
    )

  val spriteTimeline: Timeline[Sprite[Material.ImageEffects]] =
    val loopLength = 700.millis.toSeconds

    timeline(
      layer(
        animate(3.seconds) { sprite =>
          wrap(loopLength) >>> lerp(loopLength) >>> SignalFunction(d => sprite.scrubTo(d))
        },
        show(2.seconds)(_.changeCycle(CycleLabel("blink"))),
        animate(3.seconds) { sprite =>
          wrap(loopLength) >>> lerp(loopLength) >>> SignalFunction(d => sprite.scrubTo(d))
        }
      )
    )

  val clipTimeline: Timeline[Clip[Material.Bitmap]] =
    timeline(
      layer(
        animate(5.seconds) { clip =>
          wrap(clip.length) >>> lerp(0, 1, clip.length) >>> SignalFunction(d => clip.scrubTo(d))
        }
      )
    )

  val trafficLights =
    Clip(Point(0), Size(64), ClipSheet(3, Seconds(0.25), 2), Material.Bitmap(SandboxAssets.trafficLightsName))
      .moveTo(50, 0)

  def present(
      context: FrameContext[SandboxStartupData],
      model: Unit,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    val dude = context.startUpData.dude.sprite.changeCycle(CycleLabel("walk right")).moveTo(32, 32)

    Outcome(
      SceneUpdateFragment(
        tl(2.seconds).at(context.running)(crate).toBatch ++
          spriteTimeline
            .at(context.running)(dude)
            .toBatch ++
          clipTimeline
            .at(context.running)(trafficLights)
            .toBatch
      )
    )
