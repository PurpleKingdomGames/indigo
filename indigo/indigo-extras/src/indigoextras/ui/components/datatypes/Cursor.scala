package indigoextras.ui.components.datatypes

import indigo.*

final case class Cursor(
    position: Int,
    blinkRate: Option[Seconds],
    lastModified: Seconds
):

  def moveTo(position: Int): Cursor =
    this.copy(position = position)

  def noCursorBlink: Cursor =
    this.copy(blinkRate = None)
  def withCursorBlinkRate(interval: Seconds): Cursor =
    this.copy(blinkRate = Some(interval))

  def withLastCursorMove(value: Seconds): Cursor =
    this.copy(lastModified = value)

  def cursorLeft: Cursor =
    this.copy(position = if (position - 1 >= 0) position - 1 else position)

  def cursorRight(maxLength: Int): Cursor =
    this.copy(position = if (position + 1 <= maxLength) position + 1 else maxLength)

  def cursorHome: Cursor =
    this.copy(position = 0)

  def moveCursorTo(newCursorPosition: Int, maxLength: Int): Cursor =
    if newCursorPosition >= 0 && newCursorPosition <= maxLength then this.copy(position = newCursorPosition)
    else if newCursorPosition < 0 then this.copy(position = 0)
    else this.copy(position = Math.max(0, maxLength))

  def cursorEnd(maxLength: Int): Cursor =
    this.copy(position = maxLength)

object Cursor:
  val default: Cursor =
    Cursor(0, Option(Seconds(0.5)), Seconds.zero)
