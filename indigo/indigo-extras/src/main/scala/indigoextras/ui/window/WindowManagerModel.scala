package indigoextras.ui.window

import indigo.*
import indigoextras.ui.components.datatypes.Anchor
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

final case class WindowManagerModel[ReferenceData](windows: Batch[Window[?, ReferenceData]]):
  def register(windowModels: Window[?, ReferenceData]*): WindowManagerModel[ReferenceData] =
    register(Batch.fromSeq(windowModels))
  def register(
      windowModels: Batch[Window[?, ReferenceData]]
  ): WindowManagerModel[ReferenceData] =
    this.copy(windows = windows ++ windowModels)

  def open(ids: WindowId*): Outcome[WindowManagerModel[ReferenceData]] =
    open(Batch.fromSeq(ids))

  def open(ids: Batch[WindowId]): Outcome[WindowManagerModel[ReferenceData]] =
    Outcome(
      this.copy(windows = windows.map(w => if ids.exists(_ == w.id) then w.open else w)),
      ids.filter(id => windows.exists(_.id == id)).map(WindowEvent.Opened.apply)
    )

  def close(id: WindowId): Outcome[WindowManagerModel[ReferenceData]] =
    Outcome(
      this.copy(windows = windows.map(w => if w.id == id then w.close else w)),
      Batch(WindowEvent.Closed(id))
    )

  def toggle(id: WindowId): Outcome[WindowManagerModel[ReferenceData]] =
    windows.find(_.id == id).map(_.isOpen) match
      case None =>
        Outcome(this)

      case Some(isOpen) =>
        Outcome(
          this.copy(
            windows = windows.map { w =>
              if w.id == id then if isOpen then w.close else w.open
              else w
            }
          ),
          Batch(if isOpen then WindowEvent.Closed(id) else WindowEvent.Opened(id))
        )

  def focusAt(
      coords: Coords,
      context: UIContext[ReferenceData]
  ): WindowManagerModel[ReferenceData] =
    val reordered =
      windows.reverse.find { w =>
        w.isOpen &&
        w.activeCheck(context).isActive &&
        w.bounds(context.frame.viewport.toSize, context.magnification).contains(coords)
      } match
        case None =>
          windows.map(_.blur)

        case Some(w) =>
          windows.filterNot(_.id == w.id).map(_.blur) :+ w.focus

    this.copy(windows = reordered)

  def focusOn(id: WindowId): WindowManagerModel[ReferenceData] =
    val reordered =
      windows.find(_.id == id) match
        case None =>
          windows

        case Some(w) =>
          windows.filterNot(_.id == w.id).map(_.blur) :+ w.focus

    this.copy(windows = reordered)

  def focused: Option[Window[?, ReferenceData]] =
    windows.find(_.hasFocus)

  def windowAt(coords: Coords, viewport: Size, magnification: Int): Option[WindowId] =
    windows.reverse.find(_.bounds(viewport, magnification).contains(coords)).map(_.id)

  def moveTo(
      id: WindowId,
      position: Coords,
      space: Space,
      viewport: Size,
      magnification: Int
  ): WindowManagerModel[ReferenceData] =
    this.copy(
      windows = windows.map { w =>
        if w.id == id then
          space match
            case Space.Screen =>
              w.moveTo(position)

            case Space.Window =>
              // The coords are relative to the window, so we need to adjust them to screen coords.
              w.moveTo(position + w.bounds(viewport, magnification).coords)
        else w
      }
    )

  def anchor(
      id: WindowId,
      anchor: Anchor
  ): WindowManagerModel[ReferenceData] =
    this.copy(
      windows = windows.map { w =>
        if w.id == id then w.withAnchor(anchor)
        else w
      }
    )

  def resizeTo(
      id: WindowId,
      dimensions: Dimensions,
      space: Space,
      viewport: Size,
      magnification: Int
  ): WindowManagerModel[ReferenceData] =
    this.copy(
      windows = windows.map { w =>
        if w.id == id then
          space match
            case Space.Screen =>
              // The dimensions are relative to the screen, so we need to adjust them to window dimensions.
              w.resizeTo(dimensions - w.bounds(viewport, magnification).coords.toDimensions)

            case Space.Window =>
              w.resizeTo(dimensions)
        else w
      }
    )

  def transformTo(
      id: WindowId,
      bounds: Bounds,
      space: Space,
      viewport: Size,
      magnification: Int
  ): WindowManagerModel[ReferenceData] =
    this.copy(
      windows = windows.map { w =>
        // Note: We do _not_ use .withBounds here because that won't do the min size checks.
        if w.id == id then
          space match
            case Space.Screen =>
              // See above (moveTo / resizeTo) for the reasoning behind these adjustments.
              w.moveTo(bounds.coords)
                .resizeTo(bounds.dimensions - w.bounds(viewport, magnification).coords.toDimensions)

            case Space.Window =>
              // See above (moveTo / resizeTo) for the reasoning behind these adjustments.
              w.moveTo(bounds.coords + w.bounds(viewport, magnification).coords).resizeTo(bounds.dimensions)
        else w
      }
    )

  def refresh(context: UIContext[ReferenceData], id: WindowId): Outcome[WindowManagerModel[ReferenceData]] =
    Outcome(
      this.copy(windows = windows.map(w => if w.id == id then w.refresh(context) else w))
    )

  def update(
      context: UIContext[ReferenceData],
      event: GlobalEvent
  ): Outcome[WindowManagerModel[ReferenceData]] =
    WindowManager.updateModel(context, this)(event)

object WindowManagerModel:
  def initial[ReferenceData]: WindowManagerModel[ReferenceData] =
    WindowManagerModel(Batch.empty)
