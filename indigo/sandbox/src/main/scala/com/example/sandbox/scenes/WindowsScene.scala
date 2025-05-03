package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import example.TestFont
import indigo.*
import indigo.scenes.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*

object WindowsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  val name: SceneName =
    SceneName("WindowsScene")

  val modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepLatest

  val viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[SandboxGameModel]] =
    Set(
      WindowManager[Unit, SandboxGameModel, Int](
        id = SubSystemId("window-manager"),
        magnification = 2,
        snapGrid = Size.one,
        extractReference = _.num,
        startUpData = (),
        layerKey = Constants.LayerKeys.windows
      )
        .register(CustomUI.windowAnchoredTL)
        .register(CustomUI.windowAnchoredTC)
        .register(CustomUI.windowAnchoredTR)
        .register(CustomUI.windowAnchoredCL)
        .register(CustomUI.windowAnchoredC)
        .register(CustomUI.windowAnchoredCR)
        .register(CustomUI.windowAnchoredBL)
        .register(CustomUI.windowAnchoredBC)
        .register(CustomUI.windowAnchoredBR)
        .register(CustomUI.windowA.moveTo(15, 15))
        .register(CustomUI.windowB.moveTo(30, 30))
        .open(
          CustomUI.windowAnchoredTL.id,
          CustomUI.windowAnchoredTC.id,
          CustomUI.windowAnchoredTR.id,
          CustomUI.windowAnchoredCL.id,
          CustomUI.windowAnchoredC.id,
          CustomUI.windowAnchoredCR.id,
          CustomUI.windowAnchoredBL.id,
          CustomUI.windowAnchoredBC.id,
          CustomUI.windowAnchoredBR.id,
          CustomUI.windowA.id,
          CustomUI.windowB.id
        )
    )

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    case KeyboardEvent.KeyUp(Key.KEY_Q) =>
      Outcome(model).addGlobalEvents(WindowEvent.CloseFocused)

    case KeyboardEvent.KeyUp(Key.KEY_A) =>
      Outcome(model).addGlobalEvents(WindowEvent.Anchor(CustomUI.windowA.id, Anchor.BottomLeft))

    case KeyboardEvent.KeyUp(Key.KEY_M) =>
      Outcome(model).addGlobalEvents(WindowEvent.Move(CustomUI.windowA.id, Coords(10), Space.Screen))

    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model.copy(num = if model.num == 0 then 1 else 0))

    case _ =>
      Outcome(model)

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
    Outcome(
      SceneUpdateFragment(
        LayerKey("windows") -> Layer.empty
      )
    )

