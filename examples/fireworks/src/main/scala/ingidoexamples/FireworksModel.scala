package ingidoexamples

import indigo._

final case class FireworksModel()

object FireworksModel {

  def generateStartPosition(dice: Dice, min: Point, max: Point): Point =
    Point(min.x + dice.roll(max.x - min.x), min.y)

}
