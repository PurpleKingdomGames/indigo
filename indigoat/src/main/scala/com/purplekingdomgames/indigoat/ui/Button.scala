package com.purplekingdomgames.indigoat.ui

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph.Graphic
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle

object Button {

  def apply(state: ButtonState, assets: ButtonAssets): Button =
    Button(state, assets, ButtonActions(None, None, None, None))

  object Model {

    def update(button: Button, viewEvent: ViewEvent): Button = {
      viewEvent match {
        case ButtonEvent(ButtonState.Up) =>
          button.toUpState

        case ButtonEvent(ButtonState.Over) =>
          button.toHoverState

        case ButtonEvent(ButtonState.Down) =>
          button.toDownState
      }
    }

  }

  object View {

    def applyEvents(bounds: Rectangle, gameTime: GameTime, button: Button): FrameInputEvents => Button = frameEvents => {
      frameEvents.events.foldLeft(button) { (btn, e) =>
        e match {
          case MouseUp(x, y) if bounds.isPointWithin(x, y) =>
            btn.actions.onHoverOver.map(_(gameTime, btn)).getOrElse(btn)

          case MouseUp(_, _) =>
            btn.actions.onHoverOut.map(_(gameTime, btn)).getOrElse(btn)

          case MouseDown(x, y) if bounds.isPointWithin(x, y) =>
            btn.actions.onDown.map(_(gameTime, btn)).getOrElse(btn)

          case MousePosition(x, y) if bounds.isPointWithin(x, y) && btn.state.isDown =>
            btn.actions.onHoverOver.map(_(gameTime, btn)).getOrElse(btn)

          case MousePosition(x, y) if bounds.isPointWithin(x, y) =>
            btn.actions.onHoverOver.map(_(gameTime, btn)).getOrElse(btn)

          case MousePosition(_, _) if btn.state.isDown =>
            btn.actions.onHoverOut.map(_(gameTime, btn)).getOrElse(btn)

          case MousePosition(_, _) =>
            btn.actions.onHoverOut.map(_(gameTime, btn)).getOrElse(btn)

          case _ =>
            btn
        }
      }
    }

    def renderButton(bounds: Rectangle)(button: Button): Graphic =
      button.state match {
        case ButtonState.Up =>
          button.assets.up.moveTo(bounds.position)

        case ButtonState.Over =>
          button.assets.over.moveTo(bounds.position)

        case ButtonState.Down =>
          button.assets.down.moveTo(bounds.position)
      }

    def update(bounds: Rectangle, gameTime: GameTime, button: Button, frameEvents: FrameInputEvents): Graphic =
      (applyEvents(bounds, gameTime, button) andThen renderButton(bounds))(frameEvents)

  }

}

case class Button(state: ButtonState, assets: ButtonAssets, actions: ButtonActions) {

  def draw(bounds: Rectangle, gameTime: GameTime, frameEvents: FrameInputEvents): Graphic =
    Button.View.update(bounds, gameTime, this, frameEvents)

  def withUpAction(action: (GameTime, Button) => Button): Button =
    this.copy(actions = actions.copy(onUp = Option(action)))

  def withDownAction(action: (GameTime, Button) => Button): Button =
    this.copy(actions = actions.copy(onDown = Option(action)))

  def withHoverOverAction(action: (GameTime, Button)  => Button): Button =
    this.copy(actions = actions.copy(onHoverOver = Option(action)))

  def withHoverOutAction(action: (GameTime, Button)  => Button): Button =
    this.copy(actions = actions.copy(onHoverOut = Option(action)))

  def toUpState: Button =
    this.copy(state = ButtonState.Up)

  def toHoverState: Button =
    this.copy(state = ButtonState.Over)

  def toDownState: Button =
    this.copy(state = ButtonState.Down)
}

case class ButtonActions(onUp: Option[(GameTime, Button) => Button],
                                        onDown: Option[(GameTime, Button) => Button],
                                        onHoverOver: Option[(GameTime, Button) => Button],
                                        onHoverOut: Option[(GameTime, Button) => Button])

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

case class ButtonAssets(up: Graphic, over: Graphic, down: Graphic)

case class ButtonEvent(newState: ButtonState) extends ViewEvent
