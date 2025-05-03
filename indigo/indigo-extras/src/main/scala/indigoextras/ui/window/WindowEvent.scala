package indigoextras.ui.window

import indigo.*
import indigoextras.ui.components.datatypes.Anchor as Ankor
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions

enum WindowEvent extends GlobalEvent derives CanEqual:

  // Events sent to the game

  /** Informs the game when the pointer moves into a window's bounds */
  case PointerOver(id: WindowId)

  /** Informs the game when the pointer moves out of a window's bounds */
  case PointerOut(id: WindowId)

  /** Informs the game when a window has resized */
  case Resized(id: WindowId)

  /** Informs the game when a window has opened */
  case Opened(id: WindowId)

  /** Informs the game when a window has closed */
  case Closed(id: WindowId)

  // User sent events

  /** Tells a window to open */
  case Open(id: WindowId)

  /** Tells a window to open at a specific location */
  case OpenAt(id: WindowId, coords: Coords)

  /** Tells a window to close */
  case Close(id: WindowId)

  /** Closes whichever window is focused */
  case CloseFocused

  /** Tells a window to toggle between open and closed. */
  case Toggle(id: WindowId)

  /** Brings a window into focus */
  case Focus(id: WindowId)

  /** Focuses the top window at the given location */
  case GiveFocusAt(coords: Coords)

  /** Moves a window to the location given */
  case Move(id: WindowId, position: Coords, space: Space)

  /** Anchors a window on the screen */
  case Anchor(id: WindowId, anchor: Ankor)

  /** Resizes a window to a given size */
  case Resize(id: WindowId, dimensions: Dimensions, space: Space)

  /** Changes the bounds of a window */
  case Transform(id: WindowId, bounds: Bounds, space: Space)

  /** Changes the magnification of all windows */
  case ChangeMagnification(newMagnification: Int)

  /** Tells a window request its content to refresh */
  case Refresh(id: WindowId)

  def windowId: Option[WindowId] =
    this match
      case PointerOver(id)        => Some(id)
      case PointerOut(id)         => Some(id)
      case Resized(id)            => Some(id)
      case Opened(id)             => Some(id)
      case Closed(id)             => Some(id)
      case Open(id)               => Some(id)
      case OpenAt(id, _)          => Some(id)
      case Close(id)              => Some(id)
      case Toggle(id)             => Some(id)
      case Move(id, _, _)         => Some(id)
      case Anchor(id, _)          => Some(id)
      case Resize(id, _, _)       => Some(id)
      case Transform(id, _, _)    => Some(id)
      case Refresh(id)            => Some(id)
      case Focus(id)              => Some(id)
      case GiveFocusAt(_)         => None
      case ChangeMagnification(_) => None
      case CloseFocused           => None
