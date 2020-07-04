package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.FrameContext

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class FrameContextDelegate[StartupData](context: FrameContext[StartupData]) {

  @JSExport
  val gameTime: GameTimeDelegate =
    new GameTimeDelegate(context.gameTime)

  @JSExport
  val dice: DiceDelegate =
    new DiceDelegate(context.dice)

  @JSExport
  val inputState: InputStateDelegate =
    new InputStateDelegate(context.inputState)

  @JSExport
  val boundaryLocator: BoundaryLocatorDelegate =
    new BoundaryLocatorDelegate(context.boundaryLocator)

  def toInternal: FrameContext[StartupData] =
    context
}
