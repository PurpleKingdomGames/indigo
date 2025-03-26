package indigoextras.actors

import indigo.*
import indigo.scenes.SceneContext

opaque type ActorContext = Context[Unit]
object ActorContext:
  def apply(ctx: Context[?]): ActorContext =
    Context(ctx.frame, ctx.services)

  def apply(ctx: SceneContext[?]): ActorContext =
    Context(ctx.frame, ctx.services)

  def apply(ctx: SubSystemContext[?]): ActorContext =
    Context(ctx.frame, ctx.services)

  extension (ac: ActorContext)
    def frame: Context.Frame       = ac.frame
    def services: Context.Services = ac.services
