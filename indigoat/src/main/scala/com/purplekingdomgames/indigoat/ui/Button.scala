package com.purplekingdomgames.indigoat.ui

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph.Graphic
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Rectangle}

object Button {

  def apply(state: ButtonState, assets: ButtonAssets): Button =
    Button(state, assets, ButtonActions(() => None, () => None, () => None, () => None))

  object Model {

    def update(button: Button, viewEvent: ViewEvent): Button = {
      viewEvent match {
        case ButtonEvent(bindingKey, ButtonState.Up) if button.bindingKey === bindingKey =>
          button.toUpState

        case ButtonEvent(bindingKey, ButtonState.Over) if button.bindingKey === bindingKey =>
          button.toHoverState

        case ButtonEvent(bindingKey, ButtonState.Down) if button.bindingKey === bindingKey =>
          button.toDownState

        case _ =>
          button
      }
    }

  }

  object View {

    def applyEvents(bounds: Rectangle, button: Button, frameInputEvents: FrameInputEvents): List[ViewEvent] =
      frameInputEvents.events.foldLeft[List[ViewEvent]](Nil) { (acc, e) =>
        e match {
          case MouseUp(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onUp().toList :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MouseUp(_, _) =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Up)

          case MouseDown(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onDown().toList :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MousePosition(x, y) if bounds.isPointWithin(x, y) && button.state.isDown =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MousePosition(x, y) if bounds.isPointWithin(x, y) && button.state.isOver =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MousePosition(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onHoverOver().toList :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MousePosition(_, _) if button.state.isDown =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MousePosition(_, _) =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Up)

          case _ =>
            acc
        }
      }

    def renderButton(bounds: Rectangle, button: Button): Graphic =
      button.state match {
        case ButtonState.Up =>
          button.assets.up.moveTo(bounds.position)

        case ButtonState.Over =>
          button.assets.over.moveTo(bounds.position)

        case ButtonState.Down =>
          button.assets.down.moveTo(bounds.position)
      }

    def update(bounds: Rectangle, button: Button, frameEvents: FrameInputEvents): ButtonViewUpdate =
      ButtonViewUpdate(
        renderButton(bounds, button),
        applyEvents(bounds, button, frameEvents)
      )

  }

}

case class Button(state: ButtonState, assets: ButtonAssets, actions: ButtonActions) {

  val bindingKey: BindingKey = BindingKey.generate

  def draw(bounds: Rectangle, frameEvents: FrameInputEvents): ButtonViewUpdate =
    Button.View.update(bounds, this, frameEvents)

  def withUpAction(action: () => Option[ViewEvent]): Button =
    this.copy(actions = actions.copy(onUp = action))

  def withDownAction(action: () => Option[ViewEvent]): Button =
    this.copy(actions = actions.copy(onDown = action))

  def withHoverOverAction(action: () => Option[ViewEvent]): Button =
    this.copy(actions = actions.copy(onHoverOver = action))

  def withHoverOutAction(action: () => Option[ViewEvent]): Button =
    this.copy(actions = actions.copy(onHoverOut = action))

  def toUpState: Button =
    this.copy(state = ButtonState.Up)

  def toHoverState: Button =
    this.copy(state = ButtonState.Over)

  def toDownState: Button =
    this.copy(state = ButtonState.Down)
}

case class ButtonActions(onUp: () => Option[ViewEvent],
                                        onDown: () => Option[ViewEvent],
                                        onHoverOver: () => Option[ViewEvent],
                                        onHoverOut: () => Option[ViewEvent])

sealed trait ButtonState {
  def isDown: Boolean
  def isOver: Boolean
}
object ButtonState {

  case object Up extends ButtonState {
    def isDown: Boolean = false
    def isOver: Boolean = false
  }
  case object Over extends ButtonState {
    def isDown: Boolean = false
    def isOver: Boolean = true
  }
  case object Down extends ButtonState {
    def isDown: Boolean = true
    def isOver: Boolean = false
  }

}

case class ButtonAssets(up: Graphic, over: Graphic, down: Graphic)

case class ButtonEvent(bindingKey: BindingKey, newState: ButtonState) extends ViewEvent

case class ButtonViewUpdate(buttonGraphic: Graphic, buttonEvents: List[ViewEvent]) {

  def toTuple: (Graphic, List[ViewEvent]) = (buttonGraphic, buttonEvents)

}
