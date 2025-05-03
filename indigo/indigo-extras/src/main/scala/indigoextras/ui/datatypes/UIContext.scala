package indigoextras.ui.datatypes

import indigo.*
import indigo.scenes.SceneContext

final case class UIContext[ReferenceData](
    // Specific to UIContext
    parent: Parent,
    snapGrid: Size,
    _pointerCoords: Coords,
    state: UIState,
    magnification: Int,
    // The following are all the same as in SubSystemContext
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):

  lazy val isActive: Boolean =
    state == UIState.Active

  lazy val pointerCoords: Coords =
    Coords(_pointerCoords.unsafeToPoint / snapGrid.toPoint)

  def withParent(newParent: Parent): UIContext[ReferenceData] =
    this.copy(parent = newParent)

  def withParentBounds(newBounds: Bounds): UIContext[ReferenceData] =
    withParent(parent.withBounds(newBounds))

  def moveParentTo(newPosition: Coords): UIContext[ReferenceData] =
    withParent(parent.moveTo(newPosition))
  def moveParentTo(x: Int, y: Int): UIContext[ReferenceData] =
    withParent(parent.moveTo(x, y))
  def moveParentBy(offset: Coords): UIContext[ReferenceData] =
    withParent(parent.moveBy(offset))
  def moveParentBy(x: Int, y: Int): UIContext[ReferenceData] =
    withParent(parent.moveBy(x, y))

  def resizeParentTo(newDimensions: Dimensions): UIContext[ReferenceData] =
    withParent(parent.resize(newDimensions))
  def resizeParentTo(width: Int, height: Int): UIContext[ReferenceData] =
    withParent(parent.resize(width, height))
  def resizeParentBy(amount: Dimensions): UIContext[ReferenceData] =
    withParent(parent.resizeBy(amount))
  def resizeParentBy(width: Int, height: Int): UIContext[ReferenceData] =
    withParent(parent.resizeBy(width, height))

  def withSnapGrid(newSnapGrid: Size): UIContext[ReferenceData] =
    this.copy(snapGrid = newSnapGrid)
  def clearSnapGrid: UIContext[ReferenceData] =
    this.copy(snapGrid = Size(1))

  def withPointerCoords(coords: Coords): UIContext[ReferenceData] =
    this.copy(_pointerCoords = coords)

  def withState(newState: UIState): UIContext[ReferenceData] =
    this.copy(state = newState)
  def makeActive: UIContext[ReferenceData] =
    withState(UIState.Active)
  def makeInActive: UIContext[ReferenceData] =
    withState(UIState.InActive)

  def withMagnification(newMagnification: Int): UIContext[ReferenceData] =
    this.copy(magnification = newMagnification)

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
    UIContext(
      Parent.default,
      snapGrid,
      Coords(subSystemContext.frame.input.pointers.position),
      UIState.Active,
      magnification,
      subSystemContext.reference,
      subSystemContext.frame,
      subSystemContext.services
    )

  def apply(ctx: Context[?], magnification: Int): UIContext[Unit] =
    fromContext(ctx, (), magnification)

  def apply(ctx: SceneContext[?], magnification: Int): UIContext[Unit] =
    fromSceneContext(ctx, (), magnification)

  def fromContext[ReferenceData](
      ctx: Context[?],
      reference: ReferenceData,
      magnification: Int
  ): UIContext[ReferenceData] =
    UIContext(
      Parent.default,
      Size.one,
      Coords(ctx.frame.input.pointers.position / Point.one),
      UIState.Active,
      magnification,
      reference,
      ctx.frame,
      ctx.services
    )

  def fromSceneContext[ReferenceData](
      ctx: SceneContext[?],
      reference: ReferenceData,
      magnification: Int
  ): UIContext[ReferenceData] =
    fromContext(ctx.toContext, reference, magnification)

  def fromSubSystemContext[ReferenceData](
      ctx: SubSystemContext[?],
      reference: ReferenceData,
      magnification: Int
  ): UIContext[ReferenceData] =
    fromContext(ctx.toContext, reference, magnification)

enum UIState derives CanEqual:
  case Active, InActive

  def isActive: Boolean =
    this match
      case UIState.Active   => true
      case UIState.InActive => false

  def isInActive: Boolean =
    !isActive
