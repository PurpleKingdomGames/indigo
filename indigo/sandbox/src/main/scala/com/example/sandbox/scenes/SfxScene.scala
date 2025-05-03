package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.Log
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import example.TestFont
import indigo.*
import indigo.scenes.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*

object SfxScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  val name: SceneName =
    SceneName("SfxScene scene")

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

    case Log(msg) =>
      println(msg)
      Outcome(model)

    case e =>
      val ctx =
        UIContext(context.toContext, context.frame.globalMagnification)

      model.sfxComponents.update(ctx)(e).map { cl =>
        model.copy(sfxComponents = cl)
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
    model.sfxComponents
      .present(UIContext(context.toContext, context.frame.globalMagnification))
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

object SfxComponents:

  private val text =
    Text("", TestFont.fontKey, SandboxAssets.testFontMaterial)

  def components: ComponentGroup[Unit] =
    ComponentGroup(BoundsMode.fixed(200, 300))
      .withLayout(ComponentLayout.Vertical(Padding(4)))
      .add(
        makeLabel("Playback policy")
      )
      .add(
        ComponentGroup(BoundsMode.fit)
          .withLayout(ComponentLayout.Horizontal(Padding(4)))
          .add(
            makeButton
              .onClick(
                Log("Stop all"),
                PlaySound(SandboxAssets.jumpSound, Volume.Max, PlaybackPolicy.StopAll)
              )
          )
          .add(
            makeLabel("Stop all")
          )
      )
      .add(
        ComponentGroup(BoundsMode.fit)
          .withLayout(ComponentLayout.Horizontal(Padding(4)))
          .add(
            makeButton
              .onClick(
                Log("Stop previous same"),
                PlaySound(SandboxAssets.jumpSound, Volume.Max, PlaybackPolicy.StopPreviousSame)
              )
          )
          .add(
            makeLabel("Stop previous same")
          )
      )
      .add(
        ComponentGroup(BoundsMode.fit)
          .withLayout(ComponentLayout.Horizontal(Padding(4)))
          .add(
            makeButton
              .onClick(
                Log("Continue"),
                PlaySound(SandboxAssets.jumpSound, Volume.Max, PlaybackPolicy.Continue)
              )
          )
          .add(
            makeLabel("Continue")
          )
      )

  def makeLabel(labelText: String): Label[Unit] =
    Label[Unit](
      labelText,
      (ctx, label) => Bounds(ctx.services.bounds.get(text.withText(label)))
    ) { case (ctx, label) =>
      Outcome(
        Layer(
          text
            .withText(label.text(ctx))
            .moveTo(ctx.parent.coords.unsafeToPoint)
        )
      )
    }

  def makeButton: Button[Unit] =
    Button[Unit](Bounds(32, 32)) { (context, button) =>
      Outcome(
        Layer(
          Shape
            .Box(
              button.bounds.unsafeToRectangle,
              Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
              Stroke(1, RGBA.Magenta)
            )
            .moveTo(context.parent.coords.unsafeToPoint)
        )
      )
    }
      .presentDown { (context, button) =>
        Outcome(
          Layer(
            Shape
              .Box(
                button.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                Stroke(1, RGBA.Cyan)
              )
              .moveTo(context.parent.coords.unsafeToPoint)
          )
        )
      }
      .presentOver((context, button) =>
        Outcome(
          Layer(
            Shape
              .Box(
                button.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                Stroke(1, RGBA.Yellow)
              )
              .moveTo(context.parent.coords.unsafeToPoint)
          )
        )
      )
      .onClick(Log("Button clicked"))
      .onPress(Log("Button pressed"))
      .onRelease(Log("Button released"))
