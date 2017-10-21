package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.gameengine.PowerOfTwo
import com.purplekingdomgames.indigo.gameengine.assets.TextureAtlas.supportedSizes
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point
import com.purplekingdomgames.indigo.util.Logger
import org.scalajs.dom
import org.scalajs.dom.{html, raw}

object TextureAtlas {

  import TextureAtlasFunctions._

  val IdPrefix: String = "atlas_"

  val MaxTextureSize: PowerOfTwo = PowerOfTwo._4096

  val supportedSizes: Set[PowerOfTwo] = PowerOfTwo.all

  def createWithMaxSize(max: PowerOfTwo, imageRefs: List[ImageRef], lookupByName: String => Option[LoadedImageAsset], createAtlasFunc: ((TextureMap, String => Option[LoadedImageAsset]) => Atlas)): TextureAtlas =
    (inflateAndSortByPowerOfTwo andThen groupTexturesIntoAtlasBuckets(max) andThen convertToAtlas(createAtlasFunc)(lookupByName))(imageRefs)

  def create(imageRefs: List[ImageRef], lookupByName: String => Option[LoadedImageAsset], createAtlasFunc: ((TextureMap, String => Option[LoadedImageAsset]) => Atlas)): TextureAtlas = {
    Logger.info(s"Creating atlases. Max size: ${MaxTextureSize.value}x${MaxTextureSize.value}")
    val textureAtlas = (inflateAndSortByPowerOfTwo andThen groupTexturesIntoAtlasBuckets(MaxTextureSize) andThen convertToAtlas(createAtlasFunc)(lookupByName)) (imageRefs)

    Logger.info(textureAtlas.report)

    textureAtlas
  }

  def lookUp(name: String, textureAtlas: TextureAtlas): Unit = ()

  val identity: TextureAtlas = TextureAtlas(Map(), Map())

}

// Output
case class TextureAtlas(atlases: Map[AtlasId, Atlas], legend: Map[String, AtlasIndex]) {
  def +(other: TextureAtlas): TextureAtlas = TextureAtlas(
    this.atlases ++ other.atlases,
    this.legend ++ other.legend
  )

  def lookUpByName(name: String): Option[AtlasLookupResult] =
    legend.get(name).flatMap { i =>
      atlases.get(i.id).map { a =>
        AtlasLookupResult(name, i.id, a, i.offset)
      }
    }

  def report: String = {
    val atlasRecordToString: Map[String, AtlasIndex] => ((AtlasId, Atlas)) => String = leg => at => {
      val relevant = leg.filter(k => k._2.id == at._1)

      s"Atlas [${at._1.id}] [${at._2.size.value}] contains images: ${relevant.toList.map(_._1).mkString(", ")}"
    }

    s"""Atlas details:
    |Number of atlases: ${atlases.keys.toList.length}
    |Atlases: [
    |  ${atlases.map(atlasRecordToString(legend)).mkString("\n  ")}
    |]
  """.stripMargin
    }

}
case class AtlasId(id: String)
case class AtlasIndex(id: AtlasId, offset: Point)
case class Atlas(size: PowerOfTwo, imageData: Option[raw.ImageData]) // Yuk. Only optional so that testing is bearable.
case class AtlasLookupResult(name: String, atlasId: AtlasId, atlas: Atlas, offset: Point)

object TextureAtlasFunctions {

  /**
    * Type fails all over the place, no guarantee that this list is in the right order...
    * so instead of just going through the set until we find a bigger value, we have to filter and fold all
    */
  def pickPowerOfTwoSizeFor(supportedSizes: Set[PowerOfTwo], width: Int, height: Int): PowerOfTwo =
    supportedSizes
      .filter(s => s.value >= width && s.value >= height)
      .foldLeft(PowerOfTwo.Max)(PowerOfTwo.min)

  def isTooBig(max: PowerOfTwo, width: Int, height: Int): Boolean = if(width > max.value || height > max.value) true else false

