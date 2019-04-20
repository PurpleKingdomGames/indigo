package indigo.gameengine

import indigo.shared.constants.Keys
import indigo.shared.events.{MouseEvent, KeyboardEvent}
import org.scalajs.dom
import org.scalajs.dom.html

object WorldEvents {

  def apply(canvas: html.Canvas, magnification: Int, globalEventStream: GlobalEventStream): Unit = {
    canvas.onclick = { e: dom.MouseEvent =>
      globalEventStream.pushGlobalEvent(MouseEvent.Click(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousemove = { e: dom.MouseEvent =>
      globalEventStream.pushGlobalEvent(MouseEvent.Move(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousedown = { e: dom.MouseEvent =>
      globalEventStream.pushGlobalEvent(MouseEvent.MouseDown(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmouseup = { e: dom.MouseEvent =>
      globalEventStream.pushGlobalEvent(MouseEvent.MouseUp(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    dom.document.onkeydown = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyDown(kc))
      }
    }

    dom.document.onkeyup = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyUp(kc))
      }
    }

    dom.document.onkeypress = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        globalEventStream.pushGlobalEvent(KeyboardEvent.KeyPress(kc))
      }
    }

  }

}
