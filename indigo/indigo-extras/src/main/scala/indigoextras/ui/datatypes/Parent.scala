package indigoextras.ui.datatypes

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size

final case class Parent(bounds: Bounds, additionalOffset: Coords):

  def toRectangle(snapGrid: Size): Rectangle =
    bounds.toScreenSpace(snapGrid)
  lazy val unsafeToRectangle: Rectangle =
    bounds.unsafeToRectangle

  lazy val coords: Coords =
    bounds.coords

  lazy val dimensions: Dimensions =
    bounds.dimensions

  def withBounds(newBounds: Bounds): Parent =
    this.copy(bounds = newBounds)

  def moveTo(newPosition: Coords): Parent =
    this.copy(bounds = bounds.moveTo(newPosition))
  def moveTo(x: Int, y: Int): Parent =
    this.copy(bounds = bounds.moveTo(x, y))
  def moveBy(offset: Coords): Parent =
    this.copy(bounds = bounds.moveBy(offset))
  def moveBy(x: Int, y: Int): Parent =
    this.copy(bounds = bounds.moveBy(x, y))

  def resize(newDimensions: Dimensions): Parent =
    this.copy(bounds = bounds.resize(newDimensions))
  def resize(width: Int, height: Int): Parent =
    this.copy(bounds = bounds.resize(width, height))
  def resizeBy(amount: Dimensions): Parent =
    this.copy(bounds = bounds.resizeBy(amount))
  def resizeBy(width: Int, height: Int): Parent =
    this.copy(bounds = bounds.resizeBy(width, height))

  def withAdditionalOffset(offset: Coords): Parent =
    this.copy(additionalOffset = offset)
  def withAdditionalOffset(x: Int, y: Int): Parent =
    this.copy(additionalOffset = Coords(x, y))

object Parent:

  val default: Parent =
    Parent(Bounds.zero, Coords.zero)