  val inflateAndSortByPowerOfTwo: List[ImageRef] => List[TextureDetails] = images =>
    images.map(i => TextureDetails(i, TextureAtlasFunctions.pickPowerOfTwoSizeFor(supportedSizes, i.width, i.height))).sortBy(_.size.value).reverse

  def groupTexturesIntoAtlasBuckets(max: PowerOfTwo): List[TextureDetails] => List[List[TextureDetails]] = list => {
    val runningTotal: List[TextureDetails] => Int = _.map(_.size.value).sum

    def rec(remaining: List[TextureDetails], current: List[TextureDetails], rejected: List[TextureDetails], acc: List[List[TextureDetails]], maximum: PowerOfTwo): List[List[TextureDetails]] = {
      (remaining, rejected) match {
        case (Nil, Nil) =>
          current :: acc

        case (Nil, x :: xs) =>
          rec(x :: xs, Nil, Nil, current :: acc, maximum)

        case (x :: xs, _) if x.size >= maximum =>
          rec(xs, current, rejected, List(x) :: acc, maximum)

        case (x :: xs, _) if runningTotal(current) + x.size.value > maximum.value * 2 =>
          rec(xs, current, x :: rejected, acc, maximum)

        case (x :: xs, _) =>
          rec(xs, x :: current, rejected, acc, maximum)

      }
    }

    rec(list, Nil, Nil, Nil, max)
  }

  private def createCanvas(width: Int, height: Int): html.Canvas = {
    val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
//    dom.document.body.appendChild(canvas)
    canvas.width = width
    canvas.height = height

    canvas
  }

  val createAtlasData: (TextureMap, String => Option[LoadedImageAsset]) => Atlas = (textureMap, lookupByName) => {
    val canvas: html.Canvas = createCanvas(textureMap.size.value, textureMap.size.value)
    val ctx = canvas.getContext("2d")

    textureMap.textureCoords.foreach { tex =>

      lookupByName(tex.imageRef.name).foreach { img =>
        ctx.drawImage(img.data, tex.coords.x, tex.coords.y, tex.imageRef.width, tex.imageRef.height)
      }

    }

    val imageData: raw.ImageData = ctx.getImageData(0, 0, textureMap.size.value, textureMap.size.value).asInstanceOf[raw.ImageData]

    Atlas(textureMap.size, Option(imageData))
  }

  val convertTextureDetailsToTree: TextureDetails => AtlasQuadTree = textureDetails => {
    AtlasQuadNode(textureDetails.size, AtlasTexture(textureDetails.imageRef))
  }

  val convertToTextureAtlas: ((TextureMap, String => Option[LoadedImageAsset]) => Atlas) => (String => Option[LoadedImageAsset]) => (AtlasId, List[TextureDetails]) => TextureAtlas = createAtlasFunc => lookupByName => (atlasId, list) =>
    list.map(convertTextureDetailsToTree).foldLeft(AtlasQuadTree.identity)(_ + _) match {
      case AtlasQuadEmpty(_) => TextureAtlas.identity
      case n: AtlasQuadNode =>
        val textureMap = n.toTextureMap

        val legend: Map[String, AtlasIndex] =
          textureMap.textureCoords.foldLeft(Map.empty[String, AtlasIndex])((m, t) => m ++ Map(t.imageRef.name -> AtlasIndex(atlasId, t.coords)))

        val atlas = createAtlasFunc(textureMap, lookupByName)

        TextureAtlas(
          atlases = Map(
            atlasId -> atlas
          ),
          legend = legend
        )
    }

  val combineTextureAtlases: List[TextureAtlas] => TextureAtlas = list =>
    list.foldLeft(TextureAtlas.identity)(_ + _)

  val convertToAtlas: ((TextureMap, String => Option[LoadedImageAsset]) => Atlas) => (String => Option[LoadedImageAsset]) => List[List[TextureDetails]] => TextureAtlas = createAtlasFunc => lookupByName => list =>
    combineTextureAtlases(list.zipWithIndex.map(p => convertToTextureAtlas(createAtlasFunc)(lookupByName)(AtlasId(TextureAtlas.IdPrefix + p._2), p._1)))

