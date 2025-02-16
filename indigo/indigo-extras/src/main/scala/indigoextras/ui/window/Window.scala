package indigoextras.ui.window

import indigo.*
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

final case class Window[A, ReferenceData](
    id: WindowId,
    snapGrid: Size,
    bounds: Bounds,
    content: A,
    component: Component[A, ReferenceData],
    hasFocus: Boolean,
    minSize: Dimensions,
    maxSize: Option[Dimensions],
    state: WindowState,
    background: WindowContext => Outcome[Layer],
    mode: WindowMode
):

  def withId(value: WindowId): Window[A, ReferenceData] =
    this.copy(id = value)

  def withBounds(value: Bounds): Window[A, ReferenceData] =
    this.copy(bounds = value)

  def withPosition(value: Coords): Window[A, ReferenceData] =
    withBounds(bounds.moveTo(value))
  def moveTo(position: Coords): Window[A, ReferenceData] =
    withPosition(position)
  def moveTo(x: Int, y: Int): Window[A, ReferenceData] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): Window[A, ReferenceData] =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): Window[A, ReferenceData] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): Window[A, ReferenceData] =
    val d = value.max(minSize)
    withBounds(bounds.withDimensions(maxSize.fold(d)(_.min(d))))
  def resizeTo(size: Dimensions): Window[A, ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): Window[A, ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): Window[A, ReferenceData] =
    withDimensions(bounds.dimensions + amount)
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
    this.copy(content =
      component.refresh(
        context.withParentBounds(bounds),
        content
      )
    )

  def withBackground(present: WindowContext => Outcome[Layer]): Window[A, ReferenceData] =
    this.copy(background = present)

  def withWindowMode(value: WindowMode): Window[A, ReferenceData] =
    this.copy(mode = value)
  def modal: Window[A, ReferenceData] =
    withWindowMode(WindowMode.Modal)
  def standard: Window[A, ReferenceData] =
    withWindowMode(WindowMode.Standard)

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
      Bounds(Coords.zero, minSize),
      content,
      c,
      false,
      minSize,
      None,
      WindowState.Closed,
      _ => Outcome(Layer.empty),
      WindowMode.Standard
    )

  def apply[A, ReferenceData](
      id: WindowId,
      snapGrid: Size,
      minSize: Dimensions,
      content: A
  )(
      background: WindowContext => Outcome[Layer]
  )(using c: Component[A, ReferenceData]): Window[A, ReferenceData] =
    Window(
      id,
      snapGrid,
      Bounds(Coords.zero, minSize),
      content,
      c,
      false,
      minSize,
      None,
      WindowState.Closed,
      background,
      WindowMode.Standard
    )

  def updateModel[A, ReferenceData](
      context: UIContext[ReferenceData],
      window: Window[A, ReferenceData]
  ): GlobalEvent => Outcome[Window[A, ReferenceData]] =
    case e =>
      val minBounds = window.bounds.withDimensions(window.bounds.dimensions.max(window.minSize))

      window.component
        .updateModel(
          context.withParentBounds(minBounds),
          window.content
        )(e)
        .map(m => window.withModel(m).withBounds(minBounds))
