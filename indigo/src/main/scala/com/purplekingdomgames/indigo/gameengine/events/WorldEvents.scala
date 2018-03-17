package com.purplekingdomgames.indigo.gameengine.events

import org.scalajs.dom
import org.scalajs.dom.html

object WorldEvents {

  def apply(canvas: html.Canvas, magnification: Int): Unit = {
    canvas.onclick = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseEvent.Click(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousemove = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseEvent.Move(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmousedown = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseEvent.MouseDown(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    canvas.onmouseup = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseEvent.MouseUp(e.clientX.toInt / magnification, e.clientY.toInt / magnification))
    }

    dom.document.onkeydown = { e: dom.KeyboardEvent =>
      GlobalEventStream.push(KeyboardEvent.KeyDown(e.keyCode))
    }

    dom.document.onkeyup = { e: dom.KeyboardEvent =>
      GlobalEventStream.push(KeyboardEvent.KeyUp(e.keyCode))
    }

    dom.document.onkeypress = { e: dom.KeyboardEvent =>
      GlobalEventStream.push(KeyboardEvent.KeyPress(e.keyCode))
    }

  }

}
