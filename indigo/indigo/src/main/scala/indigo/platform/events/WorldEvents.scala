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

object WorldEvents {

  def absoluteCoordsX(relativeX: Double): Int = {
    val offset: Double =
      if (window.pageXOffset > 0) window.pageXOffset
      else if (document.documentElement.scrollLeft > 0) document.documentElement.scrollLeft
      else if (document.body.scrollLeft > 0) document.body.scrollLeft
      else 0

    (relativeX + offset).toInt
  }

  def absoluteCoordsY(relativeY: Double): Int = {
    val offset: Double =
      if (window.pageYOffset > 0) window.pageYOffset
      else if (document.documentElement.scrollTop > 0) document.documentElement.scrollTop
      else if (document.body.scrollTop > 0) document.body.scrollTop
      else 0

    (relativeY + offset).toInt
  }

  def init(canvas: html.Canvas, magnification: Int, globalEventStream: GlobalEventStream): Unit = {
    // Onclick only supports the left mouse button
    canvas.onclick = { (e: dom.MouseEvent) =>
      globalEventStream.pushGlobalEvent(
        MouseEvent.Click(
          absoluteCoordsX(e.clientX) / magnification,
          absoluteCoordsY(e.clientY) / magnification
        )
      )
    }

    /*
      Follows the most conventional, basic definition of wheel.
      To be fair, the wheel event doesn't necessarily means that the device is a mouse, or even that the
      deltaY represents the direction of the vertical scrolling (usually negative is upwards and positive downwards).
      For the sake of simplicity, we're assuming a common mouse with a simple wheel.
     
      More info: https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent
     */
    canvas.onwheel = { (e: dom.WheelEvent) =>
      val wheel = MouseEvent.Wheel(
        Point(
          absoluteCoordsX(e.clientX) / magnification,
          absoluteCoordsY(e.clientY) / magnification
        ),
        e.deltaY
      )

      globalEventStream.pushGlobalEvent(wheel)
    }

    canvas.onmousemove = { (e: dom.MouseEvent) =>
      globalEventStream.pushGlobalEvent(
        MouseEvent.Move(
          absoluteCoordsX(e.clientX) / magnification,
          absoluteCoordsY(e.clientY) / magnification
        )
      )
    }

    canvas.onmousedown = { (e: dom.MouseEvent) =>
      MouseButton.fromOrdinalOpt(e.button).foreach { mouseButton =>
        globalEventStream.pushGlobalEvent(
          MouseEvent.MouseDown(
            absoluteCoordsX(e.clientX) / magnification,
            absoluteCoordsY(e.clientY) / magnification,
            mouseButton
          )
        )
      }
    }

    canvas.onmouseup = { (e: dom.MouseEvent) =>
      MouseButton.fromOrdinalOpt(e.button).foreach { mouseButton =>
        globalEventStream.pushGlobalEvent(
          MouseEvent.MouseUp(
            absoluteCoordsX(e.clientX) / magnification,
            absoluteCoordsY(e.clientY) / magnification,
            mouseButton
          )
        )
      }
    }

    // Prevent right mouse button from popping up the context menu
    canvas.oncontextmenu = _.preventDefault()

    document.onkeydown = { (e: dom.KeyboardEvent) =>
      globalEventStream.pushGlobalEvent(KeyboardEvent.KeyDown(Key(e.keyCode, e.key)))
    }

    document.onkeyup = { (e: dom.KeyboardEvent) =>
      globalEventStream.pushGlobalEvent(KeyboardEvent.KeyUp(Key(e.keyCode, e.key)))
    }

  }

}
