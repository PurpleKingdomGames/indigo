package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.dice.Dice

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class DiceDelegate(dice: Dice) {

  @JSExport
  def rollInt: Int = dice.roll

  @JSExport
  def roll(sides: Int): Int = dice.roll(sides)

  @JSExport
  def rollDouble: Double = dice.rollDouble

}
