package com.purplekingdomgames.indigoexts.quadtree

import com.purplekingdomgames.indigoexts.grid.GridPoint

trait QuadBounds {
  val x: Int
  val y: Int
  val width: Int
  val height: Int

  def left: Int   = x
  def top: Int    = y
  def right: Int  = x + width
  def bottom: Int = y + height

  def isOneUnitSquare: Boolean =
    width == 1 && height == 1

  def subdivide: (QuadBounds, QuadBounds, QuadBounds, QuadBounds) =
    QuadBounds.subdivide(this)

  def isPointWithinBounds(gridPoint: GridPoint): Boolean =
    QuadBounds.pointWithinBounds(this, gridPoint)

  def renderAsString: String =
    s"""($x, $y, $width, $height)"""

  def ===(other: QuadBounds): Boolean =
    QuadBounds.equals(this, other)

  override def toString: String =
    s"""QuadBounds($x, $y, $width, $height)"""

}

object QuadBounds {

  def apply(powerOf2: Int): QuadBounds =
    unsafeCreate(
      0,
      0,
      if (powerOf2 < 2) 2 else powerOf2,
      if (powerOf2 < 2) 2 else powerOf2
    )

  def apply(_x: Int, _y: Int, _width: Int, _height: Int): QuadBounds =
    unsafeCreate(
      if (_x < 0) 0 else _x,
      if (_y < 0) 0 else _y,
      if (_width < 2) 2 else _width,
      if (_height < 2) 2 else _height
    )

  def unsafeCreate(_x: Int, _y: Int, _width: Int, _height: Int): QuadBounds =
    new QuadBounds {
      val x: Int      = _x
      val y: Int      = _y
      val width: Int  = if (_width < 1) 1 else _width
      val height: Int = if (_height < 1) 1 else _height
    }

  def pointWithinBounds(quadBounds: QuadBounds, gridPoint: GridPoint): Boolean =
    gridPoint.x >= quadBounds.left &&
      gridPoint.y >= quadBounds.top &&
      gridPoint.x < quadBounds.right &&
      gridPoint.y < quadBounds.bottom

  def subdivide(quadBounds: QuadBounds): (QuadBounds, QuadBounds, QuadBounds, QuadBounds) =
    (
      unsafeCreate(quadBounds.x, quadBounds.y, quadBounds.width / 2, quadBounds.height / 2),
      unsafeCreate(quadBounds.x + (quadBounds.width / 2),
                   quadBounds.y,
                   quadBounds.width - (quadBounds.width / 2),
                   quadBounds.height / 2),
      unsafeCreate(quadBounds.x,
                   quadBounds.y + (quadBounds.height / 2),
                   quadBounds.width / 2,
                   quadBounds.height - (quadBounds.height / 2)),
      unsafeCreate(quadBounds.x + (quadBounds.width / 2),
                   quadBounds.y + (quadBounds.height / 2),
                   quadBounds.width - (quadBounds.width / 2),
                   quadBounds.height - (quadBounds.height / 2))
    )

  def combine(head: QuadBounds, tail: List[QuadBounds]): QuadBounds =
    tail.foldLeft(head)((a, b) => append(a, b))

  def append(a: QuadBounds, b: QuadBounds): QuadBounds = {
    val l = Math.min(a.left, b.left)
    val t = Math.min(a.top, b.top)
    val w = Math.max(a.right, b.right) - l
    val h = Math.max(a.bottom, b.bottom) - t

    unsafeCreate(l, t, w, h)
  }

  def equals(a: QuadBounds, b: QuadBounds): Boolean =
    a.x == b.x && a.y == b.y && a.width == b.width && a.height == b.height
}
