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

  def moveBoundsBy(offset: Coords): UIContext[ReferenceData] =
    this.copy(bounds = bounds.moveBy(offset))

  val isActive: Boolean =
    state == UIState.Active

  def unitReference: UIContext[Unit] =
    this.copy(reference = ())

  def withAdditionalOffset(offset: Coords): UIContext[ReferenceData] =
    this.copy(additionalOffset = offset)

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
    fromContext(ctx.toFrameContext, reference)

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
