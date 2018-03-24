package com.purplekingdomgames.indigoexts.ui

import com.purplekingdomgames.indigo.gameengine.constants.Keys
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, KeyboardEvent, MouseEvent, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Depth, Point, Rectangle}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphNode, Text}

object InputField {

  def apply(text: String): InputField =
    InputField(InputFieldState.Normal, text, 0)

  object Model {

    def update(inputField: InputField, inputFieldEvent: InputFieldEvent): InputField =
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

        case _ =>
          inputField
      }

  }

  object View {

    def applyEvent(bounds: Rectangle, inputField: InputField, frameInputEvents: FrameInputEvents): List[InputFieldEvent] = {
      frameInputEvents.events.foldLeft[List[InputFieldEvent]](Nil) { (acc, e) =>
        e match {
          case MouseEvent.MouseUp(x, y) if bounds.isPointWithin(x, y) =>
            acc :+ InputFieldEvent.GiveFocus(inputField.bindingKey)

          case MouseEvent.MouseUp(_, _) =>
            acc :+ InputFieldEvent.LoseFocus(inputField.bindingKey)

          case KeyboardEvent.KeyDown(Keys.LEFT_ARROW) if inputField.state.hasFocus =>
            acc :+ InputFieldEvent.CursorLeft(inputField.bindingKey)

          case KeyboardEvent.KeyDown(Keys.RIGHT_ARROW) if inputField.state.hasFocus =>
            acc :+ InputFieldEvent.CursorRight(inputField.bindingKey)

          case KeyboardEvent.KeyPress(Keys.BACKSPACE) if inputField.state.hasFocus =>
            acc :+ InputFieldEvent.Backspace(inputField.bindingKey)

          case KeyboardEvent.KeyPress(Keys.DELETE) if inputField.state.hasFocus =>
            acc :+ InputFieldEvent.Delete(inputField.bindingKey)

          case KeyboardEvent.KeyUp(keyCode) if inputField.state.hasFocus && keyCode.isPrintable =>
            acc :+ InputFieldEvent.AddCharacter(inputField.bindingKey, keyCode.printableCharacter)

          case _ =>
            acc
        }
      }
    }

    def render(position: Point, depth: Depth, inputField: InputField, inputFieldAssets: InputFieldAssets): RenderedInputFieldElements = {
      inputField.state match {
        case InputFieldState.Normal =>
          RenderedInputFieldElements(inputFieldAssets.text.withText(inputField.text).moveTo(position).withDepth(depth.zIndex), None)

        case InputFieldState.HasFocus =>
          RenderedInputFieldElements(
            inputFieldAssets.text.withText(inputField.text).moveTo(position).withDepth(depth.zIndex),
            Option(inputFieldAssets.cursor.moveTo(position + Point(0, 10)).withDepth(depth.zIndex + 1))
          )
      }
    }

    //TODO: Cursor position and blink...
    def update(position: Point, depth: Depth, inputField: InputField, frameEvents: FrameInputEvents, inputFieldAssets: InputFieldAssets): InputFieldViewUpdate = {
      val rendered: RenderedInputFieldElements = render(position, depth, inputField, inputFieldAssets)

      InputFieldViewUpdate(
        rendered.toNodes,
        applyEvent(rendered.field.bounds.moveTo(position), inputField, frameEvents)
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

  def addCharacter(inputField: InputField, char: String): InputField = {
    val splitString = inputField.text.splitAt(inputField.cursorPosition)

    inputField.copy(
      text = splitString._1 + char + splitString._2,
      cursorPosition = inputField.cursorPosition + 1
    )
  }

}

case class InputField(state: InputFieldState, text: String, cursorPosition: Int, bindingKey: BindingKey = BindingKey.generate) {

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

  def addCharacter(char: String): InputField =
    InputField.addCharacter(this, char)

}

case class RenderedInputFieldElements(field: Text, cursor: Option[Graphic]) {
  def toNodes: List[SceneGraphNode] =
    List(field) ++ cursor.map(c => List(c)).getOrElse(Nil)
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
  case class AddCharacter(bindingKey: BindingKey, char: String) extends InputFieldEvent
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