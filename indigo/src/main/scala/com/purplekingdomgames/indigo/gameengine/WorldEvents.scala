package com.purplekingdomgames.indigo.gameengine

import org.scalajs.dom
import org.scalajs.dom.html

object WorldEvents {

  def apply(canvas: html.Canvas, magnification: Int): Unit = {
    canvas.onclick = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseClick(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousemove = { e: dom.MouseEvent =>
      GlobalEventStream.push(MousePosition(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousedown = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseDown(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmouseup = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseUp(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    dom.document.onkeydown = { e: dom.KeyboardEvent =>
      GlobalEventStream.push(KeyDown(e.keyCode))
    }

    dom.document.onkeyup = { e: dom.KeyboardEvent =>
      GlobalEventStream.push(KeyUp(e.keyCode))
    }

    dom.document.onkeypress = { e: dom.KeyboardEvent =>
      GlobalEventStream.push(KeyPress(e.keyCode))
    }

  }

}
