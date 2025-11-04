package indigoextras.ui.datatypes

import indigo.*

/** Represents a rectangle on the ui grid, rather than a rectangle on the screen.
  */
opaque type Bounds = Rectangle

object Bounds:

  inline def apply(r: Rectangle): Bounds                            = r
  inline def apply(x: Int, y: Int, width: Int, height: Int): Bounds = Rectangle(x, y, width, height)
  inline def apply(width: Int, height: Int): Bounds                 = Rectangle(0, 0, width, height)
  inline def apply(dimensions: Dimensions): Bounds                  = Rectangle(dimensions.toSize)
  inline def apply(coords: Coords, dimensions: Dimensions): Bounds =
    Rectangle(coords.toPoint, dimensions.toSize)

  val zero: Bounds = Bounds(0, 0, 0, 0)
  val one: Bounds  = Bounds(0, 0, 1, 1)

  extension (r: Bounds)
    inline def unsafeToRectangle: Rectangle = r
    inline def coords: Coords               = Coords(r.position)
    inline def dimensions: Dimensions       = Dimensions(r.size)
    inline def toScreenSpace(charSize: Size): Rectangle =
      Rectangle(r.position * charSize.toPoint, r.size * charSize)

    inline def x: Int      = r.x
    inline def y: Int      = r.y
    inline def width: Int  = r.width
    inline def height: Int = r.height

    inline def left: Int   = if width >= 0 then x else x + width
    inline def right: Int  = if width >= 0 then x + width else x
    inline def top: Int    = if height >= 0 then y else y + height
    inline def bottom: Int = if height >= 0 then y + height else y

    inline def horizontalCenter: Int = x + (width / 2)
    inline def verticalCenter: Int   = y + (height / 2)

    inline def topLeft: Coords      = Coords(left, top)
    inline def topRight: Coords     = Coords(right, top)
    inline def bottomRight: Coords  = Coords(right, bottom)
    inline def bottomLeft: Coords   = Coords(left, bottom)
    inline def center: Coords       = Coords(horizontalCenter, verticalCenter)
    inline def halfSize: Dimensions = (dimensions / 2).abs

    def +(other: Bounds): Bounds =
      Bounds(x + other.x, y + other.y, width + other.width, height + other.height)
    def -(other: Bounds): Bounds =
      Bounds(x - other.x, y - other.y, width - other.width, height - other.height)
    def *(other: Bounds): Bounds =
      Bounds(x * other.x, y * other.y, width * other.width, height * other.height)
    def /(other: Bounds): Bounds =
      Bounds(x / other.x, y / other.y, width / other.width, height / other.height)

    def contains(coords: Coords): Boolean =
      r.contains(coords.unsafeToPoint)
    def contains(x: Int, y: Int): Boolean =
      contains(Coords(x, y))

    def moveBy(coords: Coords): Bounds =
      r.moveBy(coords.toPoint)
    def moveBy(x: Int, y: Int): Bounds =
      moveBy(Point(x, y))

    def moveTo(coords: Coords): Bounds =
      r.moveTo(coords.toPoint)
    def moveTo(x: Int, y: Int): Bounds =
      moveTo(Point(x, y))

    def resize(newSize: Dimensions): Bounds =
      r.resize(newSize.toSize)
    def resize(x: Int, y: Int): Bounds =
      resize(Size(x, y))

    def resizeBy(amount: Dimensions): Bounds =
      r.resizeBy(amount.toSize)
    def resizeBy(x: Int, y: Int): Bounds =
      resizeBy(Size(x, y))

    def withPosition(coords: Coords): Bounds =
      moveTo(coords.toPoint)
    def withPosition(x: Int, y: Int): Bounds =
      moveTo(Point(x, y))

    def withDimensions(newSize: Dimensions): Bounds =
      resize(newSize)
    def withDimensions(x: Int, y: Int): Bounds =
      resize(Size(x, y))

    def withWidth(newWidth: Int): Bounds =
      resize(Size(newWidth, height))
    def withHeight(newHeight: Int): Bounds =
      resize(Size(width, newHeight))

    def expandToInclude(other: Bounds): Bounds =
      Bounds(Rectangle.expandToInclude(r, other))
