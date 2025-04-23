package indigoextras.performers

import indigo.*
import indigo.scenes.SceneContext
import indigoextras.actors.ActorContext

final case class PerformerContext[ReferenceData](
    findById: PerformerId => Option[Performer[ReferenceData]],
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):
  def toContext: Context[Unit] =
    new Context[Unit]((), frame, services)

object PerformerContext:

  def apply[ReferenceData](
      findById: PerformerId => Option[Performer[ReferenceData]],
      reference: ReferenceData,
      ctx: Context[?]
  ): PerformerContext[ReferenceData] =
    PerformerContext(findById, reference, ctx.frame, ctx.services)

  def apply[ReferenceData](
      findById: PerformerId => Option[Performer[ReferenceData]],
      reference: ReferenceData,
      ctx: SceneContext[?]
  ): PerformerContext[ReferenceData] =
    PerformerContext(findById, reference, ctx.frame, ctx.services)

  def apply[ReferenceData](
      findById: PerformerId => Option[Performer[ReferenceData]],
      reference: ReferenceData,
      ctx: SubSystemContext[?]
  ): PerformerContext[ReferenceData] =
    PerformerContext(findById, reference, ctx.frame, ctx.services)

  def fromActorContext[ReferenceData](
      actorContext: ActorContext[ReferenceData, Performer[ReferenceData]]
  ): PerformerContext[ReferenceData] =
    def findById: PerformerId => Option[Performer[ReferenceData]] =
      id => actorContext.find(_.id == id)

    PerformerContext(
      findById,
      actorContext.reference,
      actorContext.frame,
      actorContext.services
    )
