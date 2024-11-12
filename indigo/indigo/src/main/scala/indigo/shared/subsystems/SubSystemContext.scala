package indigo.shared.subsystems

import indigo.platform.renderer.Renderer
import indigo.shared.BoundaryLocator
import indigo.shared.Context
import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.input.Gamepad
import indigo.shared.input.Keyboard
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.SceneNode
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

/** Similar to `Context` but without access to start up data. The SubSystemContext is the context in which the current
  * frame will be processed. In includes values that are unique to this frame, and also globally available services.
  *
  * @param gameTime
  *   A sampled instance of time that you should use everywhere that you need a time value.
  * @param dice
  *   A psuedorandom number generator, made predicatable/reproducable by being seeded on the current running time.
  * @param inputState
  *   A snapshot of the state of the various input methods, also allows input mapping of combinations of inputs.
  * @param boundaryLocator
  *   A service that can be interogated for the calculated dimensions of screen elements.
  */
final case class SubSystemContext[ReferenceData](
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):
  export frame.*
  export services.*

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
