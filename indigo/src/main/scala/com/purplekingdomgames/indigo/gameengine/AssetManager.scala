package com.purplekingdomgames.indigo.gameengine

import org.scalajs.dom
import org.scalajs.dom.{html, _}
import org.scalajs.dom.raw.HTMLImageElement
import com.purplekingdomgames.indigo.renderer.{ImageAsset, LoadedImageAsset}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object AssetManager {

  val loadAssets: List[ImageAsset] => Future[List[LoadedImageAsset]] = imageAssets => {
    Future.sequence(imageAssets.map(loadAsset))
  }

  private def onLoadFuture(image: HTMLImageElement): Future[HTMLImageElement] = {
    if (image.complete) {
      Future.successful(image)
    } else {
      val p = Promise[HTMLImageElement]()
      image.onload = { (_: Event) =>
        p.success(image)
      }
      p.future
    }
  }

  def loadAsset(imageAsset: ImageAsset): Future[LoadedImageAsset] = {

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = imageAsset.path

    onLoadFuture(image).map(i => LoadedImageAsset(imageAsset.name, i))
  }

}
