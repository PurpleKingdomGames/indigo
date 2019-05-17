package ingidoexamples.model

import indigo._

final case class Fuse(position: Point, length: Millis)

object Fuse {

  def generateFuse(dice: Dice, min: Point, max: Point): Fuse =
    Fuse(Point(min.x + dice.roll(max.x - min.x), min.y), Millis(dice.roll(1000).toLong))

}

