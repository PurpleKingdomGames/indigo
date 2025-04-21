package indigoextras.performers

import indigo.*
import indigo.scenes.SceneContext
import indigoextras.actors.ActorContext

final case class PerformerContext[ReferenceData](
    find: (Performer[ReferenceData] => Boolean) => Option[Performer[ReferenceData]],
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):
  def toContext: Context[Unit] =
    new Context[Unit]((), frame, services)

object PerformerContext:

  def apply[ReferenceData](
      find: (Performer[ReferenceData] => Boolean) => Option[Performer[ReferenceData]],
      reference: ReferenceData,
      ctx: Context[?]
  ): PerformerContext[ReferenceData] =
    PerformerContext(find, reference, ctx.frame, ctx.services)

  def apply[ReferenceData](
      find: (Performer[ReferenceData] => Boolean) => Option[Performer[ReferenceData]],
      reference: ReferenceData,
      ctx: SceneContext[?]
  ): PerformerContext[ReferenceData] =
    PerformerContext(find, reference, ctx.frame, ctx.services)

  def apply[ReferenceData](
      find: (Performer[ReferenceData] => Boolean) => Option[Performer[ReferenceData]],
      reference: ReferenceData,
      ctx: SubSystemContext[?]
  ): PerformerContext[ReferenceData] =
    PerformerContext(find, reference, ctx.frame, ctx.services)

  def fromActorContext[ReferenceData](
      actorContext: ActorContext[ReferenceData, Performer[ReferenceData]],
      find: (Performer[ReferenceData] => Boolean) => Option[Performer[ReferenceData]]
  ): PerformerContext[ReferenceData] =
    PerformerContext(
      find,
      actorContext.reference,
      actorContext.frame,
      actorContext.services
    )
