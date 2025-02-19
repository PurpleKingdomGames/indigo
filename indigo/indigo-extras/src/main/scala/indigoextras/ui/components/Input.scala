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
final case class Input[ReferenceData](
    text: String,
    dimensions: Dimensions,
    render: (UIContext[ReferenceData], Input[ReferenceData]) => Outcome[Layer],
    change: String => Batch[GlobalEvent],
    calculateLineLength: (UIContext[ReferenceData], String) => Int,
    characterLimit: Int,
    cursor: Cursor,
    hasFocus: Boolean,
    onFocus: () => Batch[GlobalEvent],
    onLoseFocus: () => Batch[GlobalEvent]
):
  lazy val length: Int = text.length

  def withText(value: String): Input[ReferenceData] =
    this.copy(text = value)

  def withDimensions(value: Dimensions): Input[ReferenceData] =
    this.copy(dimensions = value)

  def withWidth(value: Int): Input[ReferenceData] =
    this.copy(dimensions = dimensions.withWidth(value))

  def onChange(events: String => Batch[GlobalEvent]): Input[ReferenceData] =
    this.copy(change = events)
  def onChange(events: Batch[GlobalEvent]): Input[ReferenceData] =
    onChange(_ => events)
  def onChange(events: GlobalEvent*): Input[ReferenceData] =
    onChange(Batch.fromSeq(events))

  def withCalculateLineLength(calc: (UIContext[ReferenceData], String) => Int): Input[ReferenceData] =
    this.copy(calculateLineLength = calc)
  def calculateLineLength(context: UIContext[ReferenceData]): Int =
    calculateLineLength(context, text)

  def noCursorBlink: Input[ReferenceData] =
    this.copy(cursor = cursor.noCursorBlink)
  def withCursorBlinkRate(interval: Seconds): Input[ReferenceData] =
    this.copy(cursor = cursor.withCursorBlinkRate(interval))

  def giveFocus: Outcome[Input[ReferenceData]] =
    Outcome(
      this.copy(hasFocus = true),
      onFocus()
    )

  def loseFocus: Outcome[Input[ReferenceData]] =
    Outcome(
      this.copy(hasFocus = false),
      onLoseFocus()
    )

  def withCharacterLimit(limit: Int): Input[ReferenceData] =
    this.copy(characterLimit = limit)

  def withLastCursorMove(value: Seconds): Input[ReferenceData] =
    this.copy(cursor = cursor.withLastCursorMove(value))

  def cursorLeft: Input[ReferenceData] =
    this.copy(cursor = cursor.cursorLeft)

  def cursorRight: Input[ReferenceData] =
    this.copy(cursor = cursor.cursorRight(length))

  def cursorHome: Input[ReferenceData] =
    this.copy(cursor = cursor.cursorHome)

  def moveCursorTo(newCursorPosition: Int): Input[ReferenceData] =
    this.copy(cursor = cursor.moveCursorTo(newCursorPosition, length))

  def cursorEnd: Input[ReferenceData] =
    this.copy(cursor = cursor.cursorEnd(length))

  def delete: Input[ReferenceData] =
    if cursor.position == length then this
    else
      val splitString = text.splitAt(cursor.position)
      copy(text = splitString._1 + splitString._2.substring(1))

  def backspace: Input[ReferenceData] =
    val splitString = text.splitAt(cursor.position)

    this.copy(
      text = splitString._1.take(splitString._1.length - 1) + splitString._2,
      cursor = cursor.moveTo(
        if cursor.position > 0 then cursor.position - 1 else cursor.position
      )
    )

  def withCursor(newCursor: Cursor): Input[ReferenceData] =
    this.copy(cursor = newCursor)

  def addCharacter(char: Char): Input[ReferenceData] =
    addCharacterText(char.toString())

  def addCharacterText(textToInsert: String): Input[ReferenceData] = {
    @tailrec
    def rec(remaining: List[Char], textHead: String, textTail: String, position: Int): Input[ReferenceData] =
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

  def withFocusActions(actions: GlobalEvent*): Input[ReferenceData] =
    withFocusActions(Batch.fromSeq(actions))
  def withFocusActions(actions: => Batch[GlobalEvent]): Input[ReferenceData] =
    this.copy(onFocus = () => actions)

  def withLoseFocusActions(actions: GlobalEvent*): Input[ReferenceData] =
    withLoseFocusActions(Batch.fromSeq(actions))
  def withLoseFocusActions(actions: => Batch[GlobalEvent]): Input[ReferenceData] =
    this.copy(onLoseFocus = () => actions)

object Input:

  /** Minimal input constructor with custom rendering function
    */
  def apply[ReferenceData](
      dimensions: Dimensions,
      calculateLineLength: (UIContext[ReferenceData], String) => Int
  )(
      present: (UIContext[ReferenceData], Input[ReferenceData]) => Outcome[Layer]
  ): Input[ReferenceData] =
    Input[ReferenceData](
      "",
      dimensions,
      present,
      _ => Batch.empty,
      calculateLineLength,
      characterLimit = dimensions.width,
      cursor = Cursor.default,
      hasFocus = false,
      () => Batch.empty,
      () => Batch.empty
    )

  given [ReferenceData]: Component[Input[ReferenceData], ReferenceData] with
    def bounds(context: UIContext[ReferenceData], model: Input[ReferenceData]): Bounds =
      Bounds(model.dimensions).resizeBy(2, 2)

    def updateModel(
        context: UIContext[ReferenceData],
        model: Input[ReferenceData]
    ): GlobalEvent => Outcome[Input[ReferenceData]] =
      case _: PointerEvent.Click
          if context.isActive && Bounds(model.dimensions)
            .resizeBy(2, 2)
            .moveBy(context.parent.coords)
            .contains(context.pointerCoords) =>

        val cursorPos = Input.findCursorPosition(
          context.pointerCoords.x - context.parent.coords.x,
          model.text,
          model.calculateLineLength.curried(context)
        )

        model
          .moveCursorTo(cursorPos)
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
        model: Input[ReferenceData]
    ): Outcome[Layer] =
      model.render(
        context,
        model
      )

    def refresh(
        context: UIContext[ReferenceData],
        model: Input[ReferenceData]
    ): Input[ReferenceData] =
      model

  def findCursorPosition(mouseX: Int, text: String, calcLineLength: String => Int): Int =
    @tailrec
    def rec(current: Int, lastX: Int): Int =
      val next = calcLineLength(text.take(current))
      if next > mouseX then
        val charLength: Double      = next - lastX
        val mouseWithinChar: Double = mouseX - lastX
        val threshold               = charLength * 0.5d

        if mouseWithinChar < threshold then current - 1
        else current
      else rec(current + 1, next)

    val res = rec(0, 0)
    res
