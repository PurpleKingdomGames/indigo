package com.purplekingdomgames.indigoat.ui

import com.purplekingdomgames.indigo.gameengine.{GameEvent, GameTime, MouseClick, MousePosition}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphNode}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle

object Button {

  def apply[ViewEventType](bounds: Rectangle, state: ButtonState, assets: ButtonAssets[ViewEventType]): Button[ViewEventType] =
    Button(bounds, state, assets, None, None, None)

  object Model {

    def update[ViewEventType](gameTime: GameTime, button: Button[ViewEventType]): GameEvent => Button[ViewEventType] = {
      case MouseClick(x, y) if button.bounds.isPointWithin(x, y) =>
        button.onClick.map(_(gameTime, button)).getOrElse(button)

      case MousePosition(x, y) if button.bounds.isPointWithin(x, y) =>
        button.onHoverOver.map(_(gameTime, button)).getOrElse(button)

      case MousePosition(_, _) =>
        button.onHoverOut.map(_(gameTime, button)).getOrElse(button)

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

case class Button[ViewEventType](bounds: Rectangle, state: ButtonState, assets: ButtonAssets[ViewEventType], onClick: Option[(GameTime, Button[ViewEventType]) => Button[ViewEventType]], onHoverOver: Option[(GameTime, Button[ViewEventType]) => Button[ViewEventType]], onHoverOut: Option[(GameTime, Button[ViewEventType]) => Button[ViewEventType]]) {

  def update(gameTime: GameTime, gameEvent: GameEvent): Button[ViewEventType] =
    Button.Model.update(gameTime, this)(gameEvent)

  def draw: SceneGraphNode[ViewEventType] =
    Button.View.draw(this)

  def withClickAction(action: (GameTime, Button[ViewEventType]) => Button[ViewEventType]): Button[ViewEventType] =
    this.copy(onClick = Option(action))

  def withHoverOverAction(action: (GameTime, Button[ViewEventType])  => Button[ViewEventType]): Button[ViewEventType] =
    this.copy(onHoverOver = Option(action))

  def withHoverOutAction(action: (GameTime, Button[ViewEventType])  => Button[ViewEventType]): Button[ViewEventType] =
    this.copy(onHoverOut = Option(action))

  def toUpState: Button[ViewEventType] =
    this.copy(state = ButtonState.Up)

  def toHoverState: Button[ViewEventType] =
    this.copy(state = ButtonState.Over)

  def toDownState: Button[ViewEventType] =
    this.copy(state = ButtonState.Down)
}

sealed trait ButtonState
object ButtonState {

  case object Up extends ButtonState
  case object Over extends ButtonState
  case object Down extends ButtonState

}

case class ButtonAssets[ViewEventType](up: Graphic[ViewEventType], over: Graphic[ViewEventType], down: Graphic[ViewEventType])