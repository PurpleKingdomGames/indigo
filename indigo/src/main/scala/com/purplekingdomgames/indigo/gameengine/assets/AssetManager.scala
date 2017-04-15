package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.renderer.LoadedTextureAsset
import com.purplekingdomgames.indigo.util.Logger
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLImageElement
import org.scalajs.dom.{html, _}

import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object AssetManager {

  private def filterOutTextAssets(l: List[AssetType]): List[TextAsset] =
    l.flatMap { at =>
      at match {
        case t: TextAsset => List(t)
        case _ => Nil
      }
    }

  private def filterOutImageAssets(l: List[AssetType]): List[ImageAsset] =
    l.flatMap { at =>
      at match {
        case t: ImageAsset => List(t)
        case _ => Nil
      }
    }

  def loadAssets(assets: Set[AssetType]): Future[AssetCollection] = {
    Logger.info(s"Loading ${assets.toList.length} assets")

    for {
      t <- loadTextAssets(filterOutTextAssets(assets.toList))
      i <- loadImageAssets(filterOutImageAssets(assets.toList))
    } yield AssetCollection(i, t)
  }

  def findByName(assetCollection: AssetCollection): String => Option[LoadedImageAsset] = name =>
    assetCollection.images.find(p => p.name == name)

  private val loadImageAssets: List[ImageAsset] => Future[List[LoadedImageAsset]] = imageAssets =>
    Future.sequence(imageAssets.map(loadImageAsset))

  private def onLoadFuture(image: HTMLImageElement): Future[HTMLImageElement] =
    if (image.complete) Future.successful(image)
    else {
      val p = Promise[HTMLImageElement]()
      image.onload = { (_: Event) =>
        p.success(image)
      }
      p.future
    }

  private def loadImageAsset(imageAsset: ImageAsset): Future[LoadedImageAsset] = {
    Logger.info(s"[Image] Loading ${imageAsset.path}")

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = imageAsset.path

    onLoadFuture(image).map { i =>
      Logger.info(s"[Image] Success ${imageAsset.path}")
      LoadedImageAsset(imageAsset.name, i)
    }
  }

  private val loadTextAssets: List[TextAsset] => Future[List[LoadedTextAsset]] = textAssets =>
    Future.sequence(textAssets.map(loadTextAsset))

  private def loadTextAsset(textAsset: TextAsset): Future[LoadedTextAsset] = {
    Logger.info(s"[Text] Loading ${textAsset.path}")

    Ajax.get(textAsset.path, responseType = "text").map { xhr =>
      Logger.info(s"[Text] Success ${textAsset.path}")
      LoadedTextAsset(textAsset.name, xhr.responseText)
    }
  }

}

sealed trait AssetType
case class TextAsset(name: String, path: String) extends AssetType
case class ImageAsset(name: String, path: String) extends AssetType

case class LoadedTextAsset(name: String, contents: String)
case class LoadedImageAsset(name: String, data: html.Image)

case class AssetCollection(images: List[LoadedImageAsset], texts: List[LoadedTextAsset])