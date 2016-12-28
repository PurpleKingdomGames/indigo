package com.example.scalajsgame

import org.scalajs.dom
import org.scalajs.dom.html
import purple.renderer.{ContextAndCanvas, Engine, Rectangle2D}

import scala.language.implicitConversions
import scala.scalajs.js.JSApp

object MyGame extends JSApp {

  private val viewportHeight: Int = 256
  private val viewportWidth: Int = (viewportHeight.toDouble * (16d / 9d)).toInt

  def main(): Unit = {

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = "Sprite-0001.png"
//    image.src = "f-texture.png"
    image.onload = (_: dom.Event) => {

      implicit val cnc: ContextAndCanvas = Engine.createCanvas("canvas", viewportWidth, viewportHeight)

      Engine.addRectangle(Rectangle2D(0, 0, 64, 64, image))
      Engine.addRectangle(Rectangle2D(32, 32, 64, 64, image))
      Engine.addRectangle(Rectangle2D(viewportWidth - 64, viewportHeight - 64, 64, 64, image))

      Engine.drawScene
    }

  }

}
