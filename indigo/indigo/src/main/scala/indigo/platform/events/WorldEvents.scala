package indigo.platform.events

import indigo.shared.collections.Batch
import indigo.shared.config.ResizePolicy
import indigo.shared.constants.Key
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Size
import indigo.shared.events.ApplicationGainedFocus
import indigo.shared.events.ApplicationLostFocus
import indigo.shared.events.CanvasGainedFocus
import indigo.shared.events.CanvasLostFocus
import indigo.shared.events.KeyboardEvent
import indigo.shared.events.MouseButton
import indigo.shared.events.MouseEvent
import indigo.shared.events.NetworkEvent
import indigo.shared.events.NetworkEvent.*
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerEvent.*
import indigo.shared.events.PointerType
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.window

final class WorldEvents:

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
      onClick: dom.MouseEvent => Unit,
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
      resizeObserver: dom.ResizeObserver
  ) {
    canvas.addEventListener("click", onClick)
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
      canvas.removeEventListener("click", onClick)
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
    }
  }

  object Handlers {
    def apply(
        canvas: html.Canvas,
        resizePolicy: ResizePolicy,
        magnification: Int,
        disableContextMenu: Boolean,
        globalEventStream: GlobalEventStream
    ): Handlers = Handlers(
      canvas = canvas,
      resizePolicy,
      // onClick only supports the left mouse button
      onClick = { e =>
        MouseButton.fromOrdinalOpt(e.button).foreach { button =>
          val position         = e.position(magnification, canvas)
          val buttons          = e.indigoButtons
          val movementPosition = e.movementPosition(magnification)
          globalEventStream.pushGlobalEvent(
            MouseEvent.Click(
              position,
              buttons,
              e.altKey,
              e.ctrlKey,
              e.metaKey,
              e.shiftKey,
              movementPosition,
              button
            )
          )
        }
      },
      /*
          Follows the most conventional, basic definition of wheel.
          To be fair, the wheel event doesn't necessarily means that the device is a mouse, or even that the
          deltaY represents the direction of the vertical scrolling (usually negative is upwards and positive downwards).
          For the sake of simplicity, we're assuming a common mouse with a simple wheel.

          More info: https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent
       */
      onWheel = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val wheel = MouseEvent.Wheel(
          position,
          buttons,
          e.altKey,
          e.ctrlKey,
          e.metaKey,
          e.shiftKey,
          movementPosition,
          e.deltaY
        )

        globalEventStream.pushGlobalEvent(wheel)
      },
      onKeyDown = { e =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyDown(Key(e.keyCode, e.key)))
      },
      onKeyUp = { e =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyUp(Key(e.keyCode, e.key)))
      },
      // Prevent right mouse button from popping up the context menu
      onContextMenu = if disableContextMenu then Some((e: dom.MouseEvent) => e.preventDefault()) else None,
      onPointerEnter = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          PointerEnter(
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            PointerId(e.pointerId),
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

        if pointerType == PointerType.Mouse then {
          globalEventStream.pushGlobalEvent(
            MouseEvent.Enter(
              position,
              buttons,
              e.altKey,
              e.ctrlKey,
              e.metaKey,
              e.shiftKey,
              movementPosition
            )
          )
        }
      },
      onPointerLeave = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          PointerLeave(
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            PointerId(e.pointerId),
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

        if pointerType == PointerType.Mouse then {
          globalEventStream.pushGlobalEvent(
            MouseEvent.Leave(
              position,
              buttons,
              e.altKey,
              e.ctrlKey,
              e.metaKey,
              e.shiftKey,
              movementPosition
            )
          )
        }
      },
      onPointerDown = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          PointerDown(
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            PointerId(e.pointerId),
            e.width(magnification),
            e.height(magnification),
            e.pressure,
            e.tangentialPressure,
            Radians.fromDegrees(e.tiltX),
            Radians.fromDegrees(e.tiltY),
            Radians.fromDegrees(e.twist),
            pointerType,
            e.isPrimary,
            MouseButton.fromOrdinalOpt(e.button)
          )
        )

        if pointerType == PointerType.Mouse then {
          MouseButton.fromOrdinalOpt(e.button).foreach { button =>
            globalEventStream.pushGlobalEvent(
              MouseEvent.MouseDown(
                position,
                buttons,
                e.altKey,
                e.ctrlKey,
                e.metaKey,
                e.shiftKey,
                movementPosition,
                button
              )
            )
          }
        }
        e.preventDefault()
      },
      onPointerUp = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          PointerUp(
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            PointerId(e.pointerId),
            e.width(magnification),
            e.height(magnification),
            e.pressure,
            e.tangentialPressure,
            Radians.fromDegrees(e.tiltX),
            Radians.fromDegrees(e.tiltY),
            Radians.fromDegrees(e.twist),
            pointerType,
            e.isPrimary,
            MouseButton.fromOrdinalOpt(e.button)
          )
        )

        if pointerType == PointerType.Mouse then {
          MouseButton.fromOrdinalOpt(e.button).foreach { button =>
            globalEventStream.pushGlobalEvent(
              MouseEvent.MouseUp(
                position,
                buttons,
                e.altKey,
                e.ctrlKey,
                e.metaKey,
                e.shiftKey,
                movementPosition,
                button
              )
            )
          }
        }
        e.preventDefault()
      },
      onPointerMove = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          PointerMove(
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            PointerId(e.pointerId),
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

        if pointerType == PointerType.Mouse then {
          globalEventStream.pushGlobalEvent(
            MouseEvent.Move(
              position,
              buttons,
              e.altKey,
              e.ctrlKey,
              e.metaKey,
              e.shiftKey,
              movementPosition
            )
          )
        }
        e.preventDefault()
      },
      onPointerCancel = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)
        val pointerType      = e.toPointerType

        globalEventStream.pushGlobalEvent(
          PointerCancel(
            position,
            buttons,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey,
            movementPosition,
            PointerId(e.pointerId),
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
            if child.attributes.getNamedItem("id").value == canvas.attributes.getNamedItem("id").value then
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
      )
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _handlers: Option[Handlers] = None

  def init(
      canvas: html.Canvas,
      resizePolicy: ResizePolicy,
      magnification: Int,
      disableContextMenu: Boolean,
      globalEventStream: GlobalEventStream
  ): Unit =
    if (_handlers.isEmpty)
      _handlers = Some(Handlers(canvas, resizePolicy, magnification, disableContextMenu, globalEventStream))

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

    def indigoButtons =
      Batch.fromArray(
        (0 to 5)
          .filter(i => ((e.buttons >> i) & 1) == 1)
          .flatMap(MouseButton.fromOrdinalOpt)
          .toArray
      )

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
