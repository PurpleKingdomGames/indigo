package indigoextras.ui.datatypes

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size

final case class Parent(_bounds: Bounds, additionalOffset: Coords):

  def bounds: Bounds =
    _bounds.moveBy(additionalOffset)

  def toRectangle(snapGrid: Size): Rectangle =
    bounds.moveBy(additionalOffset).toScreenSpace(snapGrid)
  lazy val unsafeToRectangle: Rectangle =
    bounds.moveBy(additionalOffset).unsafeToRectangle

  lazy val coords: Coords =
    bounds.coords + additionalOffset

  lazy val dimensions: Dimensions =
    bounds.dimensions

  def withBounds(newBounds: Bounds): Parent =
    this.copy(_bounds = newBounds)
  def moveTo(newPosition: Coords): Parent =
    this.copy(_bounds = bounds.moveTo(newPosition))
  def moveTo(x: Int, y: Int): Parent =
    this.copy(_bounds = bounds.moveTo(x, y))
  def moveBy(offset: Coords): Parent =
    this.copy(_bounds = bounds.moveBy(offset))
  def moveBy(x: Int, y: Int): Parent =
    this.copy(_bounds = bounds.moveBy(x, y))

  def withAdditionalOffset(offset: Coords): Parent =
    this.copy(additionalOffset = offset)
  def withAdditionalOffset(x: Int, y: Int): Parent =
    this.copy(additionalOffset = Coords(x, y))

object Parent:

  val default: Parent =
    Parent(Bounds.zero, Coords.zero)