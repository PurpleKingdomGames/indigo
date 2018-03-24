package com.purplekingdomgames.indigoexts.ui

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, MouseEvent, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.Graphic
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Depth, Rectangle}

object Button {

  def apply(state: ButtonState): Button =
    Button(state, ButtonActions(() => None, () => None, () => None, () => None))

  object Model {

    def update(button: Button, buttonEvent: ButtonEvent): Button = {
      buttonEvent match {
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

    def applyEvents(bounds: Rectangle, button: Button, frameInputEvents: FrameInputEvents): List[ViewEvent] = {
      frameInputEvents.events.foldLeft[List[ViewEvent]](Nil) { (acc, e) =>
        e match {
          case MouseEvent.MouseUp(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onUp().toList :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MouseEvent.MouseUp(_, _) =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Up)

          case MouseEvent.MouseDown(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onDown().toList :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MouseEvent.Move(x, y) if bounds.isPointWithin(x, y) && button.state.isDown =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MouseEvent.Move(x, y) if bounds.isPointWithin(x, y) && button.state.isOver =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MouseEvent.Move(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onHoverOver().toList :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MouseEvent.Move(_, _) if button.state.isDown =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MouseEvent.Move(_, _) =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Up)

          case _ =>
            acc
        }
      }
    }

    def renderButton(bounds: Rectangle, depth: Depth, button: Button, assets: ButtonAssets): Graphic =
      button.state match {
        case ButtonState.Up =>
          assets.up.moveTo(bounds.position).withDepth(depth.zIndex)

        case ButtonState.Over =>
          assets.over.moveTo(bounds.position).withDepth(depth.zIndex)

        case ButtonState.Down =>
          assets.down.moveTo(bounds.position).withDepth(depth.zIndex)
      }

    def update(bounds: Rectangle, depth: Depth, button: Button, frameEvents: FrameInputEvents, assets: ButtonAssets): ButtonViewUpdate =
      ButtonViewUpdate(
        renderButton(bounds, depth, button, assets),
        applyEvents(bounds, button, frameEvents)
      )

  }

}

case class Button(state: ButtonState, actions: ButtonActions, bindingKey: BindingKey = BindingKey.generate) {

  def update(buttonEvent: ButtonEvent): Button =
    Button.Model.update(this, buttonEvent)

  def draw(bounds: Rectangle, depth: Depth, frameInputEvents: FrameInputEvents, buttonAssets: ButtonAssets): ButtonViewUpdate =
    Button.View.update(bounds, depth, this, frameInputEvents, buttonAssets)

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