object CustomUI:

  val windowA: Window[ComponentGroup[Int], Int] =
    val id = WindowId("window a")

    Window(
      id = id,
      snapGrid = Size.one,
      minSize = Dimensions(128, 64),
      content = windowChrome(id, "Window - A")
    )
      .withBackground { windowContext =>
        Outcome(
          Layer.Content(
            Shape.Box(
              windowContext.bounds.unsafeToRectangle,
              if windowContext.hasFocus then Fill.Color(RGBA.SlateGray)
              else Fill.Color(RGBA.SlateGray.mix(RGBA.Black)),
              Stroke(1, RGBA.White)
            )
          )
        )
      }

  val windowB: Window[ComponentGroup[Int], Int] =
    val id = WindowId("window b")

    Window(
      id = id,
      snapGrid = Size.one,
      minSize = Dimensions(128, 64),
      content = windowChrome(id, "Window - B")
    )
      .withBackground { windowContext =>
        Outcome(
          Layer.Content(
            Shape.Box(
              windowContext.bounds.unsafeToRectangle,
              if windowContext.hasFocus then Fill.Color(RGBA.SlateGray)
              else Fill.Color(RGBA.SteelBlue.mix(RGBA.Black)),
              Stroke(1, RGBA.White)
            )
          )
        )
      }
      .withActiveCheck { context =>
        if context.reference == 0 then WindowActive.Active
        else WindowActive.InActive
      }

  def windowChrome(windowId: WindowId, title: String): ComponentGroup[Int] =
    ComponentGroup()
      .withBoundsMode(BoundsMode.inherit)
      .withLayout(ComponentLayout.Vertical(Padding(3, 1, 1, 1)))
      .anchor(
        content,
        Anchor.TopLeft.withPadding(Padding(20, 1, 1, 1))
      )
      .anchor(
        titleBar(title)
          .onDrag { (_: Int, dragData) =>
            Batch(
              WindowEvent
                .Move(
                  windowId,
                  dragData.position - dragData.offset,
                  Space.Screen
                )
            )
          }
          .reportDrag
          .withBoundsType(BoundsType.FillWidth(20, Padding(0))),
        Anchor.TopLeft
      )
      .anchor(
        resizeWindowButton.onDrag { (_: Int, dragData) =>
          Batch(
            WindowEvent
              .Resize(
                windowId,
                dragData.position.toDimensions,
                Space.Screen
              )
          )
        }.reportDrag,
        Anchor.BottomRight
      )
      .anchor(
        closeWindowButton
          .onClick(
            WindowEvent.Close(windowId)
          ),
        Anchor.TopRight
      )

  val text =
    Text(
      "",
      TestFont.fontKey,
      SandboxAssets.testFontMaterial
    )

  def content: MaskedPane[Label[Int], Int] =
    val label: Label[Int] =
      Label[Int](
        "Count: 0",
        (ctx, label) => Bounds(0, 0, 300, 100)
      ) { case (ctx, label) =>
        Outcome(
          Layer(
            text
              .withText(label.text(ctx))
              .moveTo(ctx.parent.coords.unsafeToPoint)
          )
        )
      }
        .withText((ctx: UIContext[Int]) => "Count: " + ctx.reference)

    MaskedPane(
      BoundsMode.offset(-2, -22),
      label
    )

  def titleBar(title: String): Button[Int] =
    Button[Int](Bounds(Dimensions(0))) { (ctx, btn) =>
      Outcome(
        Layer(
          Shape
            .Box(
              btn.bounds.unsafeToRectangle,
              Fill.Color(RGBA.SlateGray.mix(RGBA.Yellow).mix(RGBA.Black)),
              Stroke(1, RGBA.White)
            )
            .moveTo(ctx.parent.coords.unsafeToPoint),
          text
            .withText(title)
            .moveTo(ctx.parent.coords.unsafeToPoint + Point(4, 2))
        )
      )
    }

  def closeWindowButton: Button[Int] =
    val size = Size(20, 20)

    makeButton(size) { coords =>
      val innerBox = Rectangle(size).contract(4).moveTo(coords + Point(4))

      Batch(
        Shape.Line(innerBox.topLeft, innerBox.bottomRight, Stroke(2, RGBA.Black)),
        Shape.Line(innerBox.bottomLeft, innerBox.topRight, Stroke(2, RGBA.Black))
      )
    }

  def resizeWindowButton: Button[Int] =
    val size = Size(20, 20)

    makeButton(size) { coords =>
      val innerBox = Rectangle(size).contract(4).moveTo(coords + Point(4))

      Batch(
        Shape.Polygon(
          Batch(
            innerBox.bottomLeft,
            innerBox.bottomRight,
            innerBox.topRight
          ),
          Fill.Color(RGBA.Black)
        )
      )
    }

  def makeButton(size: Size)(extraNodes: Point => Batch[SceneNode]): Button[Int] =
    Button[Int](Bounds(Dimensions(size))) { (ctx, btn) =>
      Outcome(
        Layer(
          Shape
            .Box(
              btn.bounds.unsafeToRectangle,
              Fill.Color(RGBA.Magenta.mix(RGBA.Black)),
              Stroke(1, RGBA.Magenta)
            )
            .moveTo(ctx.parent.coords.unsafeToPoint)
        ).addNodes(extraNodes(ctx.parent.coords.unsafeToPoint))
      )
    }
      .presentDown { (ctx, btn) =>
        Outcome(
          Layer(
            Shape
              .Box(
                btn.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Cyan.mix(RGBA.Black)),
                Stroke(1, RGBA.Cyan)
              )
              .moveTo(ctx.parent.coords.unsafeToPoint)
          ).addNodes(extraNodes(ctx.parent.coords.unsafeToPoint))
        )
      }
      .presentOver((ctx, btn) =>
        Outcome(
          Layer(
            Shape
              .Box(
                btn.bounds.unsafeToRectangle,
                Fill.Color(RGBA.Yellow.mix(RGBA.Black)),
                Stroke(1, RGBA.Yellow)
              )
              .moveTo(ctx.parent.coords.unsafeToPoint)
          ).addNodes(extraNodes(ctx.parent.coords.unsafeToPoint))
        )
      )

  val windowAnchoredTL: Window[Unit, Int] =
    makeAnchoredWindow(WindowId("window TL"), Anchor.TopLeft.withPadding(Padding(5)))
  val windowAnchoredTC: Window[Unit, Int] = makeAnchoredWindow(WindowId("window TC"), Anchor.TopCenter)
  val windowAnchoredTR: Window[Unit, Int] = makeAnchoredWindow(WindowId("window TR"), Anchor.TopRight)
  val windowAnchoredCL: Window[Unit, Int] = makeAnchoredWindow(WindowId("window CL"), Anchor.CenterLeft)
  val windowAnchoredC: Window[Unit, Int]  = makeAnchoredWindow(WindowId("window C"), Anchor.Center)
  val windowAnchoredCR: Window[Unit, Int] = makeAnchoredWindow(WindowId("window CR"), Anchor.CenterRight)
  val windowAnchoredBL: Window[Unit, Int] = makeAnchoredWindow(WindowId("window BL"), Anchor.BottomLeft)
  val windowAnchoredBC: Window[Unit, Int] = makeAnchoredWindow(WindowId("window BC"), Anchor.BottomCenter)
  val windowAnchoredBR: Window[Unit, Int] =
    makeAnchoredWindow(WindowId("window BR"), Anchor.BottomRight.withPadding(Padding(5)))

  def makeAnchoredWindow(id: WindowId, achor: Anchor): Window[Unit, Int] =
    Window(
      id = id,
      snapGrid = Size.one,
      minSize = Dimensions(32, 32),
      content = ()
    )
      .withAnchor(achor)
      .withBackground { windowContext =>
        Outcome(
          Layer.Content(
            Shape.Box(
              windowContext.bounds.unsafeToRectangle,
              if windowContext.hasFocus then Fill.Color(RGBA.Yellow)
              else Fill.Color(RGBA.Red),
              Stroke(1, RGBA.Black)
            )
          )
        )
      }
