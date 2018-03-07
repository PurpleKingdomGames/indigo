package com.purplekingdomgames.indigoat.ui

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphNode}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle

object Button {

  def apply[ViewEventType](bounds: Rectangle, state: ButtonState, assets: ButtonAssets[ViewEventType]): Button[ViewEventType] =
    Button(bounds, state, assets, ButtonActions(None, None, None, None))

  object Model {

    def update[ViewEventType](gameTime: GameTime, button: Button[ViewEventType]): GameEvent => Button[ViewEventType] = {
      case MouseClick(x, y) if button.bounds.isPointWithin(x, y) =>
        button.toHoverState.actions.onUp.map(_(gameTime, button)).getOrElse(button.toHoverState)

      case MouseUp(x, y) if button.bounds.isPointWithin(x, y) =>
        button.toHoverState.actions.onHoverOver.map(_(gameTime, button)).getOrElse(button.toHoverState)

      case MouseUp(_, _) =>
        button.toUpState.actions.onHoverOut.map(_(gameTime, button)).getOrElse(button.toUpState)

      case MouseDown(x, y) if button.bounds.isPointWithin(x, y) =>
        button.toDownState.actions.onDown.map(_(gameTime, button)).getOrElse(button.toDownState)

      case MousePosition(x, y) if button.bounds.isPointWithin(x, y) && button.state.isDown =>
        button.actions.onHoverOver.map(_(gameTime, button)).getOrElse(button)

      case MousePosition(x, y) if button.bounds.isPointWithin(x, y) =>
        button.toHoverState.actions.onHoverOver.map(_(gameTime, button)).getOrElse(button.toHoverState)

      case MousePosition(_, _) if button.state.isDown =>
        button.actions.onHoverOut.map(_(gameTime, button)).getOrElse(button)

      case MousePosition(_, _) =>
        button.toUpState.actions.onHoverOut.map(_(gameTime, button)).getOrElse(button.toUpState)

      case _ =>
        button
    }

  }

  object View {

    def draw[ViewEventType](button: Button[ViewEventType]): SceneGraphNode[ViewEventType] =
      button.state match {
        case ButtonState.Up =>
          button.assets.up.moveTo(button.bounds.position)

        case ButtonState.Over =>
          button.assets.over.moveTo(button.bounds.position)

        case ButtonState.Down =>
          button.assets.down.moveTo(button.bounds.position)
      }

  }

}

case class Button[ViewEventType](bounds: Rectangle, state: ButtonState, assets: ButtonAssets[ViewEventType], actions: ButtonActions[ViewEventType]) {

  def update(gameTime: GameTime, gameEvent: GameEvent): Button[ViewEventType] =
    Button.Model.update(gameTime, this)(gameEvent)

  def draw: SceneGraphNode[ViewEventType] =
    Button.View.draw(this)

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