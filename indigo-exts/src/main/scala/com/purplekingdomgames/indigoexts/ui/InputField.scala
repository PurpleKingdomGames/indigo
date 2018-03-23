package com.purplekingdomgames.indigoexts.ui

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Depth, Point}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphNode, Text}

object InputField {

  object Model {

    def update(inputField: InputField, inputFieldEvent: InputFieldEvent): InputField = {
      inputFieldEvent match {
        case InputFieldEvent(bindingKey, InputFieldState.Normal) if inputField.bindingKey == bindingKey =>
          inputField

        case InputFieldEvent(bindingKey, InputFieldState.HasFocus(_)) if inputField.bindingKey == bindingKey =>
          inputField

        case _ =>
          inputField
      }
    }

  }

  object View {

    def applyEvent(frameInputEvents: FrameInputEvents): List[ViewEvent] =
      frameInputEvents.events.foldLeft[List[ViewEvent]](Nil) { (acc, e) =>
        e match {
          case _ =>
            acc
        }
      }

    def render(position: Point, depth: Depth, inputField: InputField, inputFieldAssets: InputFieldAssets): List[SceneGraphNode] = {
      inputField.state match {
        case InputFieldState.Normal =>
          List(inputFieldAssets.text.moveTo(position).withDepth(depth.zIndex))

        case InputFieldState.HasFocus(_) =>
          List(
            inputFieldAssets.text.moveTo(position).withDepth(depth.zIndex),
            inputFieldAssets.cursor.moveTo(position).withDepth(depth.zIndex)
          )
      }
    }

    def update(position: Point, depth: Depth, inputField: InputField, frameEvents: FrameInputEvents, inputFieldAssets: InputFieldAssets): InputFieldViewUpdate = {
      InputFieldViewUpdate(
        render(position, depth, inputField, inputFieldAssets),
        applyEvent(frameEvents)
      )
    }

  }

}

case class InputField(state: InputFieldState) {

  val bindingKey: BindingKey = BindingKey.generate

  def update(inputFieldEvent: InputFieldEvent): InputField =
    InputField.Model.update(this, inputFieldEvent)

  def draw(position: Point, depth: Depth, frameInputEvents: FrameInputEvents, inputFieldAssets: InputFieldAssets): InputFieldViewUpdate =
    InputField.View.update(position, depth, this, frameInputEvents, inputFieldAssets)

}

case class InputFieldAssets(text: Text, cursor: Graphic)

case class InputFieldEvent(bindingKey: BindingKey, states: InputFieldState) extends ViewEvent

case class InputFieldViewUpdate(sceneGraphNodes: List[SceneGraphNode], inputFieldEvents: List[ViewEvent]) {
  def toTuple: (List[SceneGraphNode], List[ViewEvent]) = (sceneGraphNodes, inputFieldEvents)
}

sealed trait InputFieldState {
  val hasFocus: Boolean
}
object InputFieldState {
  case class HasFocus(cursorPosition: Int) extends InputFieldState {
    val hasFocus: Boolean = true
  }
  case object Normal extends InputFieldState {
    val hasFocus: Boolean = false
  }
}