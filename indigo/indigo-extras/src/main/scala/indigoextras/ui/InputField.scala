package indigoextras.ui

import indigo.shared.BoundaryLocator
import indigo.shared.FrameContext
import indigo.shared.Outcome
import indigo.shared.constants.Key
import indigo.shared.datatypes._
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.Text
import indigo.shared.temporal.Signal
import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

import scala.annotation.tailrec
import scala.collection.immutable.Nil

final case class InputField(
    text: String,
    characterLimit: Int,
    multiLine: Boolean,
    assets: InputFieldAssets,
    cursorBlinkRate: Option[Seconds],
    position: Point,
    depth: Depth,
    hasFocus: Boolean,
    cursorPosition: Int,
    lastCursorMove: Seconds,
    key: Option[BindingKey],
    onFocus: () => List[GlobalEvent],
    onLoseFocus: () => List[GlobalEvent]
) derives CanEqual:

  def bounds(boundaryLocator: BoundaryLocator): Option[Rectangle] =
    boundaryLocator.findBounds(assets.text.withText(text).moveTo(position))

  def withText(newText: String): InputField =
    this.copy(
      text = newText,
      assets = assets.withText(assets.text.withText(newText))
    )

  def withAssets(newAssets: InputFieldAssets): InputField =
    this.copy(assets = newAssets)

  def noCursorBlink: InputField =
    this.copy(cursorBlinkRate = None)
  def withCursorBlinkRate(interval: Seconds): InputField =
    this.copy(cursorBlinkRate = Some(interval))

  def moveTo(x: Int, y: Int): InputField =
    moveTo(Point(x, y))
  def moveTo(newPosition: Point): InputField =
    this.copy(position = newPosition)

  def moveBy(x: Int, y: Int): InputField =
    moveBy(Point(x, y))
  def moveBy(positionDiff: Point): InputField =
    this.copy(position = position + positionDiff)

  def withDepth(newDepth: Depth): InputField =
    this.copy(
      depth = newDepth,
      assets = assets.withText(assets.text.withDepth(newDepth))
    )

  def withKey(newKey: BindingKey): InputField =
    this.copy(key = Option(newKey))

  def giveFocus: Outcome[InputField] =
    Outcome(
      this.copy(hasFocus = true, cursorPosition = this.text.length),
      onFocus()
    )

  def loseFocus: Outcome[InputField] =
    Outcome(
      this.copy(hasFocus = false),
      onLoseFocus()
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

  def moveCursorTo(newCursorPosition: Int): InputField =
    if (newCursorPosition >= 0 && newCursorPosition < text.length())
      this.copy(cursorPosition = newCursorPosition)
    else if (newCursorPosition < 0) this.copy(cursorPosition = 0)
    else this.copy(cursorPosition = text.length() - 1)

  def cursorEnd: InputField =
    this.copy(cursorPosition = text.length)

  def delete: InputField =
    if (cursorPosition == text.length()) this
    else {
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

  private given CanEqual[List[Char], List[Char]] = CanEqual.derived

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

        case c :: cs if (c != '\n') || multiLine =>
          rec(cs, textHead + c.toString(), textTail, position + 1)

        case _ :: cs =>
          rec(cs, textHead, textTail, position)
      }

    val splitString = text.splitAt(cursorPosition)

    rec(textToInsert.toCharArray().toList, splitString._1, splitString._2, cursorPosition)
  }

  def withFocusActions(actions: GlobalEvent*): InputField =
    withFocusActions(actions.toList)
  def withFocusActions(actions: => List[GlobalEvent]): InputField =
    this.copy(onFocus = () => actions)

  def withLoseFocusActions(actions: GlobalEvent*): InputField =
    withLoseFocusActions(actions.toList)
  def withLoseFocusActions(actions: => List[GlobalEvent]): InputField =
    this.copy(onLoseFocus = () => actions)

  def update(frameContext: FrameContext[_]): Outcome[InputField] = {
    @tailrec
    def rec(
        keysReleased: List[Key],
        acc: InputField,
        touched: Boolean,
        changeEvent: Option[InputFieldChange]
    ): Outcome[InputField] =
      keysReleased match {
        case Nil =>
          if (touched)
            Outcome(acc.copy(lastCursorMove = frameContext.gameTime.running), changeEvent.toList)
          else
            Outcome(acc, changeEvent.toList)

        case Key.BACKSPACE :: ks =>
          val next = acc.backspace
          rec(ks, next, true, acc.key.map(key => InputFieldChange(key, next.text)))

        case Key.DELETE :: ks =>
          val next = acc.delete
          rec(ks, next, true, acc.key.map(key => InputFieldChange(key, next.text)))

        case Key.LEFT_ARROW :: ks =>
          rec(ks, acc.cursorLeft, true, changeEvent)

        case Key.RIGHT_ARROW :: ks =>
          rec(ks, acc.cursorRight, true, changeEvent)

        case Key.HOME :: ks =>
          rec(ks, acc.cursorHome, true, changeEvent)

        case Key.END :: ks =>
          rec(ks, acc.cursorEnd, true, changeEvent)

        case Key.ENTER :: ks =>
          val next = acc.addCharacterText(Key.ENTER.key)
          rec(ks, next, true, acc.key.map(key => InputFieldChange(key, next.text)))

        case key :: ks if key.isPrintable =>
          val next = acc.addCharacterText(key.key)
          rec(ks, next, true, acc.key.map(key => InputFieldChange(key, next.text)))

        case _ :: ks =>
          rec(ks, acc, touched, changeEvent)
      }

    val updated: Outcome[InputField] =
      if (hasFocus)
        rec(frameContext.inputState.keyboard.keysReleased, this, false, None)
      else Outcome(this)

    if (frameContext.inputState.mouse.mouseReleased)
      bounds(frameContext.boundaryLocator) match
        case Some(bounds) =>
          if frameContext.inputState.mouse.wasMouseUpWithin(bounds) then updated.flatMap(_.giveFocus)
          else updated.flatMap(_.loseFocus)
        case _ =>
          updated
    else updated
  }

  def draw(
      gameTime: GameTime,
      boundaryLocator: BoundaryLocator
  ): List[SceneNode] = {
    val field =
      assets.text
        .withText(this.text)
        .moveTo(position)
        .withDepth(depth)

    if (hasFocus) {

      val textToCursor = {
        // This odd bit of code forces line splitting code to acknowledge
        // the newline, and move the cursor to the start of the next line
        // rather than the end of the previous one.
        val t = field.text.substring(0, cursorPosition)
        if (t.endsWith("\n")) t + "\n"
        else t
      }

      val cursorPositionPoint =
        boundaryLocator
          .textAsLinesWithBounds(textToCursor, field.fontKey)
          .reverse
          .headOption
          .map(_.lineBounds.topRight + position)
          .getOrElse(position)

      cursorBlinkRate match {
        case None =>
          List(
            field,
            assets.cursor
              .moveTo(cursorPositionPoint)
              .withDepth(Depth(-(depth.toInt + 100000)))
          )

        case Some(seconds) =>
          Signal
            .Pulse(seconds)
            .map(p => if (gameTime.running - lastCursorMove < Seconds(0.5)) true else p)
            .map {
              case false =>
                List(field)

              case true =>
                List(
                  field,
                  assets.cursor
                    .moveTo(cursorPositionPoint)
                    .withDepth(Depth(-(depth.toInt + 100000)))
                )
            }
            .at(gameTime.running)
      }

    } else List(field)
  }

object InputField:

  def apply(text: String, assets: InputFieldAssets): InputField =
    InputField(
      text,
      255,
      false,
      assets,
      Some(Millis(400).toSeconds),
      Point.zero,
      Depth.zero,
      false,
      text.length(),
      Seconds.zero,
      None,
      () => Nil,
      () => Nil
    )

  def apply(text: String, characterLimit: Int, multiLine: Boolean, assets: InputFieldAssets): InputField =
    InputField(
      text,
      characterLimit,
      multiLine,
      assets,
      Some(Millis(400).toSeconds),
      Point.zero,
      Depth.zero,
      false,
      text.length(),
      Seconds.zero,
      None,
      () => Nil,
      () => Nil
    )

final case class InputFieldAssets(text: Text[_], cursor: Graphic[_]) derives CanEqual:
  def withText(newText: Text[_]): InputFieldAssets =
    this.copy(text = newText)
  def withCursor(newCursor: Graphic[_]): InputFieldAssets =
    this.copy(cursor = newCursor)

final case class InputFieldChange(key: BindingKey, updatedText: String) extends GlobalEvent derives CanEqual
