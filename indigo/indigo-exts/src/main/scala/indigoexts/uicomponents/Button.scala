package indigoexts.uicomponents

import indigo.shared.scenegraph.{Graphic, SceneUpdateFragment}
import indigo.shared.datatypes.{BindingKey, Depth, Rectangle}
import indigo.shared.events.GlobalEvent
import indigo.shared.events.MouseState
import indigo.shared.Outcome

final case class Button(
    buttonAssets: ButtonAssets,
    bounds: Rectangle,
    depth: Depth,
    state: ButtonState,
    bindingKey: BindingKey,
    onUp: () => List[GlobalEvent],
    onDown: () => List[GlobalEvent],
    onHoverOver: () => List[GlobalEvent],
    onHoverOut: () => List[GlobalEvent]
) {

  def update(mouse: MouseState): Outcome[Button] = {
    val mouseInBounds = bounds.isPointWithin(mouse.position)

    state match {
      case ButtonState.Up if mouseInBounds =>
        Outcome(toOverState)

      case ButtonState.Over if !mouseInBounds =>
        Outcome(toUpState)

      case ButtonState.Over if mouseInBounds && mouse.mousePressed =>
        Outcome(toDownState)

      case ButtonState.Down if mouseInBounds && mouse.mouseReleased =>
        Outcome(toOverState)

      case _ =>
        Outcome(this)
    }
  }

  def draw /*(inputState: InputState)*/: SceneUpdateFragment =
    SceneUpdateFragment(
      state match {
        case ButtonState.Up =>
          buttonAssets.up.moveTo(bounds.position).withDepth(depth)

        case ButtonState.Over =>
          buttonAssets.over.moveTo(bounds.position).withDepth(depth)

        case ButtonState.Down =>
          buttonAssets.down.moveTo(bounds.position).withDepth(depth)
      }
    ) //.addGlobalEvents(Button.mouseInteractions(this, inputState.mouse))

  def withUpAction(action: () => List[GlobalEvent]): Button =
    this.copy(onUp = action)

  def withDownAction(action: () => List[GlobalEvent]): Button =
    this.copy(onDown = action)

  def withHoverOverAction(action: () => List[GlobalEvent]): Button =
    this.copy(onHoverOver = action)

  def withHoverOutAction(action: () => List[GlobalEvent]): Button =
    this.copy(onHoverOut = action)

  def toUpState: Button =
    this.copy(state = ButtonState.Up)

  def toOverState: Button =
    this.copy(state = ButtonState.Over)

  def toDownState: Button =
    this.copy(state = ButtonState.Down)
}

object Button {

  def apply(buttonAssets: ButtonAssets, bounds: Rectangle, depth: Depth): Button =
    Button(
      buttonAssets,
      bounds,
      depth,
      ButtonState.Up,
      BindingKey.generate,
      onUp = () => Nil,
      onDown = () => Nil,
      onHoverOver = () => Nil,
      onHoverOut = () => Nil
    )

  // WAIT!
  // This is what is going on based on the input state.
  // Worth considering moving some of it into the update logic? Why
  // does a mouse position change trigger a state change event and
  // not just update the state of the button directly?
  // def mouseInteractions(button: Button, mouse: MouseState): List[GlobalEvent] = {
  //   val mouseInBounds = button.bounds.isPointWithin(mouse.position)

  //   button.state match {
  //     case ButtonState.Up if mouseInBounds =>
  //       ButtonEvent(button.bindingKey, ButtonState.Over) :: button.onHoverOver()

  //     case ButtonState.Over if mouseInBounds && mouse.mousePressed =>
  //       ButtonEvent(button.bindingKey, ButtonState.Down) :: button.onDown()

  //     case ButtonState.Down if mouseInBounds && mouse.mouseReleased =>
  //       ButtonEvent(button.bindingKey, ButtonState.Over) :: button.onUp()

  //     case ButtonState.Down if !mouseInBounds && !mouse.mousePressed =>
  //       ButtonEvent(button.bindingKey, ButtonState.Up) :: button.onUp() ++ button.onHoverOut()

  //     case ButtonState.Over if !mouseInBounds =>
  //       ButtonEvent(button.bindingKey, ButtonState.Up) :: button.onHoverOut()

  //     case _ =>
  //       Nil
  //   }
  // }

}

sealed trait ButtonState {
  def isUp: Boolean
  def isDown: Boolean
  def isOver: Boolean
}
object ButtonState {

  case object Up extends ButtonState {
    def isUp: Boolean   = true
    def isDown: Boolean = false
    def isOver: Boolean = false
  }
  case object Over extends ButtonState {
    def isUp: Boolean   = false
    def isDown: Boolean = false
    def isOver: Boolean = true
  }
  case object Down extends ButtonState {
    def isUp: Boolean   = false
    def isDown: Boolean = true
    def isOver: Boolean = false
  }

}

final case class ButtonAssets(up: Graphic, over: Graphic, down: Graphic)

final case class ButtonEvent(bindingKey: BindingKey, newState: ButtonState) extends GlobalEvent
