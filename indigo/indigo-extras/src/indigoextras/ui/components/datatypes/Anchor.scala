package indigoextras.ui.components.datatypes

import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions

final case class Anchor(location: AnchorLocation, padding: Padding):

  def withPadding(padding: Padding): Anchor =
    this.copy(padding = padding)

  def withLocation(location: AnchorLocation): Anchor =
    this.copy(location = location)

  def calculatePosition(area: Dimensions, component: Dimensions): Coords =
    Anchor.calculatePosition(this, area, component)

object Anchor:

  val TopLeft: Anchor      = Anchor(AnchorLocation.TopLeft, Padding.zero)
  val TopCenter: Anchor    = Anchor(AnchorLocation.TopCenter, Padding.zero)
  val TopRight: Anchor     = Anchor(AnchorLocation.TopRight, Padding.zero)
  val CenterLeft: Anchor   = Anchor(AnchorLocation.CenterLeft, Padding.zero)
  val Center: Anchor       = Anchor(AnchorLocation.Center, Padding.zero)
  val CenterRight: Anchor  = Anchor(AnchorLocation.CenterRight, Padding.zero)
  val BottomLeft: Anchor   = Anchor(AnchorLocation.BottomLeft, Padding.zero)
  val BottomCenter: Anchor = Anchor(AnchorLocation.BottomCenter, Padding.zero)
  val BottomRight: Anchor  = Anchor(AnchorLocation.BottomRight, Padding.zero)

  def calculatePosition(anchor: Anchor, area: Dimensions, component: Dimensions): Coords =
    anchor.location match
      case AnchorLocation.TopLeft =>
        Coords(anchor.padding.left, anchor.padding.top)

      case AnchorLocation.TopCenter =>
        Coords((area.width - component.width) / 2, 0) +
          Coords(0, anchor.padding.top)

      case AnchorLocation.TopRight =>
        Coords(area.width - component.width, 0) +
          Coords(-anchor.padding.right, anchor.padding.top)

      case AnchorLocation.CenterLeft =>
        Coords(0, (area.height - component.height) / 2) +
          Coords(anchor.padding.left, 0)

      case AnchorLocation.Center =>
        Coords((area.width - component.width) / 2, (area.height - component.height) / 2)

      case AnchorLocation.CenterRight =>
        Coords(area.width - component.width, (area.height - component.height) / 2) +
          Coords(-anchor.padding.right, 0)

      case AnchorLocation.BottomLeft =>
        Coords(0, area.height - component.height) +
          Coords(anchor.padding.left, -anchor.padding.bottom)

      case AnchorLocation.BottomCenter =>
        Coords((area.width - component.width) / 2, area.height - component.height) +
          Coords(0, -anchor.padding.bottom)

      case AnchorLocation.BottomRight =>
        Coords(area.width - component.width, area.height - component.height) +
          Coords(-anchor.padding.right, -anchor.padding.bottom)

enum AnchorLocation derives CanEqual:
  case TopLeft
  case TopCenter
  case TopRight
  case CenterLeft
  case Center
  case CenterRight
  case BottomLeft
  case BottomCenter
  case BottomRight
