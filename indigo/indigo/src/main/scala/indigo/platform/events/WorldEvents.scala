package indigo.platform.events

import indigo.shared.collections.Batch
import indigo.shared.config.ResizePolicy
import indigo.shared.constants.Key
import indigo.shared.constants.KeyCode
import indigo.shared.constants.KeyLocation
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Size
import indigo.shared.events.ApplicationGainedFocus
import indigo.shared.events.ApplicationLostFocus
import indigo.shared.events.CanvasGainedFocus
import indigo.shared.events.CanvasLostFocus
import indigo.shared.events.FingerId
import indigo.shared.events.KeyboardEvent
import indigo.shared.events.MouseButton
import indigo.shared.events.MouseEvent
import indigo.shared.events.NetworkEvent
import indigo.shared.events.PenEvent
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerEvent.*
import indigo.shared.events.PointerId
import indigo.shared.events.PointerType
import indigo.shared.events.TouchEvent
import indigo.shared.events.WheelEvent
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.window

import scala.annotation.nowarn
import scala.scalajs.js.Date

final class WorldEvents:
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var pointerButtons: Map[Double, Batch[(Int, Date)]] = Map.empty

  def absoluteCoordsX(relativeX: Double): Int = {
    val offset: Double =
      if (window.pageXOffset > 0) window.pageXOffset
      else if (document.documentElement.scrollLeft > 0) document.documentElement.scrollLeft
      else if (document.body.scrollLeft > 0) document.body.scrollLeft
      else 0

    (relativeX - offset).toInt
  }

  def absoluteCoordsY(relativeY: Double): Int = {
    val offset: Double =
      if (window.pageYOffset > 0) window.pageYOffset
      else if (document.documentElement.scrollTop > 0) document.documentElement.scrollTop
      else if (document.body.scrollTop > 0) document.body.scrollTop
      else 0

    (relativeY - offset).toInt
  }

  final case class Handlers(
      canvas: html.Canvas,
      resizePolicy: ResizePolicy,
      onWheel: dom.WheelEvent => Unit,
      onKeyDown: dom.KeyboardEvent => Unit,
      onKeyUp: dom.KeyboardEvent => Unit,
      onContextMenu: Option[dom.MouseEvent => Unit],
      onPointerEnter: dom.PointerEvent => Unit,
      onPointerLeave: dom.PointerEvent => Unit,
      onPointerDown: dom.PointerEvent => Unit,
      onPointerUp: dom.PointerEvent => Unit,
      onPointerMove: dom.PointerEvent => Unit,
      onPointerCancel: dom.PointerEvent => Unit,
      onBlur: dom.FocusEvent => Unit,
      onFocus: dom.FocusEvent => Unit,
      onOnline: dom.Event => Unit,
      onOffline: dom.Event => Unit,
      resizeObserver: dom.ResizeObserver,
      clickTimeMs: Long
  ) {
    canvas.addEventListener("wheel", onWheel)
    canvas.addEventListener("pointerenter", onPointerEnter)
    canvas.addEventListener("pointerleave", onPointerLeave)
    canvas.addEventListener("pointerdown", onPointerDown)
    canvas.addEventListener("pointerup", onPointerUp)
    canvas.addEventListener("pointermove", onPointerMove)
    canvas.addEventListener("pointercancel", onPointerCancel)
    canvas.addEventListener("focus", onFocus)
    canvas.addEventListener("blur", onBlur)
    window.addEventListener("focus", onFocus)
    window.addEventListener("blur", onBlur)
    onContextMenu.foreach(canvas.addEventListener("contextmenu", _))
    document.addEventListener("keydown", onKeyDown)
    document.addEventListener("keyup", onKeyUp)
    window.addEventListener("online", onOnline)
    window.addEventListener("offline", onOffline)
    resizeObserver.observe(canvas.parentElement)

    def unbind(): Unit = {
      canvas.removeEventListener("wheel", onWheel)
      canvas.removeEventListener("pointerenter", onPointerEnter)
      canvas.removeEventListener("pointerleave", onPointerLeave)
      canvas.removeEventListener("pointerdown", onPointerDown)
      canvas.removeEventListener("pointerup", onPointerUp)
      canvas.removeEventListener("pointermove", onPointerMove)
      canvas.removeEventListener("pointercancel", onPointerCancel)
      canvas.removeEventListener("focus", onFocus)
      canvas.removeEventListener("blur", onBlur)
      window.removeEventListener("focus", onFocus)
      window.removeEventListener("blur", onBlur)
      onContextMenu.foreach(canvas.removeEventListener("contextmenu", _))
      document.removeEventListener("keydown", onKeyDown)
      document.removeEventListener("keyup", onKeyUp)
      window.removeEventListener("online", onOnline)
      window.removeEventListener("offline", onOffline)
      resizeObserver.disconnect()
      pointerButtons = Map.empty
    }
  }

  object Handlers {
    def apply(
        canvas: html.Canvas,
        resizePolicy: ResizePolicy,
        magnification: Int,
        disableContextMenu: Boolean,
        globalEventStream: GlobalEventStream,
        clickTimeMs: Long
    ): Handlers = Handlers(
      canvas = canvas,
      resizePolicy,
      /*
          Follows the most conventional, basic definition of wheel.
          To be fair, the wheel event doesn't necessarily mean that the device is a mouse, or even that the
          deltaY represents the direction of the vertical scrolling (usually negative is upwards and positive downwards).
          For the sake of simplicity, we're assuming a common mouse with a simple wheel.

          More info: https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent
       */
      onWheel = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)

        @nowarn("msg=deprecated")
        val wheel = MouseEvent.Wheel(
          position,
          buttons,
          e.altKey,
          e.ctrlKey,
          e.metaKey,
          e.shiftKey,
          movementPosition,
          e.deltaX,
          e.deltaY,
          e.deltaZ
        )

        globalEventStream.pushGlobalEvent(wheel)

        val deltaMode =
          e.deltaMode match {
            case dom.WheelEvent.DOM_DELTA_PIXEL => WheelEvent.DeltaMode.Pixel
            case dom.WheelEvent.DOM_DELTA_LINE  => WheelEvent.DeltaMode.Line
            case dom.WheelEvent.DOM_DELTA_PAGE  => WheelEvent.DeltaMode.Page
            case _                              => WheelEvent.DeltaMode.Page
          }
        val newWheel = WheelEvent.Move(
          e.deltaX,
          e.deltaY,
          e.deltaZ,
          deltaMode
        )
        globalEventStream.pushGlobalEvent(newWheel)

        if (e.deltaX != 0)
          globalEventStream.pushGlobalEvent(WheelEvent.Horizontal(e.deltaX, deltaMode))

        if (e.deltaY != 0)
          globalEventStream.pushGlobalEvent(WheelEvent.Vertical(e.deltaY, deltaMode))

        if (e.deltaZ != 0)
          globalEventStream.pushGlobalEvent(WheelEvent.Depth(e.deltaZ, deltaMode))
      },
      onKeyDown = { e =>
        globalEventStream.pushGlobalEvent(
          KeyboardEvent.KeyDown(
            Key(
              KeyCode.fromString(e.code),
              e.key,
              KeyLocation.fromInt(e.location)
            ),
            e.repeat,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey
          )
        )
      },
      onKeyUp = { e =>
        globalEventStream.pushGlobalEvent(
          KeyboardEvent.KeyUp(
            Key(
              KeyCode.fromString(e.code),
              e.key,
              KeyLocation.fromInt(e.location)
            ),
            e.repeat,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey
          )
        )
      },
      // Prevent right mouse button from popping up the context menu
      onContextMenu = if disableContextMenu then Some((e: dom.MouseEvent) => e.preventDefault()) else None,
      onPointerEnter = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          Enter(
            PointerId(e.pointerId),
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            e.width(magnification),
            e.height(magnification),
            e.pressure,
            e.tangentialPressure,
            Radians.fromDegrees(e.tiltX),
            Radians.fromDegrees(e.tiltY),
            Radians.fromDegrees(e.twist),
            pointerType,
            e.isPrimary
          )
        )

        pointerType match {
          case PointerType.Mouse =>
            @nowarn("msg=deprecated")
            val enterEvent = MouseEvent.Enter(
              PointerId(e.pointerId),
              position,
              buttons,
              e.altKey,
              e.ctrlKey,
              e.metaKey,
              e.shiftKey,
              movementPosition
            )

            globalEventStream.pushGlobalEvent(enterEvent)
          case PointerType.Touch =>
            globalEventStream.pushGlobalEvent(
              TouchEvent.Enter(
                PointerId(e.pointerId),
                FingerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )
          case PointerType.Pen =>
            globalEventStream.pushGlobalEvent(
              PenEvent.Enter(
                PointerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )
          case PointerType.Unknown => ()
        }
      },
      onPointerLeave = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          Leave(
            PointerId(e.pointerId),
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            e.width(magnification),
            e.height(magnification),
            e.pressure,
            e.tangentialPressure,
            Radians.fromDegrees(e.tiltX),
            Radians.fromDegrees(e.tiltY),
            Radians.fromDegrees(e.twist),
            pointerType,
            e.isPrimary
          )
        )

        @nowarn("msg=deprecated")
        val outEvent = Out(
          PointerId(e.pointerId),
          position,
          buttons,
          e.altKey,
          e.ctrlKey,
          e.metaKey,
          e.shiftKey,
          movementPosition,
          e.width(magnification),
          e.height(magnification),
          e.pressure,
          e.tangentialPressure,
          Radians.fromDegrees(e.tiltX),
          Radians.fromDegrees(e.tiltY),
          Radians.fromDegrees(e.twist),
          pointerType,
          e.isPrimary
        )

        globalEventStream.pushGlobalEvent(outEvent)

        pointerType match {
          case PointerType.Mouse =>
            @nowarn("msg=deprecated")
            val leaveEvent =
              MouseEvent.Leave(
                PointerId(e.pointerId),
                position,
                buttons,
                e.altKey,
                e.ctrlKey,
                e.metaKey,
                e.shiftKey,
                movementPosition
              )

            globalEventStream.pushGlobalEvent(leaveEvent)
          case PointerType.Touch =>
            globalEventStream.pushGlobalEvent(
              TouchEvent.Leave(
                PointerId(e.pointerId),
                FingerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )
          case PointerType.Pen =>
            globalEventStream.pushGlobalEvent(
              PenEvent.Leave(
                PointerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )
          case PointerType.Unknown => ()
        }
      },
      onPointerDown = { e =>
        val position         = e.position(magnification, canvas)
        val pointerType      = e.toPointerType
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)

        // A pen being touched to a touchpad, or a finger touching a screen both result in a left button being registered
        // This is misleading, and so here we reduce the button count by 1 to remove the left button. This also
        // has the result of making what was the middle button on a pen to a left button, and a right button to a middle buton
        val button = if (pointerType == PointerType.Mouse) e.button else e.button - 1

        // Add the button to the list of buttons that are down, to check later when the button is released
        pointerButtons = pointerButtons.updated(
          e.pointerId,
          pointerButtons
            .getOrElse(e.pointerId, Batch.empty) :+ (button -> new Date(Date.now()))
        )

        globalEventStream.pushGlobalEvent(
          Down(
            PointerId(e.pointerId),
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            e.width(magnification),
            e.height(magnification),
            e.pressure,
            e.tangentialPressure,
            Radians.fromDegrees(e.tiltX),
            Radians.fromDegrees(e.tiltY),
            Radians.fromDegrees(e.twist),
            pointerType,
            e.isPrimary,
            MouseButton.fromOrdinalOpt(button)
          )
        )

        pointerType match {
          case PointerType.Mouse =>
            MouseButton.fromOrdinalOpt(button).foreach { button =>
              @nowarn("msg=deprecated")
              val event =
                MouseEvent.MouseDown(
                  PointerId(e.pointerId),
                  position,
                  buttons,
                  e.altKey,
                  e.ctrlKey,
                  e.metaKey,
                  e.shiftKey,
                  movementPosition,
                  button
                )

              globalEventStream.pushGlobalEvent(event)
              globalEventStream.pushGlobalEvent(
                MouseEvent.Down(
                  PointerId(e.pointerId),
                  position,
                  movementPosition,
                  button
                )
              )
            }

          case PointerType.Touch =>
            globalEventStream.pushGlobalEvent(
              TouchEvent.Down(
                PointerId(e.pointerId),
                FingerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )

          case PointerType.Pen =>
            globalEventStream.pushGlobalEvent(
              PenEvent.Down(
                PointerId(e.pointerId),
                position,
                movementPosition,
                e.pressure,
                MouseButton.fromOrdinalOpt(button)
              )
            )

          case PointerType.Unknown => ()
        }
        e.preventDefault()
      },
      onPointerUp = { e =>
        @nowarn("msg=deprecated")
        val position         = e.position(magnification, canvas)
        val pointerType      = e.toPointerType
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)

        // A pen being touched to a touchpad, or a finger touching a screen both result in a left button being registered
        // This is misleading, and so here we reduce the button count by 1 to remove the left button. This also
        // has the result of making what was the middle button on a pen to a left button, and a right button to a middle buton
        val button = if (pointerType == PointerType.Mouse) e.button else e.button - 1

        // Check to see if this button is up within the clickTimeMs, and if so fire a click event
        pointerButtons.getOrElse(e.pointerId, Batch.empty).find(_._1 == button) match {
          case Some((_, downTime)) if Date.now() - downTime.getTime() <= clickTimeMs =>
            val btn = MouseButton.fromOrdinalOpt(button)
            globalEventStream.pushGlobalEvent(
              Click(
                PointerId(e.pointerId),
                position,
                buttons,
                e.altKey,
                e.ctrlKey,
                e.metaKey,
                e.shiftKey,
                movementPosition,
                e.width(magnification),
                e.height(magnification),
                e.pressure,
                e.tangentialPressure,
                Radians.fromDegrees(e.tiltX),
                Radians.fromDegrees(e.tiltY),
                Radians.fromDegrees(e.twist),
                pointerType,
                e.isPrimary,
                btn
              )
            )

            pointerType match {
              case PointerType.Mouse if btn.isDefined =>
                globalEventStream.pushGlobalEvent(
                  MouseEvent.Click(
                    PointerId(e.pointerId),
                    position,
                    buttons,
                    e.altKey,
                    e.ctrlKey,
                    e.metaKey,
                    e.shiftKey,
                    movementPosition,
                    btn.get
                  )
                )

              case PointerType.Touch =>
                globalEventStream.pushGlobalEvent(
                  TouchEvent.Tap(
                    PointerId(e.pointerId),
                    FingerId(e.pointerId.toInt),
                    position,
                    movementPosition,
                    e.pressure
                  )
                )

              case PointerType.Pen =>
                globalEventStream.pushGlobalEvent(
                  PenEvent.Click(
                    PointerId(e.pointerId),
                    position,
                    movementPosition,
                    e.pressure,
                    btn
                  )
                )

              case (PointerType.Unknown | PointerType.Mouse) => ()
            }
          case _ => ()
        }

        // Remove the button from the list of buttons that are down
        pointerButtons = pointerButtons.updated(
          e.pointerId,
          pointerButtons
            .getOrElse(e.pointerId, Batch.empty)
            .filterNot(_._1 == button)
        )

        globalEventStream.pushGlobalEvent(
          Up(
            PointerId(e.pointerId),
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            e.width(magnification),
            e.height(magnification),
            e.pressure,
            e.tangentialPressure,
            Radians.fromDegrees(e.tiltX),
            Radians.fromDegrees(e.tiltY),
            Radians.fromDegrees(e.twist),
            pointerType,
            e.isPrimary,
            MouseButton.fromOrdinalOpt(button)
          )
        )

        pointerType match {
          case PointerType.Mouse =>
            MouseButton.fromOrdinalOpt(e.button).foreach { button =>
              @nowarn("msg=deprecated")
              val event =
                MouseEvent.MouseUp(
                  PointerId(e.pointerId),
                  position,
                  buttons,
                  e.altKey,
                  e.ctrlKey,
                  e.metaKey,
                  e.shiftKey,
                  movementPosition,
                  button
                )

              globalEventStream.pushGlobalEvent(event)
              globalEventStream.pushGlobalEvent(
                MouseEvent.Up(
                  PointerId(e.pointerId),
                  position,
                  movementPosition,
                  button
                )
              )

            }

          case PointerType.Touch =>
            globalEventStream.pushGlobalEvent(
              TouchEvent.Up(
                PointerId(e.pointerId),
                FingerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )

          case PointerType.Pen =>
            globalEventStream.pushGlobalEvent(
              PenEvent.Up(
                PointerId(e.pointerId),
                position,
                movementPosition,
                e.pressure,
                MouseButton.fromOrdinalOpt(button)
              )
            )

          case PointerType.Unknown => ()
        }
        e.preventDefault()
      },
      onPointerMove = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          PointerEvent.Move(
            PointerId(e.pointerId),
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            e.width(magnification),
            e.height(magnification),
            e.pressure,
            e.tangentialPressure,
            Radians.fromDegrees(e.tiltX),
            Radians.fromDegrees(e.tiltY),
            Radians.fromDegrees(e.twist),
            pointerType,
            e.isPrimary
          )
        )

        pointerType match {
          case PointerType.Mouse =>
            @nowarn("msg=deprecated")
            val event =
              MouseEvent.Move(
                PointerId(e.pointerId),
                position,
                buttons,
                e.altKey,
                e.ctrlKey,
                e.metaKey,
                e.shiftKey,
                movementPosition
              )

            globalEventStream.pushGlobalEvent(event)

          case PointerType.Touch =>
            globalEventStream.pushGlobalEvent(
              TouchEvent.Move(
                PointerId(e.pointerId),
                FingerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )

          case PointerType.Pen =>
            globalEventStream.pushGlobalEvent(
              PenEvent.Move(
                PointerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )

          case PointerType.Unknown => ()
        }
        e.preventDefault()
      },
      onPointerCancel = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          Cancel(
            PointerId(e.pointerId),
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            e.width(magnification),
            e.height(magnification),
            e.pressure,
            e.tangentialPressure,
            Radians.fromDegrees(e.tiltX),
            Radians.fromDegrees(e.tiltY),
            Radians.fromDegrees(e.twist),
            pointerType,
            e.isPrimary
          )
        )

        pointerType match {
          case PointerType.Mouse =>
            globalEventStream.pushGlobalEvent(
              MouseEvent.Cancel(
                PointerId(e.pointerId),
                position,
                movementPosition
              )
            )

          case PointerType.Touch =>
            globalEventStream.pushGlobalEvent(
              TouchEvent.Cancel(
                PointerId(e.pointerId),
                FingerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )

          case PointerType.Pen =>
            globalEventStream.pushGlobalEvent(
              PenEvent.Cancel(
                PointerId(e.pointerId),
                position,
                movementPosition,
                e.pressure
              )
            )

          case PointerType.Unknown => ()
        }
        e.preventDefault()
      },
      onFocus = { e =>
        globalEventStream.pushGlobalEvent(
          if e.isWindowTarget then ApplicationGainedFocus
          else CanvasGainedFocus
        )
      },
      onBlur = { e =>
        globalEventStream.pushGlobalEvent(
          if e.isWindowTarget then ApplicationLostFocus
          else CanvasLostFocus
        )
      },
      onOnline = { e =>
        globalEventStream.pushGlobalEvent(NetworkEvent.Online)
      },
      onOffline = { e =>
        globalEventStream.pushGlobalEvent(NetworkEvent.Offline)
      },
      resizeObserver = new dom.ResizeObserver((entries, _) =>
        entries.foreach { entry =>
          entry.target.childNodes.foreach { child =>
            child match {
              case child: dom.Element
                  if child.attributes.getNamedItem("id").value == canvas.attributes.getNamedItem("id").value =>
                val containerSize = new Size(
                  Math.floor(entry.contentRect.width).toInt,
                  Math.floor(entry.contentRect.height).toInt
                )
                val canvasSize = new Size(canvas.width, canvas.height)
                if resizePolicy != ResizePolicy.NoResize then
                  val newSize = resizePolicy match {
                    case ResizePolicy.Resize => containerSize
                    case ResizePolicy.ResizePreserveAspect =>
                      val width       = canvas.width.toDouble
                      val height      = canvas.height.toDouble
                      val aspectRatio = Math.min(width, height) / Math.max(width, height)

                      if width > height then
                        val newHeight = containerSize.width.toDouble * aspectRatio
                        if newHeight > containerSize.height then
                          Size(
                            (containerSize.height / aspectRatio).toInt,
                            containerSize.height
                          )
                        else
                          Size(
                            containerSize.width,
                            newHeight.toInt
                          )
                      else
                        val newWidth = containerSize.height.toDouble * aspectRatio
                        if newWidth > containerSize.width then
                          Size(
                            containerSize.width,
                            (containerSize.width / aspectRatio).toInt
                          )
                        else
                          Size(
                            newWidth.toInt,
                            containerSize.height
                          )
                    case _ => canvasSize
                  }

                  if (newSize != canvasSize) {
                    canvas.width = Math.min(newSize.width, containerSize.width)
                    canvas.height = Math.min(newSize.height, containerSize.height)
                  }
            }
          }
        }
      ),
      clickTimeMs
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _handlers: Option[Handlers] = None

  def init(
      canvas: html.Canvas,
      resizePolicy: ResizePolicy,
      magnification: Int,
      disableContextMenu: Boolean,
      globalEventStream: GlobalEventStream,
      clickTimeMs: Long
  ): Unit =
    if (_handlers.isEmpty)
      _handlers = Some(
        Handlers(canvas, resizePolicy, magnification, disableContextMenu, globalEventStream, clickTimeMs)
      )

  def kill(): Unit = _handlers.foreach { x =>
    x.unbind()
    _handlers = None
  }

  extension (e: dom.FocusEvent)
    def isWindowTarget: Boolean =
      val target = e.target
      target match {
        case e: dom.Element if e.tagName == "WINDOW" => true
        case _                                       => false
      }

  extension (e: dom.MouseEvent)
    /** @return
      *   position relative to magnification level
      */
    def position(magnification: Int, canvas: html.Canvas): Point =
      val rect = canvas.getBoundingClientRect()

      Point(
        absoluteCoordsX(e.pageX.toInt - rect.left.toInt) / magnification,
        absoluteCoordsY(e.pageY.toInt - rect.top.toInt) / magnification
      )

    def movementPosition(magnification: Int): Point =
      Point(
        (e.movementX / magnification).toInt,
        (e.movementY / magnification).toInt
      )

    /** The property indicates which buttons are pressed on the mouse (or other input device) when a mouse event is
      * triggered.
      */
    def indigoButtons =
      WorldEvents.buttonsFromInt(e.buttons)

  extension (e: dom.PointerEvent)
    def width(magnification: Int): Int =
      (e.width / magnification).toInt

    def height(magnification: Int): Int =
      (e.height / magnification).toInt

    def toPointerType =
      e.pointerType match {
        case "mouse" => PointerType.Mouse
        case "pen"   => PointerType.Pen
        case "touch" => PointerType.Touch
        case _       => PointerType.Unknown
      }

end WorldEvents

object WorldEvents:

  /** Work out which buttons are pressed on the mouse (or other input device) based on the `buttons` field from a
    * `dom.MouseEvent`.
    */
  def buttonsFromInt(buttons: Int): Batch[MouseButton] =
    Batch.fromArray(
      (0 to 5)
        .filter(i => ((buttons >> i) & 1) == 1)
        .flatMap(MouseButton.fromOrdinalOpt)
        .toArray
    )
