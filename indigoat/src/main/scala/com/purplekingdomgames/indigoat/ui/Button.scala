package com.purplekingdomgames.indigoat.ui

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph.Graphic
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle

object Button {

  def apply[ViewEventType](state: ButtonState, assets: ButtonAssets[ViewEventType]): Button[ViewEventType] =
    Button(state, assets, ButtonActions(None, None, None, None))

  object Model {

    //TODO: needs to receive some sort of button event to change the state against.
    def update[ViewEventType](button: Button[ViewEventType]): Button[ViewEventType] =
      button

  }

  object View {

    def applyEvents[ViewEventType](bounds: Rectangle, gameTime: GameTime, button: Button[ViewEventType]): FrameInputEvents => Button[ViewEventType] = frameEvents => {
      frameEvents.events.foldLeft(button) { (btn, e) =>
        e match {
          case MouseUp(x, y) if bounds.isPointWithin(x, y) =>
            btn.toHoverState.actions.onHoverOver.map(_(gameTime, btn)).getOrElse(btn.toHoverState)

          case MouseUp(_, _) =>
            btn.toUpState.actions.onHoverOut.map(_(gameTime, btn)).getOrElse(btn.toUpState)

          case MouseDown(x, y) if bounds.isPointWithin(x, y) =>
            btn.toDownState.actions.onDown.map(_(gameTime, btn)).getOrElse(btn.toDownState)

          case MousePosition(x, y) if bounds.isPointWithin(x, y) && btn.state.isDown =>
            btn.actions.onHoverOver.map(_(gameTime, btn)).getOrElse(btn)

          case MousePosition(x, y) if bounds.isPointWithin(x, y) =>
            btn.toHoverState.actions.onHoverOver.map(_(gameTime, btn)).getOrElse(btn.toHoverState)

          case MousePosition(_, _) if btn.state.isDown =>
            btn.actions.onHoverOut.map(_(gameTime, btn)).getOrElse(btn)

          case MousePosition(_, _) =>
            btn.toUpState.actions.onHoverOut.map(_(gameTime, btn)).getOrElse(btn.toUpState)

          case _ =>
            btn
        }
      }
    }

    def renderButton[ViewEventType](bounds: Rectangle)(button: Button[ViewEventType]): Graphic[ViewEventType] =
      button.state match {
        case ButtonState.Up =>
          button.assets.up.moveTo(bounds.position)

        case ButtonState.Over =>
          button.assets.over.moveTo(bounds.position)

        case ButtonState.Down =>
          button.assets.down.moveTo(bounds.position)
      }

    def update[ViewEventType](bounds: Rectangle, gameTime: GameTime, button: Button[ViewEventType], frameEvents: FrameInputEvents): Graphic[ViewEventType] =
      (applyEvents(bounds, gameTime, button) andThen renderButton(bounds))(frameEvents)

  }

}

case class Button[ViewEventType](state: ButtonState, assets: ButtonAssets[ViewEventType], actions: ButtonActions[ViewEventType]) {

  def draw(bounds: Rectangle, gameTime: GameTime, frameEvents: FrameInputEvents): Graphic[ViewEventType] =
    Button.View.update(bounds, gameTime, this, frameEvents)

  def withUpAction(action: (GameTime, Button[ViewEventType]) => Button[ViewEventType]): Button[ViewEventType] =
    this.copy(actions = actions.copy(onUp = Option(action)))

  def withDownAction(action: (GameTime, Button[ViewEventType]) => Button[ViewEventType]): Button[ViewEventType] =
    this.copy(actions = actions.copy(onDown = Option(action)))

  def withHoverOverAction(action: (GameTime, Button[ViewEventType])  => Button[ViewEventType]): Button[ViewEventType] =
    this.copy(actions = actions.copy(onHoverOver = Option(action)))

  def withHoverOutAction(action: (GameTime, Button[ViewEventType])  => Button[ViewEventType]): Button[ViewEventType] =
    this.copy(actions = actions.copy(onHoverOut = Option(action)))

  def toUpState: Button[ViewEventType] =
    this.copy(state = ButtonState.Up)

  def toHoverState: Button[ViewEventType] =
    this.copy(state = ButtonState.Over)

  def toDownState: Button[ViewEventType] =
    this.copy(state = ButtonState.Down)
}

case class ButtonActions[ViewEventType](onUp: Option[(GameTime, Button[ViewEventType]) => Button[ViewEventType]],
                                        onDown: Option[(GameTime, Button[ViewEventType]) => Button[ViewEventType]],
                                        onHoverOver: Option[(GameTime, Button[ViewEventType]) => Button[ViewEventType]],
                                        onHoverOut: Option[(GameTime, Button[ViewEventType]) => Button[ViewEventType]])

sealed trait ButtonState {
  def isDown: Boolean
}
object ButtonState {

  case object Up extends ButtonState {
    def isDown: Boolean = false
  }
  case object Over extends ButtonState {
    def isDown: Boolean = false
  }
  case object Down extends ButtonState {
    def isDown: Boolean = true
  }

}

case class ButtonAssets[ViewEventType](up: Graphic[ViewEventType], over: Graphic[ViewEventType], down: Graphic[ViewEventType])