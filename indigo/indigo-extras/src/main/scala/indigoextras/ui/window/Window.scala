package indigoextras.ui.window

import indigo.*
import indigoextras.ui.component.Component
import indigoextras.ui.components.datatypes.Anchor
import indigoextras.ui.components.datatypes.Padding
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

final case class Window[A, ReferenceData](
    id: WindowId,
    snapGrid: Size,
    position: WindowPosition,
    dimensions: Dimensions,
    content: A,
    component: Component[A, ReferenceData],
    hasFocus: Boolean,
    minSize: Dimensions,
    maxSize: Option[Dimensions],
    state: WindowState,
    background: WindowContext[ReferenceData] => Outcome[Layer],
    mode: WindowMode,
    activeCheck: UIContext[ReferenceData] => WindowActive
):

  def bounds(viewport: Size, magnification: Int): Bounds =
    position match
      case WindowPosition.Fixed(coords) =>
        Bounds(coords, dimensions)

      case WindowPosition.Anchored(anchor) =>
        Bounds(
          anchor.calculatePosition(Dimensions(viewport) / magnification, dimensions),
          dimensions
        )

  def withId(value: WindowId): Window[A, ReferenceData] =
    this.copy(id = value)

  def withCoords(value: Coords): Window[A, ReferenceData] =
    this.copy(position = WindowPosition.Fixed(value))
  def moveTo(position: Coords): Window[A, ReferenceData] =
    withCoords(position)
  def moveTo(x: Int, y: Int): Window[A, ReferenceData] =
    moveTo(Coords(x, y))

  def withAnchor(value: Anchor): Window[A, ReferenceData] =
    this.copy(position = WindowPosition.Anchored(value))

  def anchorTopLeft: Window[A, ReferenceData]      = withAnchor(Anchor.TopLeft)
  def anchorTopCenter: Window[A, ReferenceData]    = withAnchor(Anchor.TopCenter)
  def anchorTopRight: Window[A, ReferenceData]     = withAnchor(Anchor.TopRight)
  def anchorCenterLeft: Window[A, ReferenceData]   = withAnchor(Anchor.CenterLeft)
  def anchorCenter: Window[A, ReferenceData]       = withAnchor(Anchor.Center)
  def anchorCenterRight: Window[A, ReferenceData]  = withAnchor(Anchor.CenterRight)
  def anchorBottomLeft: Window[A, ReferenceData]   = withAnchor(Anchor.BottomLeft)
  def anchorBottomCenter: Window[A, ReferenceData] = withAnchor(Anchor.BottomCenter)
  def anchorBottomRight: Window[A, ReferenceData]  = withAnchor(Anchor.BottomRight)

  def modifyAnchor(f: Anchor => Anchor): Window[A, ReferenceData] =
    this.copy(
      position = position match
        case WindowPosition.Anchored(anchor) =>
          WindowPosition.Anchored(f(anchor))

        case WindowPosition.Fixed(_) =>
          position
    )

  def withAnchorPadding(padding: Padding): Window[A, ReferenceData] =
    modifyAnchor(_.withPadding(padding))

  def withDimensions(value: Dimensions): Window[A, ReferenceData] =
    val d = value.max(minSize)
    this.copy(dimensions = maxSize.fold(d)(_.min(d)))
  def resizeTo(size: Dimensions): Window[A, ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): Window[A, ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): Window[A, ReferenceData] =
    withDimensions(dimensions + amount)
  def resizeBy(x: Int, y: Int): Window[A, ReferenceData] =
    resizeBy(Dimensions(x, y))

  def withModel(value: A): Window[A, ReferenceData] =
    this.copy(content = value)

  def withFocus(value: Boolean): Window[A, ReferenceData] =
    this.copy(hasFocus = value)
  def focus: Window[A, ReferenceData] =
    withFocus(true)
  def blur: Window[A, ReferenceData] =
    withFocus(false)

  def withMinSize(min: Dimensions): Window[A, ReferenceData] =
    this.copy(minSize = min)
  def withMinSize(width: Int, height: Int): Window[A, ReferenceData] =
    this.copy(minSize = Dimensions(width, height))

  def withMaxSize(max: Dimensions): Window[A, ReferenceData] =
    this.copy(maxSize = Option(max))
  def withMaxSize(width: Int, height: Int): Window[A, ReferenceData] =
    this.copy(maxSize = Option(Dimensions(width, height)))
  def noMaxSize: Window[A, ReferenceData] =
    this.copy(maxSize = None)

  def withState(value: WindowState): Window[A, ReferenceData] =
    this.copy(state = value)
  def open: Window[A, ReferenceData] =
    withState(WindowState.Open)
  def close: Window[A, ReferenceData] =
    withState(WindowState.Closed)

  def isOpen: Boolean =
    state == WindowState.Open
  def isClosed: Boolean =
    state == WindowState.Closed

  def refresh(context: UIContext[ReferenceData]): Window[A, ReferenceData] =
    this.copy(
      content = component.refresh(
        context.withParentBounds(bounds(context.frame.viewport.toSize, context.magnification)),
        content
      )
    )

  def withBackground(present: WindowContext[ReferenceData] => Outcome[Layer]): Window[A, ReferenceData] =
    this.copy(background = present)

  def withWindowMode(value: WindowMode): Window[A, ReferenceData] =
    this.copy(mode = value)
  def modal: Window[A, ReferenceData] =
    withWindowMode(WindowMode.Modal)
  def standard: Window[A, ReferenceData] =
    withWindowMode(WindowMode.Standard)

  def withActiveCheck(check: UIContext[ReferenceData] => WindowActive): Window[A, ReferenceData] =
    this.copy(activeCheck = check)
  def makeActive: Window[A, ReferenceData] =
    withActiveCheck(_ => WindowActive.Active)
  def makeInActive: Window[A, ReferenceData] =
    withActiveCheck(_ => WindowActive.InActive)

object Window:

  def apply[A, ReferenceData](
      id: WindowId,
      snapGrid: Size,
      minSize: Dimensions,
      content: A
  )(using c: Component[A, ReferenceData]): Window[A, ReferenceData] =
    Window(
      id,
      snapGrid,
      WindowPosition.Fixed(Coords.zero),
      minSize,
      content,
      c,
      false,
      minSize,
      None,
      WindowState.Closed,
      _ => Outcome(Layer.empty),
      WindowMode.Standard,
      _ => WindowActive.Active
    )

  def apply[A, ReferenceData](
      id: WindowId,
      snapGrid: Size,
      minSize: Dimensions,
      content: A
  )(
      background: WindowContext[ReferenceData] => Outcome[Layer]
  )(using c: Component[A, ReferenceData]): Window[A, ReferenceData] =
    Window(
      id,
      snapGrid,
      WindowPosition.Fixed(Coords.zero),
      minSize,
      content,
      c,
      false,
      minSize,
      None,
      WindowState.Closed,
      background,
      WindowMode.Standard,
      _ => WindowActive.Active
    )

  def updateModel[A, ReferenceData](
      context: UIContext[ReferenceData],
      window: Window[A, ReferenceData]
  ): GlobalEvent => Outcome[Window[A, ReferenceData]] =
    case e =>
      val bounds =
        window.bounds(context.frame.viewport.toSize, context.magnification)
      val minBounds =
        bounds.withDimensions(bounds.dimensions.max(window.minSize))

      window.component
        .updateModel(
          context.withParentBounds(minBounds),
          window.content
        )(e)
        .map(m => window.withModel(m).withDimensions(minBounds.dimensions))
