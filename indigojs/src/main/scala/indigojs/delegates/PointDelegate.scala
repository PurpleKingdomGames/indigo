package indigojs.delegates

import indigo.shared.datatypes.Point

final class PointDelegate(val x: Int, val y: Int) {
  def toInternal: Point =
    Point(x, y)
}
