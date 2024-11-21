package indigo.shared.materials

import indigo.shared.datatypes.Rectangle

enum FillType derives CanEqual:
  case Normal
  case Stretch
  case Tile
  case NineSlice(center: Rectangle)

object FillType:
  object NineSlice:
    def apply(top: Int, right: Int, bottom: Int, left: Int): FillType =
      FillType.NineSlice(
        Rectangle(left, top, right - left, bottom - top)
      )
