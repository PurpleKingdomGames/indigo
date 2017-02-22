package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.renderer.{ImageAsset, LoadedImageAsset}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLImageElement
import org.scalajs.dom.{html, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object AssetManager {

  def loadAssets(imageAssets: Set[ImageAsset], textAssets: Set[TextAsset]): Future[AssetCollection] =
    for {
      i <- loadImageAssets(imageAssets.toList)
      t <- loadTextAssets(textAssets.toList)
    } yield AssetCollection(i, t)

  val loadImageAssets: List[ImageAsset] => Future[List[LoadedImageAsset]] = imageAssets =>
    Future.sequence(imageAssets.map(loadImageAsset))

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

  def loadImageAsset(imageAsset: ImageAsset): Future[LoadedImageAsset] = {

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = imageAsset.path

    onLoadFuture(image).map(i => LoadedImageAsset(imageAsset.name, i))
  }

  val loadTextAssets: List[TextAsset] => Future[List[LoadedTextAsset]] = textAssets =>
    Future.sequence(textAssets.map(loadTextAsset))

  def loadTextAsset(textAsset: TextAsset): Future[LoadedTextAsset] = {
    val p = Promise[LoadedTextAsset]()

    val client = new XMLHttpRequest()
    client.open("GET", s"${textAsset.path}")
    client.onreadystatechange = (_: Event) => {
      if(client.readyState == XMLHttpRequest.DONE && client.status == 200) {
        println("body: " + client.responseText)

        p.success(LoadedTextAsset(textAsset.name, client.responseText))
      }
    }
    client.send()

    p.future
  }

}

sealed trait TextAssetStates
case class TextAsset(name: String, path: String) extends TextAssetStates
case class LoadedTextAsset(name: String, contents: String) extends TextAssetStates

case class AssetCollection(images: List[LoadedImageAsset], texts: List[LoadedTextAsset])