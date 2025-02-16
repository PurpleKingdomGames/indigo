package indigoextras.ui.datatypes

import indigo.*
import indigo.scenes.SceneContext

final case class UIContext[ReferenceData](
    // Specific to UIContext
    parent: Parent,
    snapGrid: Size,
    pointerCoords: Coords,
    state: UIState,
    magnification: Int,
    // The following are all the same as in SubSystemContext
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):

  lazy val isActive: Boolean =
    state == UIState.Active

  def withParent(newParent: Parent): UIContext[ReferenceData] =
    this.copy(parent = newParent)

  def withParentBounds(newBounds: Bounds): UIContext[ReferenceData] =
    withParent(parent.withBounds(newBounds))

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
      Parent.default,
      snapGrid,
      pointerCoords,
      UIState.Active,
      magnification,
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
      Parent.default,
      Size(1),
      Coords.zero,
      UIState.Active,
      1,
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
