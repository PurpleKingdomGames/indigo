package indigo.gameengine.events

import indigo.gameengine.constants.Keys
import org.scalajs.dom
import org.scalajs.dom.html

object WorldEvents {

  def apply(canvas: html.Canvas, magnification: Int): Unit = {
    canvas.onclick = { e: dom.MouseEvent =>
      GlobalEventStream.pushGameEvent(MouseEvent.Click(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousemove = { e: dom.MouseEvent =>
      GlobalEventStream.pushGameEvent(MouseEvent.Move(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousedown = { e: dom.MouseEvent =>
      GlobalEventStream.pushGameEvent(MouseEvent.MouseDown(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmouseup = { e: dom.MouseEvent =>
      GlobalEventStream.pushGameEvent(MouseEvent.MouseUp(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    dom.document.onkeydown = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        GlobalEventStream.pushGameEvent(KeyboardEvent.KeyDown(kc))
      }
    }

    dom.document.onkeyup = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        GlobalEventStream.pushGameEvent(KeyboardEvent.KeyUp(kc))
      }
    }

    dom.document.onkeypress = { e: dom.KeyboardEvent =>
      Keys.codeToKeyCode(e.keyCode).foreach { kc =>
        GlobalEventStream.pushGameEvent(KeyboardEvent.KeyPress(kc))
      }
    }

  }

}
