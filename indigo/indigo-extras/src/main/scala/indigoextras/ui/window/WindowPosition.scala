package indigoextras.ui.window

import indigoextras.ui.components.datatypes.Anchor
import indigoextras.ui.datatypes.Coords

enum WindowPosition derives CanEqual:
  case Fixed(coords: Coords)
  case Anchored(anchor: Anchor)

  def isAnchored: Boolean =
    this match
      case Anchored(_) => true
      case Fixed(_)    => false

  def isFixed: Boolean =
    this match
      case Anchored(_) => false
      case Fixed(_)    => true
