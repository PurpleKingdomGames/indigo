package indigoexts.uicomponents

import indigo.gameengine.GameTime
import indigo.gameengine.assets.FontRegister
import indigo.gameengine.constants.Keys
import indigo.gameengine.events.{FrameInputEvents, KeyboardEvent, MouseEvent, GlobalEvent}
import indigo.gameengine.scenegraph.datatypes._
import indigo.gameengine.scenegraph.{Graphic, SceneGraphNode, SceneUpdateFragment, Text}

import indigo.Eq._

object InputField {

  def apply(text: String): InputField =
    InputField(InputFieldState.Normal, text, 0, InputFieldOptions.default, BindingKey.generate)

  object Model {

    def update(inputField: InputField, inputFieldEvent: InputFieldEvent): InputField =
      inputFieldEvent match {
        case InputFieldEvent.Delete(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.delete

        case InputFieldEvent.Backspace(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.backspace

        case InputFieldEvent.CursorLeft(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.cursorLeft

        case InputFieldEvent.CursorRight(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.cursorRight

        case InputFieldEvent.CursorHome(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.cursorHome

        case InputFieldEvent.CursorEnd(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.cursorEnd

        case InputFieldEvent.GiveFocus(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.giveFocus

        case InputFieldEvent.LoseFocus(bindingKey) if inputField.bindingKey === bindingKey =>
          inputField.loseFocus

        case InputFieldEvent.AddCharacter(bindingKey, char) if inputField.bindingKey === bindingKey =>
          inputField.addCharacter(char)

        case _ =>
          inputField
      }

  }

  object View {

    def applyEvent(bounds: Rectangle, inputField: InputField, frameInputEvents: FrameInputEvents): List[InputFieldEvent] =
      frameInputEvents.globalEvents.foldLeft[List[InputFieldEvent]](Nil) { (acc, e) =>
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

          case KeyboardEvent.KeyPress(Keys.HOME) if inputField.state.hasFocus =>
            acc :+ InputFieldEvent.CursorHome(inputField.bindingKey)

          case KeyboardEvent.KeyPress(Keys.END) if inputField.state.hasFocus =>
            acc :+ InputFieldEvent.CursorEnd(inputField.bindingKey)

          case KeyboardEvent.KeyUp(keyCode) if inputField.state.hasFocus && keyCode.isPrintable =>
            acc :+ InputFieldEvent.AddCharacter(inputField.bindingKey, keyCode.printableCharacter)

          case _ =>
            acc
        }
      }

    private def calculateCursorPosition(textLine: String, offset: Point, fontInfo: FontInfo, cursorPosition: Int): Point = {
      val lines      = textLine.substring(0, cursorPosition).split('\n')
      val lineCount  = Math.max(0, lines.length - 1) + (if (textLine.takeRight(1) === "\n") 1 else 0)
      val lineHeight = Text.calculateBoundsOfLine("a", fontInfo).height
      val lastLine   = if (textLine.takeRight(1) === "\n") "" else lines.reverse.headOption.getOrElse("")
      val bounds     = Text.calculateBoundsOfLine(lastLine, fontInfo)

      Point(bounds.size.x, 0) + offset + Point(0, lineHeight * lineCount)
    }

    private def drawCursor(gameTime: GameTime, inputField: InputField, position: Point, depth: Depth, inputFieldAssets: InputFieldAssets): Option[Graphic] =
      if (((gameTime.running * 0.00001) * 150).toInt % 2 === 0)
        FontRegister.findByFontKey(inputFieldAssets.text.fontKey).map { fontInfo =>
          inputFieldAssets.cursor
            .moveTo(calculateCursorPosition(inputField.text, position, fontInfo, inputField.cursorPosition))
            .withDepth(Depth(-(depth.zIndex + 100)))
        } else
        None

    private def render(gameTime: GameTime, position: Point, depth: Depth, inputField: InputField, inputFieldAssets: InputFieldAssets): RenderedInputFieldElements =
      inputField.state match {
        case InputFieldState.Normal =>
          RenderedInputFieldElements(inputFieldAssets.text.withText(inputField.text).moveTo(position).withDepth(depth), None)

        case InputFieldState.HasFocus =>
          RenderedInputFieldElements(
            inputFieldAssets.text.withText(inputField.text).moveTo(position).withDepth(depth),
            drawCursor(gameTime, inputField, position, depth, inputFieldAssets)
          )
      }

    def update(gameTime: GameTime, position: Point, depth: Depth, inputField: InputField, frameEvents: FrameInputEvents, inputFieldAssets: InputFieldAssets): InputFieldViewUpdate = {
      val rendered: RenderedInputFieldElements = render(gameTime, position, depth, inputField, inputFieldAssets)

      InputFieldViewUpdate(
        rendered.toNodes,
        applyEvent(rendered.field.bounds.moveTo(position), inputField, frameEvents)
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
        text = (splitString._1 + char + splitString._2).replaceAllLiterally("\n\n", "\n"),
        cursorPosition = inputField.cursorPosition + 1
      )
    } else inputField

}

final case class InputField(state: InputFieldState, text: String, cursorPosition: Int, options: InputFieldOptions, bindingKey: BindingKey) {

  def update(inputFieldEvent: InputFieldEvent): InputField =
    InputField.Model.update(this, inputFieldEvent)

  def draw(gameTime: GameTime, position: Point, depth: Depth, frameInputEvents: FrameInputEvents, inputFieldAssets: InputFieldAssets): InputFieldViewUpdate =
    InputField.View.update(gameTime, position, depth, this, frameInputEvents, inputFieldAssets)

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
    SceneUpdateFragment()
      .addGameLayerNodes(sceneGraphNodes)
      .addViewEvents(inputFieldEvents)

  def toTuple: (List[SceneGraphNode], List[InputFieldEvent]) =
    (sceneGraphNodes, inputFieldEvents)
}

sealed trait InputFieldEvent extends GlobalEvent {
  val bindingKey: BindingKey
}
object InputFieldEvent {
  final case class Delete(bindingKey: BindingKey)                     extends InputFieldEvent
  final case class Backspace(bindingKey: BindingKey)                  extends InputFieldEvent
  final case class CursorLeft(bindingKey: BindingKey)                 extends InputFieldEvent
  final case class CursorRight(bindingKey: BindingKey)                extends InputFieldEvent
  final case class CursorHome(bindingKey: BindingKey)                 extends InputFieldEvent
  final case class CursorEnd(bindingKey: BindingKey)                  extends InputFieldEvent
  final case class GiveFocus(bindingKey: BindingKey)                  extends InputFieldEvent
  final case class LoseFocus(bindingKey: BindingKey)                  extends InputFieldEvent
  final case class AddCharacter(bindingKey: BindingKey, char: String) extends InputFieldEvent
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
