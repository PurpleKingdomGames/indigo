package indigo.platform.events

import indigo.shared.constants.Key
import indigo.shared.datatypes.Point
import indigo.shared.events.KeyboardEvent
import indigo.shared.events.MouseButton
import indigo.shared.events.MouseEvent
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
      onclick: dom.MouseEvent => Unit,
      onwheel: dom.WheelEvent => Unit,
      onmousemove: dom.MouseEvent => Unit,
      onmousedown: dom.MouseEvent => Unit,
      onmouseup: dom.MouseEvent => Unit,
      onkeydown: dom.KeyboardEvent => Unit,
      onkeyup: dom.KeyboardEvent => Unit,
      oncontextmenu: Option[dom.MouseEvent => Unit] = None
  ) {
    canvas.onclick = onclick
    canvas.onwheel = onwheel
    canvas.onmousemove = onmousemove
    canvas.onmousedown = onmousedown
    canvas.onmouseup = onmouseup
    oncontextmenu.foreach(x => canvas.oncontextmenu = x)
    document.onkeydown = onkeydown
    document.onkeyup = onkeyup

    def unbind(): Unit = {
      canvas.removeEventListener("click", onclick)
      canvas.removeEventListener("wheel", onwheel)
      canvas.removeEventListener("mousemove", onmousemove)
      canvas.removeEventListener("mousedown", onmousedown)
      canvas.removeEventListener("mouseup", onmouseup)
      oncontextmenu.foreach(x => canvas.removeEventListener("contextmenu", x))
      document.removeEventListener("keydown", onkeydown)
      document.removeEventListener("keyup", onkeyup)
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
      onclick = { (e: dom.MouseEvent) =>
        val rect = canvas.getBoundingClientRect()

        globalEventStream.pushGlobalEvent(
          MouseEvent.Click(
            absoluteCoordsX(e.pageX.toInt - rect.left.toInt) / magnification,
            absoluteCoordsY(e.pageY.toInt - rect.top.toInt) / magnification
          )
        )
      },
      /*
          Follows the most conventional, basic definition of wheel.
          To be fair, the wheel event doesn't necessarily means that the device is a mouse, or even that the
          deltaY represents the direction of the vertical scrolling (usually negative is upwards and positive downwards).
          For the sake of simplicity, we're assuming a common mouse with a simple wheel.

          More info: https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent
       */
      onwheel = { (e: dom.WheelEvent) =>
        val rect = canvas.getBoundingClientRect()
        val wheel = MouseEvent.Wheel(
          Point(
            absoluteCoordsX(e.pageX.toInt - rect.left.toInt) / magnification,
            absoluteCoordsY(e.pageY.toInt - rect.top.toInt) / magnification
          ),
          e.deltaY
        )

        globalEventStream.pushGlobalEvent(wheel)
      },
      onmousemove = { (e: dom.MouseEvent) =>
        val rect = canvas.getBoundingClientRect()

        globalEventStream.pushGlobalEvent(
          MouseEvent.Move(
            absoluteCoordsX(e.pageX.toInt - rect.left.toInt) / magnification,
            absoluteCoordsY(e.pageY.toInt - rect.top.toInt) / magnification
          )
        )
      },
      onmousedown = { (e: dom.MouseEvent) =>
        val rect = canvas.getBoundingClientRect()

        MouseButton.fromOrdinalOpt(e.button).foreach { mouseButton =>
          globalEventStream.pushGlobalEvent(
            MouseEvent.MouseDown(
              absoluteCoordsX(e.pageX.toInt - rect.left.toInt) / magnification,
              absoluteCoordsY(e.pageY.toInt - rect.top.toInt) / magnification,
              mouseButton
            )
          )
        }
      },
      onmouseup = { (e: dom.MouseEvent) =>
        val rect = canvas.getBoundingClientRect()

        MouseButton.fromOrdinalOpt(e.button).foreach { mouseButton =>
          globalEventStream.pushGlobalEvent(
            MouseEvent.MouseUp(
              absoluteCoordsX(e.pageX.toInt - rect.left.toInt) / magnification,
              absoluteCoordsY(e.pageY.toInt - rect.top.toInt) / magnification,
              mouseButton
            )
          )
        }
      },
      onkeydown = { (e: dom.KeyboardEvent) =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyDown(Key(e.keyCode, e.key)))
      },
      onkeyup = { (e: dom.KeyboardEvent) =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyUp(Key(e.keyCode, e.key)))
      },
      // Prevent right mouse button from popping up the context menu
      oncontextmenu = if (disableContextMenu) Some((e: dom.MouseEvent) => e.preventDefault()) else None
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

end WorldEvents
