package indigoexts.uicomponents

import indigo.shared.time.GameTime
import indigo.shared.constants.Keys
import indigo.shared.events.{InputState, KeyboardEvent, GlobalEvent}
import indigo.shared.datatypes._
import indigo.shared.scenegraph.{Graphic, SceneUpdateFragment, Text}

import indigo.shared.EqualTo._
import indigo.shared.temporal.Signal
import indigo.shared.time.Millis
import indigo.shared.BoundaryLocator
import scala.collection.immutable.Nil
import scala.annotation.tailrec

final case class InputField(
    bindingKey: BindingKey,
    text: String,
    characterLimit: Int,
    multiLine: Boolean,
    hasFocus: Boolean,
    cursorPosition: Int
) {

  def giveFocus: InputField =
    this.copy(
      hasFocus = true,
      cursorPosition = this.text.length
    )

  def loseFocus: InputField =
    this.copy(
      hasFocus = false
    )

  def withCharacterLimit(limit: Int): InputField =
    this.copy(characterLimit = limit)

  def makeMultiLine: InputField =
    this.copy(multiLine = true)

  def makeSingleLine: InputField =
    this.copy(multiLine = false)

  def cursorLeft: InputField =
    this.copy(cursorPosition = if (cursorPosition - 1 >= 0) cursorPosition - 1 else cursorPosition)

  def cursorRight: InputField =
    this.copy(cursorPosition = if (cursorPosition + 1 <= text.length) cursorPosition + 1 else text.length)

  def cursorHome: InputField =
    this.copy(cursorPosition = 0)

  def cursorEnd: InputField =
    this.copy(cursorPosition = text.length)

  def delete: InputField = {
    val splitString = text.splitAt(cursorPosition)
    copy(text = splitString._1 + splitString._2.substring(1))
  }

  def backspace: InputField = {
    val splitString = text.splitAt(cursorPosition)

    this.copy(
      text = splitString._1.take(splitString._1.length - 1) + splitString._2,
      cursorPosition = if (cursorPosition > 0) cursorPosition - 1 else cursorPosition
    )
  }

  def addCharacter(char: Char): InputField =
    addCharacterText(char.toString())

  def addCharacterText(textToInsert: String): InputField = {
    @tailrec
    def rec(remaining: List[Char], textHead: String, textTail: String, position: Int): InputField =
      remaining match {
        case Nil =>
          this.copy(
            text = textHead + textTail,
            cursorPosition = position
          )

        case _ if (textHead + textTail).length >= characterLimit =>
          rec(Nil, textHead, textTail, position)

        case c :: cs if (c !== '\n') || multiLine =>
          rec(cs, textHead + c.toString(), textTail, position + 1)

        case _ :: cs =>
          rec(cs, textHead, textTail, position)
      }

    val splitString = text.splitAt(cursorPosition)

    rec(textToInsert.toCharArray().toList, splitString._1, splitString._2, cursorPosition)
  }

  def update(event: GlobalEvent): InputField =
    event match {
      case KeyboardEvent.KeyUp(Keys.BACKSPACE) if hasFocus =>
        backspace

      case KeyboardEvent.KeyUp(Keys.DELETE) if hasFocus =>
        delete

      case KeyboardEvent.KeyUp(Keys.LEFT_ARROW) if hasFocus =>
        cursorLeft

      case KeyboardEvent.KeyUp(Keys.RIGHT_ARROW) if hasFocus =>
        cursorRight

      case KeyboardEvent.KeyUp(Keys.HOME) if hasFocus =>
        cursorHome

      case KeyboardEvent.KeyUp(Keys.END) if hasFocus =>
        cursorEnd

      case InputFieldEvent.GiveFocus(bk) if bindingKey === bk =>
        giveFocus

      case InputFieldEvent.LoseFocus(bk) if bindingKey === bk =>
        loseFocus

      case KeyboardEvent.KeyUp(Keys.ENTER) if hasFocus =>
        addCharacterText(Keys.ENTER.key)

      case KeyboardEvent.KeyUp(key) if hasFocus && key.isPrintable =>
        addCharacterText(key.key)

      case _ =>
        this
    }

  def draw(
      gameTime: GameTime,
      position: Point,
      depth: Depth,
      inputState: InputState,
      text: Text,
      cursor: Graphic,
      boundaryLocator: BoundaryLocator
  ): SceneUpdateFragment = {
    val field =
      text
        .withText(this.text)
        .moveTo(position)
        .withDepth(depth)

    val sceneUpdateFragment =
      SceneUpdateFragment.empty
        .addUiLayerNodes(field)
        .addGlobalEvents(
          if (inputState.mouse.mouseReleased)
            if (inputState.mouse.wasMouseUpWithin(field.bounds(boundaryLocator))) {
              List(InputFieldEvent.GiveFocus(bindingKey))
            } else List(InputFieldEvent.LoseFocus(bindingKey))
          else Nil
        )

    if (hasFocus) {
      val cursorPositionPoint =
        boundaryLocator
          .textAsLinesWithBounds(field.text.substring(0, cursorPosition), field.fontKey)
          .reverse
          .headOption
          .map(_.lineBounds.topRight + position)
          .getOrElse(position)

      Signal
        .Pulse(Millis(250).toSeconds)
        .map {
          case false =>
            sceneUpdateFragment

          case true =>
            sceneUpdateFragment
              .addUiLayerNodes(
                cursor
                  .moveTo(cursorPositionPoint)
                  .withDepth(Depth(-(depth.zIndex + 100000)))
              )
        }
        .at(gameTime.running)
    } else sceneUpdateFragment
  }

}

object InputField {

  def apply(text: String): InputField =
    InputField(BindingKey.generate, text, 255, false, false, text.length())

  def apply(bindingKey: BindingKey, text: String): InputField =
    InputField(bindingKey, text, 255, false, false, text.length())

  def apply(bindingKey: BindingKey, text: String, characterLimit: Int, multiLine: Boolean): InputField =
    InputField(bindingKey, text, characterLimit, multiLine, false, text.length())

}

sealed trait InputFieldEvent extends GlobalEvent {
  val bindingKey: BindingKey
}
object InputFieldEvent {
  final case class GiveFocus(bindingKey: BindingKey) extends InputFieldEvent
  final case class LoseFocus(bindingKey: BindingKey) extends InputFieldEvent
}
