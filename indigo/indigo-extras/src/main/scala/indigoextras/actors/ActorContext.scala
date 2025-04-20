package indigoextras.actors

import indigo.*
import indigo.scenes.SceneContext

final case class ActorContext[ReferenceData, A](
    find: (A => Boolean) => Option[A],
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):
  def toContext: Context[Unit] =
    new Context[Unit]((), frame, services)

object ActorContext:

  def apply[ReferenceData, A](
      find: (A => Boolean) => Option[A],
      reference: ReferenceData,
      ctx: Context[?]
  ): ActorContext[ReferenceData, A] =
    ActorContext(find, reference, ctx.frame, ctx.services)

  def apply[ReferenceData, A](
      find: (A => Boolean) => Option[A],
      reference: ReferenceData,
      ctx: SceneContext[?]
  ): ActorContext[ReferenceData, A] =
    ActorContext(find, reference, ctx.frame, ctx.services)

  def apply[ReferenceData, A](
      find: (A => Boolean) => Option[A],
      reference: ReferenceData,
      ctx: SubSystemContext[?]
  ): ActorContext[ReferenceData, A] =
    ActorContext(find, reference, ctx.frame, ctx.services)
