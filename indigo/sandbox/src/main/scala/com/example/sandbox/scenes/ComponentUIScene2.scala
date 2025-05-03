package com.example.sandbox.scenes

import com.example.sandbox.Fonts
import com.example.sandbox.Log
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
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

    case Log(msg) =>
      println(s"Log: $msg")
      Outcome(model)

    case e =>
      val ctx =
        UIContext(context.toContext, context.frame.globalMagnification)
          .withReferenceData(4)
          .withMagnification(2)

      for {
        s <- model.scrollPane.update(ctx.moveParentTo(50, 10))(e)
        b <- model.button.update(ctx.moveParentTo(10, 10))(e)
      } yield model.copy(button = b, scrollPane = s)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    case _ =>
      Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =

    val ctx =
      UIContext(context.toContext, context.frame.globalMagnification)
        .withReferenceData(4)
        .moveParentTo(50, 10)

    val scrollPaneBorder =
      Shape.Box(
        CustomComponents.scrollPaneBounds.unsafeToRectangle.moveTo(ctx.parent.coords.unsafeToPoint),
        Fill.None,
        Stroke(1, RGBA.Cyan)
      )

    for {
      s <- model.scrollPane.present(ctx)
      b <- model.button.present(ctx.moveParentTo(10, 10))
    } yield SceneUpdateFragment(b, s)
      .addLayer(Layer(scrollPaneBorder))

  object CustomComponents:

    val scrollPaneBounds = Bounds(0, 0, 200, 100)

    val text =
      Text(
        "",
        Fonts.fontKey,
        SandboxAssets.fontMaterial
      )

    val listOfLabels: ComponentList[Int] =
      ComponentList(Dimensions(200, 200)) { (ctx: UIContext[Int]) =>
        (1 to ctx.reference).toBatch.map { i =>
          ComponentId("lbl" + i) ->
            ComponentGroup[Int]()
              .withLayout(ComponentLayout.Horizontal(Padding.right(10)))
              .add(
                Button[Int](Bounds(16, 16)) { (ctx, btn) =>
                  Outcome(
                    Layer(
                      Shape
                        .Box(
                          Rectangle(
                            ctx.parent.bounds.unsafeToRectangle.position,
                            btn.bounds.dimensions.unsafeToSize
                          ),
                          Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
                          Stroke(1, RGBA.Magenta)
                        )
                    )
                  )
                }
                  .presentDown { (ctx, btn) =>
                    Outcome(
                      Layer(
                        Shape
                          .Box(
                            Rectangle(
                              ctx.parent.bounds.unsafeToRectangle.position,
                              btn.bounds.dimensions.unsafeToSize
                            ),
                            Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                            Stroke(1, RGBA.Cyan)
                          )
                      )
                    )
                  }
                  .presentOver { (ctx, btn) =>
                    Outcome(
                      Layer(
                        Shape
                          .Box(
                            Rectangle(
                              ctx.parent.bounds.unsafeToRectangle.position,
                              btn.bounds.dimensions.unsafeToSize
                            ),
                            Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                            Stroke(1, RGBA.Yellow)
                          )
                      )
                    )
                  }
                  .onClick(_ => Batch(Log("Clicked button: " + i)))
              )
              .add(
                Label[Int](
                  i.toString,
                  (_, label) => Bounds(0, 0, 250, 20)
                ) { case (ctx, label) =>
                  Outcome(
                    Layer(
                      text
                        .withText(label.text(ctx))
                        .moveTo(ctx.parent.coords.unsafeToPoint)
                    )
                  )
                }
              )
        }
      }
        .withLayout(ComponentLayout.Vertical(Padding(10)))

    val scrollButton: Button[Unit] =
      Button[Unit](Bounds(16, 16)) { (ctx, btn) =>
        Outcome(
          Layer(
            Shape
              .Box(
                Rectangle(
                  ctx.parent.bounds.unsafeToRectangle.position,
                  btn.bounds.dimensions.unsafeToSize
                ),
                Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
                Stroke(1, RGBA.Magenta)
              )
          )
        )
      }
        .presentDown { (ctx, btn) =>
          Outcome(
            Layer(
              Shape
                .Box(
                  Rectangle(
                    ctx.parent.bounds.unsafeToRectangle.position,
                    btn.bounds.dimensions.unsafeToSize
                  ),
                  Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                  Stroke(1, RGBA.Cyan)
                )
            )
          )
        }
        .presentOver { (ctx, btn) =>
          Outcome(
            Layer(
              Shape
                .Box(
                  Rectangle(
                    ctx.parent.bounds.unsafeToRectangle.position,
                    btn.bounds.dimensions.unsafeToSize
                  ),
                  Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                  Stroke(1, RGBA.Yellow)
                )
            )
          )
        }

    val pane: ScrollPane[ComponentList[Int], Int] =
      ScrollPane(
        BindingKey("scroll pane"),
        scrollPaneBounds.dimensions,
        listOfLabels,
        scrollButton
      )
        .withBackground { bounds =>
          Layer(
            Shape.Box(
              bounds.unsafeToRectangle,
              Fill.Color(RGBA.Black.withAlpha(0.5)),
              Stroke(1, RGBA.White)
            )
          )
        }
        .withScrollBackground { bounds =>
          Layer(
            Shape.Box(
              bounds.unsafeToRectangle,
              Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
              Stroke.None
            )
          )
        }
