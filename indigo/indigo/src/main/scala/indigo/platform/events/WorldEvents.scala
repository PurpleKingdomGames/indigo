package indigo.platform.events

import indigo.shared.constants.Key
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
    canvas.onclick = { (e: dom.MouseEvent) =>
      globalEventStream.pushGlobalEvent(
        MouseEvent.Click(
          absoluteCoordsX(e.clientX) / magnification,
          absoluteCoordsY(e.clientY) / magnification,
          MouseButton.fromOrdinal(e.button)
        )
      )
    }

    canvas.onmousemove = { (e: dom.MouseEvent) =>
      globalEventStream.pushGlobalEvent(
        MouseEvent.Move(
          absoluteCoordsX(e.clientX) / magnification,
          absoluteCoordsY(e.clientY) / magnification,
          MouseButton.fromOrdinal(e.button)
        )
      )
    }

    canvas.onmousedown = { (e: dom.MouseEvent) =>
      globalEventStream.pushGlobalEvent(
        MouseEvent.MouseDown(
          absoluteCoordsX(e.clientX) / magnification,
          absoluteCoordsY(e.clientY) / magnification,
          MouseButton.fromOrdinal(e.button)
        )
      )
    }

    canvas.onmouseup = { (e: dom.MouseEvent) =>
      globalEventStream.pushGlobalEvent(
        MouseEvent.MouseUp(
          absoluteCoordsX(e.clientX) / magnification,
          absoluteCoordsY(e.clientY) / magnification,
          MouseButton.fromOrdinal(e.button)
        )
      )
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