  def mergeTrees(a: AtlasQuadTree, b: AtlasQuadTree, max: PowerOfTwo): Option[AtlasQuadTree] =
    (a, b) match {
      case (AtlasQuadEmpty(_), AtlasQuadEmpty(_)) =>
        Some(a)

      case (AtlasQuadNode(_, _), AtlasQuadEmpty(_)) =>
        Some(a)

      case (AtlasQuadEmpty(_), AtlasQuadNode(_, _)) =>
        Some(b)

      case (AtlasQuadNode(_, _), AtlasQuadNode(sizeB, _)) if a.canAccommodate(sizeB) =>
        mergeTreeBIntoA(a, b)

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(_, _)) if b.canAccommodate(sizeA) =>
        mergeTreeBIntoA(b, a)

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(sizeB, _)) if sizeA >= sizeB && sizeA.doubled <= max =>
        mergeTreeBIntoA(createEmptyTree(calculateSizeNeededToHouseAB(sizeA, sizeB)), a).flatMap { c =>
          mergeTreeBIntoA(c, b)
        }

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(sizeB, _)) if sizeB >= sizeA && sizeB.doubled <= max =>
        mergeTreeBIntoA(createEmptyTree(calculateSizeNeededToHouseAB(sizeA, sizeB)), b).flatMap { c =>
          mergeTreeBIntoA(c, a)
        }

      case _ =>
        Logger.info("Could not merge trees")
        None
    }

  def mergeTreeBIntoA(a: AtlasQuadTree, b: AtlasQuadTree): Option[AtlasQuadTree] =
    if (!a.canAccommodate(b.size) && !b.canAccommodate(a.size)) None
    else Option {
      if(a.canAccommodate(b.size)) a.insert(b) else b.insert(a)
    }

  def calculateSizeNeededToHouseAB(sizeA: PowerOfTwo, sizeB: PowerOfTwo): PowerOfTwo =
    if(sizeA >= sizeB) sizeA.doubled else sizeB.doubled

  def createEmptyTree(size: PowerOfTwo): AtlasQuadNode = AtlasQuadNode(size, AtlasQuadDivision.empty(size.halved))

}


// Input
case class ImageRef(name: String, width: Int, height: Int)

case class TextureDetails(imageRef: ImageRef, size: PowerOfTwo)

case class TextureMap(size: PowerOfTwo, textureCoords: List[TextureAndCoords])
case class TextureAndCoords(imageRef: ImageRef, coords: Point)

// Intermediate tree structure
sealed trait AtlasQuadTree {
  val size: PowerOfTwo
  def canAccommodate(requiredSize: PowerOfTwo): Boolean
  def insert(tree: AtlasQuadTree): AtlasQuadTree

  def +(other: AtlasQuadTree): AtlasQuadTree = AtlasQuadTree.append(this, other)

  def toTextureCoordsList(offset: Point): List[TextureAndCoords]
}

// Oh look! It's a monoid...
object AtlasQuadTree {

  def identity: AtlasQuadTree = AtlasQuadEmpty(PowerOfTwo._1)

  def append(first: AtlasQuadTree, second: AtlasQuadTree): AtlasQuadTree =
    TextureAtlasFunctions.mergeTrees(first, second, PowerOfTwo.Max).getOrElse(first)

}

