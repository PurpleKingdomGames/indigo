package indigoexts.uicomponents

import indigo.shared.time.GameTime
import indigo.shared.constants.Keys
import indigo.shared.events.{InputState, KeyboardEvent, GlobalEvent}
import indigo.shared.datatypes._
import indigo.shared.scenegraph.{Graphic, SceneGraphNode, SceneUpdateFragment, Text}

import indigo.shared.EqualTo._
import indigo.shared.temporal.Signal
import indigo.shared.time.Millis
import indigo.shared.BoundaryLocator

object InputField {

  def apply(text: String, boundaryLocator: BoundaryLocator): InputField =
    InputField(InputFieldState.Normal, text, 0, InputFieldOptions.default, BindingKey.generate, boundaryLocator)

  object Model {

    def update(inputField: InputField, event: GlobalEvent): InputField =
      event match {
        case KeyboardEvent.KeyUp(Keys.BACKSPACE) if inputField.state.hasFocus =>
          inputField.backspace

        case KeyboardEvent.KeyUp(Keys.DELETE) if inputField.state.hasFocus =>
          inputField.delete

        case KeyboardEvent.KeyUp(Keys.LEFT_ARROW) if inputField.state.hasFocus =>
          inputField.cursorLeft

        case KeyboardEvent.KeyUp(Keys.RIGHT_ARROW) if inputField.state.hasFocus =>
          inputField.cursorRight

        case KeyboardEvent.KeyUp(Keys.HOME) if inputField.state.hasFocus =>
          inputField.cursorHome

        case KeyboardEvent.KeyUp(Keys.END) if inputField.state.hasFocus =>
          inputField.cursorEnd

        case InputFieldEvent.GiveFocus(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.giveFocus

        case InputFieldEvent.LoseFocus(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.loseFocus

        case KeyboardEvent.KeyUp(Keys.ENTER) if inputField.state.hasFocus =>
          inputField.addCharacter(Keys.ENTER.key)

        case KeyboardEvent.KeyUp(key) if inputField.state.hasFocus && key.isPrintable =>
          inputField.addCharacter(key.key)

        case _ =>
          inputField
      }

  }

  object View {

    def applyEvent(bounds: Rectangle, inputField: InputField, inputState: InputState): List[InputFieldEvent] =
      if (inputState.mouse.mouseReleased) {
        if (inputState.mouse.wasMouseUpWithin(bounds)) {
          List(InputFieldEvent.GiveFocus(inputField.bindingKey))
        } else {
          List(InputFieldEvent.LoseFocus(inputField.bindingKey))
        }
      } else {
        Nil
      }

    private def calculateCursorPosition(boundaryLocator: BoundaryLocator, text: String, fontKey: FontKey, offset: Point, cursorPosition: Int): Option[Point] = {
      val linesWithBounds = boundaryLocator.textAsLinesWithBounds(text.substring(0, cursorPosition), fontKey)
      val lineCount       = linesWithBounds.length

      for {
        lineHeight <- linesWithBounds.headOption.map(_.lineBounds.height)
        lastLine   <- linesWithBounds.reverse.headOption
      } yield Point(lastLine.lineBounds.size.x, 0) + offset + Point(0, lineHeight * lineCount)
    }

    private def drawCursor(boundaryLocator: BoundaryLocator, gameTime: GameTime, inputField: InputField, position: Point, depth: Depth, inputFieldAssets: InputFieldAssets): Option[Graphic] =
      Signal
        .Pulse(Millis(150).toSeconds)
        .map {
          case false =>
            None

          case true =>
            val cursorPosition: Option[Point] =
              calculateCursorPosition(boundaryLocator, inputField.text, inputFieldAssets.text.fontKey, position, inputField.cursorPosition)

            val cursor =
              cursorPosition.map { pt =>
                inputFieldAssets.cursor
                  .moveTo(pt)
                  .withDepth(Depth(-(depth.zIndex + 100)))
              }
            cursor
        }
        .at(gameTime.running)

    private def render(boundaryLocator: BoundaryLocator, gameTime: GameTime, position: Point, depth: Depth, inputField: InputField, inputFieldAssets: InputFieldAssets): RenderedInputFieldElements =
      inputField.state match {
        case InputFieldState.Normal =>
          RenderedInputFieldElements(inputFieldAssets.text.withText(inputField.text).moveTo(position).withDepth(depth), None)

        case InputFieldState.HasFocus =>
          RenderedInputFieldElements(
            inputFieldAssets.text.withText(inputField.text).moveTo(position).withDepth(depth),
            drawCursor(boundaryLocator, gameTime, inputField, position, depth, inputFieldAssets)
          )
      }

    def update(
        boundaryLocator: BoundaryLocator,
        gameTime: GameTime,
        position: Point,
        depth: Depth,
        inputField: InputField,
        frameEvents: InputState,
        inputFieldAssets: InputFieldAssets
    ): InputFieldViewUpdate = {
      val rendered: RenderedInputFieldElements = render(boundaryLocator, gameTime, position, depth, inputField, inputFieldAssets)

      InputFieldViewUpdate(
        rendered.toNodes,
        applyEvent(rendered.field.bounds(boundaryLocator), inputField, frameEvents)
      )
    }

  }

  def deleteCharacter(inputField: InputField): InputField = {
    val splitString = inputField.text.splitAt(inputField.cursorPosition)

    if (splitString._2.isEmpty) inputField
    else
      inputField.copy(
        text = splitString._1 + splitString._2.substring(1)
      )
  }

  def backspace(inputField: InputField): InputField = {
    val splitString = inputField.text.splitAt(inputField.cursorPosition)

    if (splitString._1.isEmpty) inputField
    else
      inputField.copy(
        text = splitString._1.take(splitString._1.length - 1) + splitString._2,
        cursorPosition = if (inputField.cursorPosition > 0) inputField.cursorPosition - 1 else inputField.cursorPosition
      )
  }

  def addCharacter(inputField: InputField, char: String): InputField =
    if (inputField.text.length < inputField.options.characterLimit && ((char !== "\n") || inputField.options.multiLine)) {
      val splitString = inputField.text.splitAt(inputField.cursorPosition)

      inputField.copy(
        text = (splitString._1 + char + splitString._2).replace("\n\n", "\n"),
        cursorPosition = inputField.cursorPosition + 1
      )
    } else inputField

}

final case class InputField(state: InputFieldState, text: String, cursorPosition: Int, options: InputFieldOptions, bindingKey: BindingKey, boundaryLocator: BoundaryLocator) {

  def update(inputFieldEvent: InputFieldEvent): InputField =
    InputField.Model.update(this, inputFieldEvent)

  def draw(gameTime: GameTime, position: Point, depth: Depth, inputState: InputState, inputFieldAssets: InputFieldAssets): InputFieldViewUpdate =
    InputField.View.update(boundaryLocator, gameTime, position, depth, this, inputState, inputFieldAssets)

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
    this.copy(cursorPosition = if (cursorPosition - 1 >= 0) cursorPosition - 1 else cursorPosition)

  def cursorRight: InputField =
    this.copy(cursorPosition = if (cursorPosition + 1 <= text.length) cursorPosition + 1 else text.length)

  def cursorHome: InputField =
    this.copy(cursorPosition = 0)

  def cursorEnd: InputField =
    this.copy(cursorPosition = text.length)

  def delete: InputField =
    InputField.deleteCharacter(this)

  def backspace: InputField =
    InputField.backspace(this)

  def addCharacter(char: String): InputField =
    InputField.addCharacter(this, char)

  def withCharacterLimit(limit: Int): InputField =
    this.copy(options = options.withCharacterLimit(limit))

  def makeMultiLine: InputField =
    this.copy(options = options.makeMultiLine)

  def makeSingleLine: InputField =
    this.copy(options = options.makeSingleLine)

}

final case class InputFieldOptions(characterLimit: Int, multiLine: Boolean) {

  def withCharacterLimit(limit: Int): InputFieldOptions =
    this.copy(characterLimit = limit)

  def makeMultiLine: InputFieldOptions =
    this.copy(multiLine = true)

  def makeSingleLine: InputFieldOptions =
    this.copy(multiLine = false)

}
object InputFieldOptions {
  val default: InputFieldOptions =
    InputFieldOptions(characterLimit = 255, multiLine = false)
}

final case class RenderedInputFieldElements(field: Text, cursor: Option[Graphic]) {
  def toNodes: List[SceneGraphNode] =
    List(field) ++ cursor.map(c => List(c)).getOrElse(Nil)
}

final case class InputFieldAssets(text: Text, cursor: Graphic)

final case class InputFieldViewUpdate(sceneGraphNodes: List[SceneGraphNode], inputFieldEvents: List[InputFieldEvent]) {
  def toSceneUpdateFragment: SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(sceneGraphNodes)
      .addGlobalEvents(inputFieldEvents)

  def toTuple: (List[SceneGraphNode], List[InputFieldEvent]) =
    (sceneGraphNodes, inputFieldEvents)
}

sealed trait InputFieldEvent extends GlobalEvent {
  val bindingKey: BindingKey
}
object InputFieldEvent {
  final case class GiveFocus(bindingKey: BindingKey) extends InputFieldEvent
  final case class LoseFocus(bindingKey: BindingKey) extends InputFieldEvent
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
