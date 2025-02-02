package indigoextras.ui.datatypes

import indigo.*
import indigo.scenes.SceneContext

final case class UIContext[ReferenceData](
    // Specific to UIContext
    bounds: Bounds,
    snapGrid: Size,
    pointerCoords: Coords,
    state: UIState,
    magnification: Int,
    additionalOffset: Coords,
    // The following are all the same as in SubSystemContext
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):
  lazy val screenSpaceBounds: Rectangle =
    bounds.toScreenSpace(snapGrid)

  val isActive: Boolean =
    state == UIState.Active

  def withBounds(newBounds: Bounds): UIContext[ReferenceData] =
    this.copy(bounds = newBounds)
  def moveTo(newPosition: Coords): UIContext[ReferenceData] =
    this.copy(bounds = bounds.moveTo(newPosition))
  def moveTo(x: Int, y: Int): UIContext[ReferenceData] =
    this.copy(bounds = bounds.moveTo(x, y))
  def moveBy(offset: Coords): UIContext[ReferenceData] =
    this.copy(bounds = bounds.moveBy(offset))
  def moveBy(x: Int, y: Int): UIContext[ReferenceData] =
    this.copy(bounds = bounds.moveBy(x, y))

  def withSnapGrid(newSnapGrid: Size): UIContext[ReferenceData] =
    this.copy(snapGrid = newSnapGrid)
  def clearSnapGrid: UIContext[ReferenceData] =
    this.copy(snapGrid = Size(1))

  def withPointerCoords(coords: Coords): UIContext[ReferenceData] =
    this.copy(pointerCoords = coords)

  def withState(newState: UIState): UIContext[ReferenceData] =
    this.copy(state = newState)
  def makeActive: UIContext[ReferenceData] =
    withState(UIState.Active)
  def makeInActive: UIContext[ReferenceData] =
    withState(UIState.InActive)

  def withMagnification(newMagnification: Int): UIContext[ReferenceData] =
    this.copy(magnification = newMagnification)

  def withAdditionalOffset(offset: Coords): UIContext[ReferenceData] =
    this.copy(additionalOffset = offset)
  def withAdditionalOffset(x: Int, y: Int): UIContext[ReferenceData] =
    withAdditionalOffset(Coords(x, y))

  def withReferenceData[NewReferenceData](newReference: NewReferenceData): UIContext[NewReferenceData] =
    this.copy(reference = newReference)
  def unitReference: UIContext[Unit] =
    this.copy(reference = ())

object UIContext:

  def apply[ReferenceData](
      subSystemContext: SubSystemContext[ReferenceData],
      snapGrid: Size,
      magnification: Int
  ): UIContext[ReferenceData] =
    val pointerCoords = Coords(subSystemContext.frame.input.pointers.position / snapGrid.toPoint)
    UIContext(
      Bounds.zero,
      snapGrid,
      pointerCoords,
      UIState.Active,
      magnification,
      Coords.zero,
      subSystemContext.reference,
      subSystemContext.frame,
      subSystemContext.services
    )

  def apply[ReferenceData](
      subSystemContext: SubSystemContext[ReferenceData],
      snapGrid: Size,
      magnification: Int,
      additionalOffset: Coords
  ): UIContext[ReferenceData] =
    val pointerCoords = Coords(subSystemContext.frame.input.pointers.position / snapGrid.toPoint)
    UIContext(
      Bounds.zero,
      snapGrid,
      pointerCoords,
      UIState.Active,
      magnification,
      additionalOffset,
      subSystemContext.reference,
      subSystemContext.frame,
      subSystemContext.services
    )

  def apply(ctx: Context[?]): UIContext[Unit] =
    fromContext(ctx, ())

  def fromContext[ReferenceData](
      ctx: Context[?],
      reference: ReferenceData
  ): UIContext[ReferenceData] =
    UIContext(
      Bounds.zero,
      Size(1),
      Coords.zero,
      UIState.Active,
      1,
      Coords.zero,
      reference,
      ctx.frame,
      ctx.services
    )

  def fromSceneContext[ReferenceData](
      ctx: SceneContext[?],
      reference: ReferenceData
  ): UIContext[ReferenceData] =
    fromContext(ctx.toContext, reference)

  def fromSubSystemContext[ReferenceData](
      ctx: SubSystemContext[?],
      reference: ReferenceData
  ): UIContext[ReferenceData] =
    fromContext(ctx.toContext, reference)

enum UIState derives CanEqual:
  case Active, InActive

  def isActive: Boolean =
    this match
      case UIState.Active   => true
      case UIState.InActive => false

  def isInActive: Boolean =
    !isActive
