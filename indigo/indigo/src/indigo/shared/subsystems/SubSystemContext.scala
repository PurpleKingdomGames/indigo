package indigo.shared.subsystems

import indigo.shared.Context

/** Similar to a regular `Context` but without access to start up data. The `SubSystemContext`` is the context in which
  * the current frame will be processed by a sub-system. It includes values that are unique to this frame, and also
  * globally available services.
  */
final case class SubSystemContext[ReferenceData](
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):

  def toContext: Context[Unit] =
    new Context[Unit]((), frame, services)

  def withReference[A](newReference: A): SubSystemContext[A] =
    new SubSystemContext(
      newReference,
      frame,
      services
    )

  def unit: SubSystemContext[Unit] =
    new SubSystemContext(
      (),
      frame,
      services
    )

object SubSystemContext:

  def fromContext[A](ctx: Context[A]): SubSystemContext[A] =
    new SubSystemContext(
      ctx.startUpData,
      ctx.frame,
      ctx.services
    )

  extension (ctx: Context[?])
    def forSubSystems: SubSystemContext[Unit] =
      SubSystemContext.fromContext(ctx).unit