case class AtlasQuadNode(size: PowerOfTwo, atlas: AtlasSum) extends AtlasQuadTree {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean =
    if(size < requiredSize) false
    else atlas.canAccommodate(requiredSize)

  def insert(tree: AtlasQuadTree): AtlasQuadTree = this.copy ( atlas =
    atlas match {
      case AtlasTexture(_) => this.atlas

      case d @ AtlasQuadDivision(AtlasQuadEmpty(s), _, _, _) if s === tree.size =>
        d.copy(q1 = tree)
      case d @ AtlasQuadDivision(_, AtlasQuadEmpty(s), _, _) if s === tree.size =>
        d.copy(q2 = tree)
      case d @ AtlasQuadDivision(_, _, AtlasQuadEmpty(s), _) if s === tree.size =>
        d.copy(q3 = tree)
      case d @ AtlasQuadDivision(_, _, _, AtlasQuadEmpty(s)) if s === tree.size =>
        d.copy(q4 = tree)

      case d @ AtlasQuadDivision(AtlasQuadEmpty(s), _, _, _) if s > tree.size =>
        d.copy(q1 = TextureAtlasFunctions.createEmptyTree(s).insert(tree))
      case d @ AtlasQuadDivision(_, AtlasQuadEmpty(s), _, _) if s > tree.size =>
        d.copy(q2 = TextureAtlasFunctions.createEmptyTree(s).insert(tree))
      case d @ AtlasQuadDivision(_, _, AtlasQuadEmpty(s), _) if s > tree.size =>
        d.copy(q3 = TextureAtlasFunctions.createEmptyTree(s).insert(tree))
      case d @ AtlasQuadDivision(_, _, _, AtlasQuadEmpty(s)) if s > tree.size =>
        d.copy(q4 = TextureAtlasFunctions.createEmptyTree(s).insert(tree))

      case d @ AtlasQuadDivision(AtlasQuadNode(_, _), _, _, _) if d.q1.canAccommodate(tree.size) =>
        d.copy(q1 = d.q1.insert(tree))
      case d @ AtlasQuadDivision(_, AtlasQuadNode(_, _), _, _) if d.q2.canAccommodate(tree.size) =>
        d.copy(q2 = d.q2.insert(tree))
      case d @ AtlasQuadDivision(_, _, AtlasQuadNode(_, _), _) if d.q3.canAccommodate(tree.size) =>
        d.copy(q3 = d.q3.insert(tree))
      case d @ AtlasQuadDivision(_, _, _, AtlasQuadNode(_, _)) if d.q4.canAccommodate(tree.size) =>
        d.copy(q4 = d.q4.insert(tree))

      case _ =>
        Logger.info("Unexpected failure to insert tree")
        this.atlas
    }
  )

  def toTextureCoordsList(offset: Point): List[TextureAndCoords] =
    atlas match {
      case AtlasTexture(imageRef) =>
        List(TextureAndCoords(imageRef, offset))

      case AtlasQuadDivision(q1, q2, q3, q4) =>
        q1.toTextureCoordsList(offset) ++
          q2.toTextureCoordsList(offset + size.halved.toPoint.withY(0)) ++
          q3.toTextureCoordsList(offset + size.halved.toPoint.withX(0)) ++
          q4.toTextureCoordsList(offset + size.halved.toPoint)

    }


  def toTextureMap: TextureMap =
    TextureMap(size, toTextureCoordsList(Point.zero))
}

case class AtlasQuadEmpty(size: PowerOfTwo) extends AtlasQuadTree {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean = size >= requiredSize
  def insert(tree: AtlasQuadTree): AtlasQuadTree = this

  def toTextureCoordsList(offset: Point): List[TextureAndCoords] = Nil
}

sealed trait AtlasSum {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean
}

case class AtlasTexture(imageRef: ImageRef) extends AtlasSum {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean = false
}

case class AtlasQuadDivision(q1: AtlasQuadTree, q2: AtlasQuadTree, q3: AtlasQuadTree, q4: AtlasQuadTree) extends AtlasSum {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean =
    q1.canAccommodate(requiredSize) || q2.canAccommodate(requiredSize) || q3.canAccommodate(requiredSize) || q4.canAccommodate(requiredSize)
}

object AtlasQuadDivision {
  def empty(size: PowerOfTwo): AtlasQuadDivision = AtlasQuadDivision(AtlasQuadEmpty(size), AtlasQuadEmpty(size), AtlasQuadEmpty(size), AtlasQuadEmpty(size))
}