package purple.gameengine

import org.scalajs.dom
import org.scalajs.dom.html

object WorldEvents {

  def apply(canvas: html.Canvas): Unit = {

    canvas.onclick = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseClick(e.clientX.toInt, e.clientY.toInt))
    }

    canvas.onmousemove = { e: dom.MouseEvent =>
      GlobalEventStream.push(MousePosition(e.clientX.toInt, e.clientY.toInt))
    }

    canvas.onmousedown = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseDown(e.clientX.toInt, e.clientY.toInt))
    }

    canvas.onmouseup = { e: dom.MouseEvent =>
      GlobalEventStream.push(MouseUp(e.clientX.toInt, e.clientY.toInt))
    }

    dom.document.onkeydown = { e: dom.KeyboardEvent =>
      GlobalEventStream.push(KeyDown(e.keyCode))
    }

    dom.document.onkeyup = { e: dom.KeyboardEvent =>
      GlobalEventStream.push(KeyUp(e.keyCode))
    }

  }

}
