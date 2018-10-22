package indigo.gameengine.events

import indigo.gameengine.constants.Keys
import org.scalajs.dom
import org.scalajs.dom.html

object WorldEvents {

  def apply(canvas: html.Canvas, magnification: Int)(implicit globalEventStream: GlobalEventStream): Unit = {
    canvas.onclick = { e: dom.MouseEvent =>
      globalEventStream.pushGameEvent(MouseEvent.Click(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousemove = { e: dom.MouseEvent =>
      globalEventStream.pushGameEvent(MouseEvent.Move(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousedown = { e: dom.MouseEvent =>
      globalEventStream.pushGameEvent(MouseEvent.MouseDown(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmouseup = { e: dom.MouseEvent =>
      globalEventStream.pushGameEvent(MouseEvent.MouseUp(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    dom.document.onkeydown = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        globalEventStream.pushGameEvent(KeyboardEvent.KeyDown(kc))
      }
    }

    dom.document.onkeyup = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        globalEventStream.pushGameEvent(KeyboardEvent.KeyUp(kc))
      }
    }

    dom.document.onkeypress = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        globalEventStream.pushGameEvent(KeyboardEvent.KeyPress(kc))
      }
    }

  }

}
