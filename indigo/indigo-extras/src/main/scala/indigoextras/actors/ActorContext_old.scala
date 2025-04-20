// package indigoextras.actors

// import indigo.*
// import indigo.scenes.SceneContext

// final case class ActorContext[ReferenceData](
//     reference: ReferenceData,
//     frame: Context.Frame,
//     services: Context.Services
// ):

//   def toContext: Context[Unit] =
//     new Context[Unit]((), frame, services)

//   def withReference[A](newReference: A): ActorContext[A] =
//     new ActorContext(
//       newReference,
//       frame,
//       services
//     )

//   def unit: ActorContext[Unit] =
//     new ActorContext(
//       (),
//       frame,
//       services
//     )

// object ActorContext:

//   def apply(ctx: Context[?]): ActorContext[Unit] =
//     ActorContext((), ctx.frame, ctx.services)

//   def apply(ctx: SceneContext[?]): ActorContext[Unit] =
//     ActorContext((), ctx.frame, ctx.services)

//   def apply(ctx: SubSystemContext[?]): ActorContext[Unit] =
//     ActorContext((), ctx.frame, ctx.services)
