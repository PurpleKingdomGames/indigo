package indigoexts.uicomponents

import indigo.shared.scenegraph.{Graphic, SceneUpdateFragment}
import indigo.shared.datatypes.{BindingKey, Depth, Rectangle}
import indigo.shared.EqualTo._
import indigo.shared.events.{FrameInputEvents, MouseEvent, GlobalEvent}

object Button {

  def apply(state: ButtonState): Button =
    Button(state, ButtonActions(() => Nil, () => Nil, () => Nil, () => Nil), BindingKey.generate)

  def default: Button =
    apply(ButtonState.Up)

  object Model {

    def update(button: Button, buttonEvent: ButtonEvent): Button =
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

  object View {

    def applyEvents(bounds: Rectangle, button: Button, frameInputEvents: FrameInputEvents): List[GlobalEvent] =
      frameInputEvents.globalEvents.foldLeft[List[GlobalEvent]](Nil) { (acc, e) =>
        e match {
          case MouseEvent.MouseUp(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onUp() :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MouseEvent.MouseUp(_, _) =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Up)

          case MouseEvent.MouseDown(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onDown() :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MouseEvent.Move(x, y) if bounds.isPointWithin(x, y) && button.state.isDown =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MouseEvent.Move(x, y) if bounds.isPointWithin(x, y) && button.state.isOver =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MouseEvent.Move(x, y) if bounds.isPointWithin(x, y) =>
            acc ++ button.actions.onHoverOver() :+ ButtonEvent(button.bindingKey, ButtonState.Over)

          case MouseEvent.Move(_, _) if button.state.isDown =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Down)

          case MouseEvent.Move(_, _) =>
            acc :+ ButtonEvent(button.bindingKey, ButtonState.Up)

          case _ =>
            acc
        }
      }

    def renderButton(bounds: Rectangle, depth: Depth, button: Button, assets: ButtonAssets): Graphic =
      button.state match {
        case ButtonState.Up =>
          assets.up.moveTo(bounds.position).withDepth(depth)

        case ButtonState.Over =>
          assets.over.moveTo(bounds.position).withDepth(depth)

        case ButtonState.Down =>
          assets.down.moveTo(bounds.position).withDepth(depth)
      }

    def update(bounds: Rectangle, depth: Depth, button: Button, frameEvents: FrameInputEvents, assets: ButtonAssets): ButtonViewUpdate =
      ButtonViewUpdate(
        renderButton(bounds, depth, button, assets),
        applyEvents(bounds, button, frameEvents)
      )

  }

}

final case class Button(state: ButtonState, actions: ButtonActions, bindingKey: BindingKey) {

  def update(buttonEvent: ButtonEvent): Button =
    Button.Model.update(this, buttonEvent)

  def draw(bounds: Rectangle, depth: Depth, frameInputEvents: FrameInputEvents, buttonAssets: ButtonAssets): ButtonViewUpdate =
    Button.View.update(bounds, depth, this, frameInputEvents, buttonAssets)

  def withUpAction(action: () => List[GlobalEvent]): Button =
    this.copy(actions = actions.copy(onUp = action))

  def withDownAction(action: () => List[GlobalEvent]): Button =
    this.copy(actions = actions.copy(onDown = action))

  def withHoverOverAction(action: () => List[GlobalEvent]): Button =
    this.copy(actions = actions.copy(onHoverOver = action))

  def withHoverOutAction(action: () => List[GlobalEvent]): Button =
    this.copy(actions = actions.copy(onHoverOut = action))

  def toUpState: Button =
    this.copy(state = ButtonState.Up)

  def toHoverState: Button =
    this.copy(state = ButtonState.Over)

  def toDownState: Button =
    this.copy(state = ButtonState.Down)
}

final case class ButtonActions(onUp: () => List[GlobalEvent], onDown: () => List[GlobalEvent], onHoverOver: () => List[GlobalEvent], onHoverOut: () => List[GlobalEvent])

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

final case class ButtonAssets(up: Graphic, over: Graphic, down: Graphic)

final case class ButtonEvent(bindingKey: BindingKey, newState: ButtonState) extends GlobalEvent

final case class ButtonViewUpdate(buttonGraphic: Graphic, buttonEvents: List[GlobalEvent]) {

  def toSceneUpdateFragment: SceneUpdateFragment =
    SceneUpdateFragment()
      .addGameLayerNodes(buttonGraphic)
      .addGlobalEvents(buttonEvents)

  def toTuple: (Graphic, List[GlobalEvent]) =
    (buttonGraphic, buttonEvents)

}
