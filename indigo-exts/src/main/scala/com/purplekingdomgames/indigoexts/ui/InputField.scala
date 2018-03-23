package com.purplekingdomgames.indigoexts.ui

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Depth, Point}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphNode, Text}

object InputField {

  def apply(text: String): InputField =
    InputField(InputFieldState.Normal, text, 0)

  object Model {

    def update(inputField: InputField, inputFieldEvent: InputFieldEvent): InputField = {
      inputFieldEvent match {
        case InputFieldEvent.Delete(bindingKey) if inputField.bindingKey == bindingKey =>
          inputField.delete

        case InputFieldEvent.Backspace(bindingKey) if inputField.bindingKey == bindingKey =>
          inputField.backspace

        case InputFieldEvent.CursorLeft(bindingKey) if inputField.bindingKey == bindingKey =>
          inputField.cursorLeft

        case InputFieldEvent.CursorRight(bindingKey) if inputField.bindingKey == bindingKey =>
          inputField.cursorRight

        case InputFieldEvent.GiveFocus(bindingKey) if inputField.bindingKey == bindingKey =>
          inputField.giveFocus

        case InputFieldEvent.LoseFocus(bindingKey) if inputField.bindingKey == bindingKey =>
          inputField.loseFocus

        case InputFieldEvent.AddCharacter(bindingKey, char) if inputField.bindingKey == bindingKey =>
          inputField.addCharacter(char)
      }
    }

  }

  object View {

    //TODO: Convert frame input events into InputFieldEvents
    def applyEvent(frameInputEvents: FrameInputEvents): List[InputFieldEvent] =
      frameInputEvents.events.foldLeft[List[InputFieldEvent]](Nil) { (acc, e) =>
        e match {
          case _ =>
            acc
        }
      }

    def render(position: Point, depth: Depth, inputField: InputField, inputFieldAssets: InputFieldAssets): List[SceneGraphNode] = {
      inputField.state match {
        case InputFieldState.Normal =>
          List(inputFieldAssets.text.withText(inputField.text).moveTo(position).withDepth(depth.zIndex))

        case InputFieldState.HasFocus =>
          List(
            inputFieldAssets.text.withText(inputField.text).moveTo(position).withDepth(depth.zIndex),
            inputFieldAssets.cursor.moveTo(position).withDepth(depth.zIndex)
          )
      }
    }

    //TODO: Cursor position and blink...
    def update(position: Point, depth: Depth, inputField: InputField, frameEvents: FrameInputEvents, inputFieldAssets: InputFieldAssets): InputFieldViewUpdate = {
      InputFieldViewUpdate(
        render(position, depth, inputField, inputFieldAssets),
        applyEvent(frameEvents)
      )
    }

  }

  def deleteCharacter(inputField: InputField): InputField = {
    val splitString = inputField.text.splitAt(inputField.cursorPosition)

    if(splitString._2.isEmpty) inputField
    else {
      inputField.copy(
        text = splitString._1 + splitString._2.substring(1)
      )
    }
  }

  def backspace(inputField: InputField): InputField = {
    val splitString = inputField.text.splitAt(inputField.cursorPosition)

    if(splitString._1.isEmpty) inputField
    else {
      inputField.copy(
        text = splitString._1.take(splitString._1.length - 1) + splitString._2
      )
    }
  }

  def addCharacter(inputField: InputField, char: Char): InputField = {
    val splitString = inputField.text.splitAt(inputField.cursorPosition)

    inputField.copy(
      text = splitString._1 + char + splitString._2
    )
  }

}

case class InputField(state: InputFieldState, text: String, cursorPosition: Int) {

  val bindingKey: BindingKey = BindingKey.generate

  def update(inputFieldEvent: InputFieldEvent): InputField =
    InputField.Model.update(this, inputFieldEvent)

  def draw(position: Point, depth: Depth, frameInputEvents: FrameInputEvents, inputFieldAssets: InputFieldAssets): InputFieldViewUpdate =
    InputField.View.update(position, depth, this, frameInputEvents, inputFieldAssets)

  def giveFocus: InputField =
    this.copy(
      state = InputFieldState.HasFocus,
      cursorPosition = this.text.length
    )

  def loseFocus: InputField =
    this.copy(
      state = InputFieldState.Normal,
      cursorPosition = 0
    )

  def cursorLeft: InputField =
    this.copy(cursorPosition = if(cursorPosition - 1 >= 0) cursorPosition - 1 else cursorPosition)

  def cursorRight: InputField =
    this.copy(cursorPosition = if(cursorPosition + 1 <= text.length) cursorPosition + 1 else text.length)

  def delete: InputField =
    InputField.deleteCharacter(this)

  def backspace: InputField =
    InputField.backspace(this)

  def addCharacter(char: Char): InputField =
    InputField.addCharacter(this, char)

}

case class InputFieldAssets(text: Text, cursor: Graphic)

case class InputFieldViewUpdate(sceneGraphNodes: List[SceneGraphNode], inputFieldEvents: List[InputFieldEvent]) {
  def toTuple: (List[SceneGraphNode], List[InputFieldEvent]) = (sceneGraphNodes, inputFieldEvents)
}

sealed trait InputFieldEvent extends ViewEvent {
  val bindingKey: BindingKey
}
object InputFieldEvent {
  case class Delete(bindingKey: BindingKey) extends InputFieldEvent
  case class Backspace(bindingKey: BindingKey) extends InputFieldEvent
  case class CursorLeft(bindingKey: BindingKey) extends InputFieldEvent
  case class CursorRight(bindingKey: BindingKey) extends InputFieldEvent
  case class GiveFocus(bindingKey: BindingKey) extends InputFieldEvent
  case class LoseFocus(bindingKey: BindingKey) extends InputFieldEvent
  case class AddCharacter(bindingKey: BindingKey, char: Char) extends InputFieldEvent
}

sealed trait InputFieldState {
  val hasFocus: Boolean
}
object InputFieldState {
  case object HasFocus extends InputFieldState {
    val hasFocus: Boolean = true
  }
  case object Normal extends InputFieldState {
    val hasFocus: Boolean = false
  }
}