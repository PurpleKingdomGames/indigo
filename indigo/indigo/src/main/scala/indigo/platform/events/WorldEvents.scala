package indigo.platform.events

import indigo.shared.constants.Key
import indigo.shared.datatypes.Point
import indigo.shared.events.KeyboardEvent
import indigo.shared.events.MouseButton
import indigo.shared.events.MouseEvent
import indigo.shared.events.PointerEvent
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
      onClick: dom.MouseEvent => Unit,
      onWheel: dom.WheelEvent => Unit,
      onMouseMove: dom.MouseEvent => Unit,
      onMouseDown: dom.MouseEvent => Unit,
      onMouseUp: dom.MouseEvent => Unit,
      onKeyDown: dom.KeyboardEvent => Unit,
      onKeyUp: dom.KeyboardEvent => Unit,
      onContextMenu: Option[dom.MouseEvent => Unit],

      onPointerDown: dom.PointerEvent => Unit,
      onPointerUp: dom.PointerEvent => Unit,
      onPointerMove: dom.PointerEvent => Unit,
      onPointerCancel: dom.PointerEvent => Unit,
  ) {
    canvas.addEventListener("click", onClick)
    canvas.addEventListener("wheel", onWheel)
    canvas.addEventListener("mousemove", onMouseMove)
    canvas.addEventListener("mousedown", onMouseDown)
    canvas.addEventListener("mouseup", onMouseUp)

    canvas.addEventListener("pointerdown", onPointerDown)
    canvas.addEventListener("pointerup", onPointerUp)
    canvas.addEventListener("pointermove", onPointerMove)
    canvas.addEventListener("pointercancel", onPointerCancel)

    val tempHandler = { (e: dom.PointerEvent) =>
      dom.console.log(e.`type`)
    }

    canvas.addEventListener("pointerover", tempHandler)
    canvas.addEventListener("pointerenter", tempHandler)
    canvas.addEventListener("pointerout", tempHandler)
    canvas.addEventListener("pointerleave", tempHandler)
    canvas.addEventListener("gotpointercapture", tempHandler)
    canvas.addEventListener("lostpointercapture", tempHandler)

    onContextMenu.foreach(handler => canvas.addEventListener("contextmenu", handler))

    document.addEventListener("keydown", onKeyDown)
    document.addEventListener("keyup", onKeyUp)

    def unbind(): Unit = {
      canvas.removeEventListener("click", onClick)
      canvas.removeEventListener("wheel", onWheel)
      canvas.removeEventListener("mousemove", onMouseMove)
      canvas.removeEventListener("mousedown", onMouseDown)
      canvas.removeEventListener("mouseup", onMouseUp)
      onContextMenu.foreach(x => canvas.removeEventListener("contextmenu", x))
      document.removeEventListener("keydown", onKeyDown)
      document.removeEventListener("keyup", onKeyUp)
    }
  }

  object Handlers {
    def apply(
        canvas: html.Canvas,
        magnification: Int,
        disableContextMenu: Boolean,
        globalEventStream: GlobalEventStream
    ): Handlers = Handlers(
      canvas = canvas,
      // Onclick only supports the left mouse button
      onClick = { (e: dom.MouseEvent) =>
        globalEventStream.pushGlobalEvent(
          MouseEvent.Click(e.position(magnification, canvas))
        )
      },
      /*
          Follows the most conventional, basic definition of wheel.
          To be fair, the wheel event doesn't necessarily means that the device is a mouse, or even that the
          deltaY represents the direction of the vertical scrolling (usually negative is upwards and positive downwards).
          For the sake of simplicity, we're assuming a common mouse with a simple wheel.

          More info: https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent
       */
      onWheel = { (e: dom.WheelEvent) =>
        val wheel = MouseEvent.Wheel(
          e.position(magnification, canvas),
          e.deltaY
        )

        globalEventStream.pushGlobalEvent(wheel)
      },
      onMouseMove = { (e: dom.MouseEvent) =>
        globalEventStream.pushGlobalEvent(
          MouseEvent.Move(e.position(magnification, canvas))
        )
      },
      onMouseDown = { (e: dom.MouseEvent) =>
        MouseButton.fromOrdinalOpt(e.button).foreach { mouseButton =>
          globalEventStream.pushGlobalEvent(
            MouseEvent.MouseDown(
              e.position(magnification, canvas),
              mouseButton
            )
          )
        }
      },
      onMouseUp = { (e: dom.MouseEvent) =>
        MouseButton.fromOrdinalOpt(e.button).foreach { mouseButton =>
          globalEventStream.pushGlobalEvent(
            MouseEvent.MouseUp(
              e.position(magnification, canvas),
              mouseButton
            )
          )
        }
      },
      onKeyDown = { (e: dom.KeyboardEvent) =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyDown(Key(e.keyCode, e.key)))
      },
      onKeyUp = { (e: dom.KeyboardEvent) =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyUp(Key(e.keyCode, e.key)))
      },
      // Prevent right mouse button from popping up the context menu
      onContextMenu = if disableContextMenu then Some((e: dom.MouseEvent) => e.preventDefault()) else None,
      onPointerDown = { (e: dom.PointerEvent) =>
        globalEventStream.pushGlobalEvent(
          PointerEvent.PointerDown(e.position(magnification, canvas))
        )
        e.preventDefault()
      },
      onPointerUp = { (e: dom.PointerEvent) =>
        globalEventStream.pushGlobalEvent(
          PointerEvent.PointerUp(e.position(magnification, canvas))
        )
        e.preventDefault()
      },
      onPointerMove = { (e: dom.PointerEvent) =>
        globalEventStream.pushGlobalEvent(
          PointerEvent.PointerMove(e.position(magnification, canvas))
        )
        e.preventDefault()
      },
      onPointerCancel = { (e: dom.PointerEvent) =>
        globalEventStream.pushGlobalEvent(
          PointerEvent.PointerCancel(e.position(magnification, canvas))
        )
        e.preventDefault()
      }
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _handlers: Option[Handlers] = None

  def init(
      canvas: html.Canvas,
      magnification: Int,
      disableContextMenu: Boolean,
      globalEventStream: GlobalEventStream
  ): Unit =
    if (_handlers.isEmpty) _handlers = Some(Handlers(canvas, magnification, disableContextMenu, globalEventStream))

  def kill(): Unit = _handlers.foreach { x =>
    x.unbind()
    _handlers = None
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

end WorldEvents
