package indigoextras.ui.components

import indigo.*
import indigo.syntax.*
import indigoextras.ui.component.Component
import indigoextras.ui.components.datatypes.Cursor
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

import scala.annotation.tailrec

/** Input components allow the user to input text information.
  */
final case class Input(
    text: String,
    dimensions: Dimensions,
    render: (Coords, Bounds, Input, Seconds) => Outcome[Layer],
    change: String => Batch[GlobalEvent],
    //
    characterLimit: Int,
    cursor: Cursor,
    hasFocus: Boolean,
    onFocus: () => Batch[GlobalEvent],
    onLoseFocus: () => Batch[GlobalEvent]
):
  lazy val length: Int = text.length

  def withText(value: String): Input =
    this.copy(text = value)

  def withDimensions(value: Dimensions): Input =
    this.copy(dimensions = value)

  def withWidth(value: Int): Input =
    this.copy(dimensions = dimensions.withWidth(value))

  def onChange(events: String => Batch[GlobalEvent]): Input =
    this.copy(change = events)
  def onChange(events: Batch[GlobalEvent]): Input =
    onChange(_ => events)
  def onChange(events: GlobalEvent*): Input =
    onChange(Batch.fromSeq(events))

  def noCursorBlink: Input =
    this.copy(cursor = cursor.noCursorBlink)
  def withCursorBlinkRate(interval: Seconds): Input =
    this.copy(cursor = cursor.withCursorBlinkRate(interval))

  def giveFocus: Outcome[Input] =
    Outcome(
      this.copy(hasFocus = true),
      onFocus()
    )

  def loseFocus: Outcome[Input] =
    Outcome(
      this.copy(hasFocus = false),
      onLoseFocus()
    )

  def withCharacterLimit(limit: Int): Input =
    this.copy(characterLimit = limit)

  def withLastCursorMove(value: Seconds): Input =
    this.copy(cursor = cursor.withLastCursorMove(value))

  def cursorLeft: Input =
    this.copy(cursor = cursor.cursorLeft)

  def cursorRight: Input =
    this.copy(cursor = cursor.cursorRight(length))

  def cursorHome: Input =
    this.copy(cursor = cursor.cursorHome)

  def moveCursorTo(newCursorPosition: Int): Input =
    this.copy(cursor = cursor.moveCursorTo(newCursorPosition, length))

  def cursorEnd: Input =
    this.copy(cursor = cursor.cursorEnd(length))

  def delete: Input =
    if cursor.position == length then this
    else
      val splitString = text.splitAt(cursor.position)
      copy(text = splitString._1 + splitString._2.substring(1))

  def backspace: Input =
    val splitString = text.splitAt(cursor.position)

    this.copy(
      text = splitString._1.take(splitString._1.length - 1) + splitString._2,
      cursor = cursor.moveTo(
        if cursor.position > 0 then cursor.position - 1 else cursor.position
      )
    )

  def addCharacter(char: Char): Input =
    addCharacterText(char.toString())

  def addCharacterText(textToInsert: String): Input = {
    @tailrec
    def rec(remaining: List[Char], textHead: String, textTail: String, position: Int): Input =
      remaining match
        case Nil =>
          this.copy(
            text = textHead + textTail,
            cursor = cursor.moveTo(position)
          )

        case _ if (textHead + textTail).length >= characterLimit =>
          rec(Nil, textHead, textTail, position)

        case c :: cs if c != '\n' =>
          rec(cs, textHead + c.toString(), textTail, position + 1)

        case _ :: cs =>
          rec(cs, textHead, textTail, position)

    val splitString = text.splitAt(cursor.position)

    rec(textToInsert.toCharArray().toList, splitString._1, splitString._2, cursor.position)
  }

  def withFocusActions(actions: GlobalEvent*): Input =
    withFocusActions(Batch.fromSeq(actions))
  def withFocusActions(actions: => Batch[GlobalEvent]): Input =
    this.copy(onFocus = () => actions)

  def withLoseFocusActions(actions: GlobalEvent*): Input =
    withLoseFocusActions(Batch.fromSeq(actions))
  def withLoseFocusActions(actions: => Batch[GlobalEvent]): Input =
    this.copy(onLoseFocus = () => actions)

object Input:

  /** Minimal input constructor with custom rendering function
    */
  def apply(dimensions: Dimensions)(
      present: (Coords, Bounds, Input, Seconds) => Outcome[Layer]
  ): Input =
    Input(
      "",
      dimensions,
      present,
      _ => Batch.empty,
      //
      characterLimit = dimensions.width,
      cursor = Cursor.default,
      hasFocus = false,
      () => Batch.empty,
      () => Batch.empty
    )

  given [ReferenceData]: Component[Input, ReferenceData] with
    def bounds(reference: ReferenceData, model: Input): Bounds =
      Bounds(model.dimensions).resizeBy(2, 2)

    def updateModel(
        context: UIContext[ReferenceData],
        model: Input
    ): GlobalEvent => Outcome[Input] =
      case _: PointerEvent.Click
          if context.isActive && Bounds(model.dimensions)
            .resizeBy(2, 2)
            .moveBy(context.bounds.coords)
            .contains(context.pointerCoords) =>
        model
          .moveCursorTo(context.pointerCoords.x - context.bounds.coords.x - 1)
          .giveFocus

      case _: PointerEvent.Click =>
        model.loseFocus

      case KeyboardEvent.KeyUp(Key.BACKSPACE) if model.hasFocus =>
        val next = model.backspace.withLastCursorMove(context.frame.time.running)
        Outcome(next, model.change(next.text))

      case KeyboardEvent.KeyUp(Key.DELETE) if model.hasFocus =>
        val next = model.delete.withLastCursorMove(context.frame.time.running)
        Outcome(next, model.change(next.text))

      case KeyboardEvent.KeyUp(Key.ARROW_LEFT) if model.hasFocus =>
        Outcome(model.cursorLeft.withLastCursorMove(context.frame.time.running))

      case KeyboardEvent.KeyUp(Key.ARROW_RIGHT) if model.hasFocus =>
        Outcome(model.cursorRight.withLastCursorMove(context.frame.time.running))

      case KeyboardEvent.KeyUp(Key.HOME) if model.hasFocus =>
        Outcome(model.cursorHome.withLastCursorMove(context.frame.time.running))

      case KeyboardEvent.KeyUp(Key.END) if model.hasFocus =>
        Outcome(model.cursorEnd.withLastCursorMove(context.frame.time.running))

      case KeyboardEvent.KeyUp(Key.ENTER) if model.hasFocus =>
        // Enter key is ignored. Single line input fields.
        Outcome(model.withLastCursorMove(context.frame.time.running))

      case KeyboardEvent.KeyUp(key) if model.hasFocus && key.isPrintable =>
        val next = model.addCharacterText(key.key).withLastCursorMove(context.frame.time.running)
        Outcome(next, model.change(next.text))

      case FrameTick if !context.isActive =>
        model.loseFocus

      case _ =>
        Outcome(model)

    def present(
        context: UIContext[ReferenceData],
        model: Input
    ): Outcome[Layer] =
      model.render(
        context.bounds.coords,
        Bounds(model.dimensions),
        model,
        context.frame.time.running
      )

    def refresh(reference: ReferenceData, model: Input, parentDimensions: Dimensions): Input =
      model
