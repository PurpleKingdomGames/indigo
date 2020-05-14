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

final case class InputField(
    bindingKey: BindingKey,
    text: String,
    characterLimit: Int,
    multiLine: Boolean,
    private val hasFocus: Boolean,
    private val cursorPosition: Int
) {

  def giveFocus: InputField =
    this.copy(
      hasFocus = true,
      cursorPosition = this.text.length
    )

  def loseFocus: InputField =
    this.copy(
      hasFocus = false,
      cursorPosition = 0
    )

  def withCharacterLimit(limit: Int): InputField =
    this.copy(characterLimit = limit)

  def makeMultiLine: InputField =
    this.copy(multiLine = true)

  def makeSingleLine: InputField =
    this.copy(multiLine = false)

  def cursorLeft: InputField =
    if (hasFocus) {
      this.copy(cursorPosition = if (cursorPosition - 1 >= 0) cursorPosition - 1 else cursorPosition)
    } else this

  def cursorRight: InputField =
    if (hasFocus) {
      this.copy(cursorPosition = if (cursorPosition + 1 <= text.length) cursorPosition + 1 else text.length)
    } else this

  def cursorHome: InputField =
    if (hasFocus) {
      this.copy(cursorPosition = 0)
    } else this

  def cursorEnd: InputField =
    if (hasFocus) {
      this.copy(cursorPosition = text.length)
    } else this

  def delete: InputField =
    if (hasFocus) {
      val splitString = text.splitAt(cursorPosition)

      if (splitString._2.isEmpty) this
      else
        copy(
          text = splitString._1 + splitString._2.substring(1)
        )
    } else this

  def backspace: InputField =
    if (hasFocus) {
      val splitString = text.splitAt(cursorPosition)

      if (splitString._1.isEmpty) this
      else {
        this.copy(
          text = splitString._1.take(splitString._1.length - 1) + splitString._2,
          cursorPosition = if (cursorPosition > 0) cursorPosition - 1 else cursorPosition
        )
      }
    } else this

  def addCharacter(char: String): InputField =
    if (hasFocus) {
      if (text.length < characterLimit && ((char !== "\n") || multiLine)) {
        val splitString = text.splitAt(cursorPosition)

        this.copy(
          text = (splitString._1 + char + splitString._2).replace("\n\n", "\n"),
          cursorPosition = cursorPosition + 1
        )
      } else this
    } else this

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

      case InputFieldEvent.GiveFocus(bindingKey) if bindingKey === bindingKey =>
        giveFocus

      case InputFieldEvent.LoseFocus(bindingKey) if bindingKey === bindingKey =>
        loseFocus

      case KeyboardEvent.KeyUp(Keys.ENTER) if hasFocus =>
        addCharacter(Keys.ENTER.key)

      case KeyboardEvent.KeyUp(key) if hasFocus && key.isPrintable =>
        addCharacter(key.key)

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
      SceneUpdateFragment(field)
        .addGlobalEvents(
          if (inputState.mouse.mouseReleased) {
            if (inputState.mouse.wasMouseUpWithin(field.bounds(boundaryLocator))) {
              List(InputFieldEvent.GiveFocus(bindingKey))
            } else {
              List(InputFieldEvent.LoseFocus(bindingKey))
            }
          } else {
            Nil
          }
        )

    if (hasFocus) {
      Signal
        .Pulse(Millis(250).toSeconds)
        .map {
          case false =>
            sceneUpdateFragment

          case true =>
            sceneUpdateFragment
              .addUiLayerNodes(
                cursor
                  .moveTo(calculateCursorPosition(boundaryLocator, this.text, text.fontKey, position, cursorPosition))
                  .withDepth(Depth(-(depth.zIndex + 100)))
              )
        }
        .at(gameTime.running)
    } else sceneUpdateFragment
  }

  private def calculateCursorPosition(boundaryLocator: BoundaryLocator, text: String, fontKey: FontKey, offset: Point, cursorPosition: Int): Point = {
    val linesWithBounds = boundaryLocator.textAsLinesWithBounds(text.substring(0, cursorPosition), fontKey)
    val lineCount       = linesWithBounds.length

    val res = for {
      lineHeight <- linesWithBounds.headOption.map(_.lineBounds.height)
      lastLine   <- linesWithBounds.reverse.headOption
    } yield Point(lastLine.lineBounds.size.x, 0) + offset + Point(0, lineHeight * lineCount)

    res.getOrElse(Point.zero)
  }

}

object InputField {

  def apply(text: String): InputField =
    InputField(BindingKey.generate, text, 255, false, false, 0)

  def apply(bindingKey: BindingKey, text: String): InputField =
    InputField(bindingKey, text, 255, false, false, 0)

  def apply(bindingKey: BindingKey, text: String, characterLimit: Int, multiLine: Boolean): InputField =
    InputField(bindingKey, text, characterLimit, multiLine, false, 0)

}

sealed trait InputFieldEvent extends GlobalEvent {
  val bindingKey: BindingKey
}
object InputFieldEvent {
  final case class GiveFocus(bindingKey: BindingKey) extends InputFieldEvent
  final case class LoseFocus(bindingKey: BindingKey) extends InputFieldEvent
}
