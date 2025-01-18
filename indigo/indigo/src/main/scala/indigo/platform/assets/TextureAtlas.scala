package indigo.platform.assets

import indigo.shared.IndigoLogger
import indigo.shared.PowerOfTwo
import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetTag
import indigo.shared.datatypes.Point
import org.scalajs.dom
import org.scalajs.dom.ImageData
import org.scalajs.dom.html

import scala.annotation.tailrec

import scalajs.js.JSConverters.*

object TextureAtlas {

  import TextureAtlasFunctions._

  val IdPrefix: String = "atlas_"

  val MaxTextureSize: PowerOfTwo = PowerOfTwo._4096

  val supportedSizes: Set[PowerOfTwo] = PowerOfTwo.all

  def createWithMaxSize(
      max: PowerOfTwo,
      imageRefs: List[ImageRef],
      lookupByName: AssetName => Option[LoadedImageAsset],
      createAtlasFunc: (TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas
  ): TextureAtlas =
    (inflateAndSortByPowerOfTwo andThen groupTexturesIntoAtlasBuckets(max) andThen convertToAtlas(createAtlasFunc)(
      lookupByName
    ))(
      imageRefs
    )

  def create(
      imageRefs: List[ImageRef],
      lookupByName: AssetName => Option[LoadedImageAsset],
      createAtlasFunc: (TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas
  ): TextureAtlas = {
    IndigoLogger.info(
      s"Creating atlases. Max size: ${MaxTextureSize.value.toString()}x${MaxTextureSize.value.toString()}"
    )
    val textureAtlas =
      (inflateAndSortByPowerOfTwo andThen groupTexturesIntoAtlasBuckets(MaxTextureSize) andThen convertToAtlas(
        createAtlasFunc
      )(lookupByName))(imageRefs)

    IndigoLogger.info(textureAtlas.report)

    textureAtlas
  }

  val identity: TextureAtlas = TextureAtlas(scalajs.js.Dictionary.empty[Atlas], scalajs.js.Dictionary.empty[AtlasIndex])

}

// Output
final case class TextureAtlas(atlases: scalajs.js.Dictionary[Atlas], legend: scalajs.js.Dictionary[AtlasIndex])
    derives CanEqual {
  def +(other: TextureAtlas): TextureAtlas =
    TextureAtlas(
      (atlases ++ other.atlases).toJSDictionary,
      (legend ++ other.legend).toJSDictionary
    )

  def lookUpByName(name: AssetName): Option[AtlasLookupResult] =
    legend.get(name.toString).flatMap { i =>
      atlases.get(i.id.toString).map { a =>
        new AtlasLookupResult(name, i.id, a, i.offset)
      }
    }

  def report: String = {
    val atlasRecordToString: scalajs.js.Dictionary[AtlasIndex] => ((String, Atlas)) => String = leg =>
      at => {
        val relevant = leg.filter { (k: (String, AtlasIndex)) =>
          k._2.id.toString == at._1
        }

        s"Atlas [${at._1}] [${at._2.size.value.toString()}] contains images: ${relevant.toList.map(_._1).mkString(", ")}"
      }

    s"""Atlas details:
    |Number of atlases: ${atlases.keys.toList.length.toString()}
    |Atlases: [
    |  ${atlases.map(atlasRecordToString(legend)).mkString("\n  ")}
    |]
  """.stripMargin
  }

}

opaque type AtlasId = String
object AtlasId:
  inline def apply(id: String): AtlasId                = id
  extension (aid: AtlasId) inline def toString: String = aid
  given CanEqual[AtlasId, AtlasId]                     = CanEqual.derived
  given CanEqual[Option[AtlasId], Option[AtlasId]]     = CanEqual.derived

final case class AtlasIndex(id: AtlasId, offset: Point, size: Point) derives CanEqual

final case class Atlas(
    size: PowerOfTwo,
    imageData: Option[ImageData]
) derives CanEqual // Yuk. Only optional so that testing is bearable.

final case class AtlasLookupResult(name: AssetName, atlasId: AtlasId, atlas: Atlas, offset: Point) derives CanEqual

object TextureAtlasFunctions {

  /** Type fails all over the place, no guarantee that this list is in the right order... so instead of just going
    * through the set until we find a bigger value, we have to filter and fold all
    */
  def pickPowerOfTwoSizeFor(supportedSizes: Set[PowerOfTwo], width: Int, height: Int): PowerOfTwo =
    supportedSizes
      .filter(s => s.value >= width && s.value >= height)
      .foldLeft(PowerOfTwo.Max)(PowerOfTwo.min)

  def isTooBig(max: PowerOfTwo, width: Int, height: Int): Boolean =
    if (width > max.value || height > max.value) true else false

  val inflateAndSortByPowerOfTwo: List[ImageRef] => List[TextureDetails] = images =>
    images
      .map(i =>
        TextureDetails(
          i,
          TextureAtlasFunctions.pickPowerOfTwoSizeFor(TextureAtlas.supportedSizes, i.width, i.height),
          i.tag
        )
      )
      .sortBy(_.size.value)
      .reverse

  def groupTexturesIntoAtlasBuckets(max: PowerOfTwo): List[TextureDetails] => List[List[TextureDetails]] =
    list => {
      val runningTotal: List[TextureDetails] => Int = _.map(_.size.value).sum

      @tailrec
      def createBuckets(
          remaining: List[TextureDetails],
          current: List[TextureDetails],
          rejected: List[TextureDetails],
          acc: List[List[TextureDetails]],
          maximum: PowerOfTwo
      ): List[List[TextureDetails]] =
        (remaining, rejected) match {
          case (Nil, Nil) =>
            current :: acc

          case (Nil, x :: xs) =>
            createBuckets(x :: xs, Nil, Nil, current :: acc, maximum)

          case (x :: xs, _) if x.size >= maximum =>
            createBuckets(xs, current, rejected, List(x) :: acc, maximum)

          case (x :: xs, _) if runningTotal(current) + x.size.value > maximum.value * 2 =>
            createBuckets(xs, current, x :: rejected, acc, maximum)

          case (x :: xs, _) =>
            createBuckets(xs, x :: current, rejected, acc, maximum)

        }

      def sortAndGroupByTag: List[TextureDetails] => List[(String, List[TextureDetails])] =
        _.groupBy(_.tag.map(_.toString).getOrElse("")).toList.sortBy(_._1)

      sortAndGroupByTag(list).flatMap { case (_, tds) =>
        createBuckets(tds, Nil, Nil, Nil, max)
      }
    }

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  private def createCanvas(width: Int, height: Int): html.Canvas = {
    val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
    // Handy if you want to draw the atlas to the page...
    // dom.document.body.appendChild(canvas)
    canvas.width = width
    canvas.height = height

    canvas
  }

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  val createAtlasData: (TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas = (textureMap, lookupByName) => {
    val canvas: html.Canvas = createCanvas(textureMap.size.value, textureMap.size.value)
    val ctx                 = canvas.getContext("2d")

    textureMap.textureCoords.foreach { tex =>
      lookupByName(tex.imageRef.name).foreach { img =>
        ctx.drawImage(img.data, tex.coords.x, tex.coords.y, tex.imageRef.width, tex.imageRef.height)
      }

    }

    val imageData: ImageData =
      ctx.getImageData(0, 0, textureMap.size.value, textureMap.size.value).asInstanceOf[ImageData]

    new Atlas(textureMap.size, Option(imageData))
  }

  val convertTextureDetailsToTree: TextureDetails => AtlasQuadTree = textureDetails =>
    AtlasQuadNode(textureDetails.size, AtlasTexture(textureDetails.imageRef))

  val convertToTextureAtlas: ((TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas) => (
      AssetName => Option[LoadedImageAsset]
  ) => (AtlasId, List[TextureDetails]) => TextureAtlas = createAtlasFunc =>
    lookupByName =>
      (atlasId, list) =>
        list.map(convertTextureDetailsToTree).foldLeft(AtlasQuadTree.identity)(_ + _) match {
          case AtlasQuadEmpty(_) => TextureAtlas.identity
          case n: AtlasQuadNode =>
            val textureMap = n.toTextureMap

            val legend: scalajs.js.Dictionary[AtlasIndex] =
              textureMap.textureCoords.foldLeft(scalajs.js.Dictionary.empty[AtlasIndex]) { (m, t) =>
                val name = t.imageRef.name
                val size = lookupByName(name).map(img => Point(img.data.width, img.data.height)).getOrElse(Point.zero)
                (m ++ scalajs.js.Dictionary(name.toString -> new AtlasIndex(atlasId, t.coords, size))).toJSDictionary
              }

            val atlas = createAtlasFunc(textureMap, lookupByName)

            TextureAtlas(
              atlases = scalajs.js.Dictionary(
                atlasId.toString -> atlas
              ),
              legend = legend
            )
        }

  val combineTextureAtlases: List[TextureAtlas] => TextureAtlas = list => list.foldLeft(TextureAtlas.identity)(_ + _)

  val convertToAtlas: ((TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas) => (
      AssetName => Option[LoadedImageAsset]
  ) => List[List[TextureDetails]] => TextureAtlas = createAtlasFunc =>
    lookupByName =>
      list =>
        combineTextureAtlases(
          list.zipWithIndex
            .map(p =>
              convertToTextureAtlas(createAtlasFunc)(lookupByName)(
                AtlasId(TextureAtlas.IdPrefix + p._2.toString),
                p._1
              )
            )
        )

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
        IndigoLogger.info("Could not merge trees")
        None
    }

  def mergeTreeBIntoA(a: AtlasQuadTree, b: AtlasQuadTree): Option[AtlasQuadTree] =
    if (!a.canAccommodate(b.size) && !b.canAccommodate(a.size)) None
    else
      Option {
        if (a.canAccommodate(b.size)) a.insert(b) else b.insert(a)
      }

  def calculateSizeNeededToHouseAB(sizeA: PowerOfTwo, sizeB: PowerOfTwo): PowerOfTwo =
    if (sizeA >= sizeB) sizeA.doubled else sizeB.doubled

  def createEmptyTree(size: PowerOfTwo): AtlasQuadNode = AtlasQuadNode(size, AtlasQuadDivision.empty(size.halved))

}

// Input
final case class ImageRef(name: AssetName, width: Int, height: Int, tag: Option[AssetTag]) derives CanEqual

final case class TextureDetails(imageRef: ImageRef, size: PowerOfTwo, tag: Option[AssetTag]) derives CanEqual

final case class TextureMap(size: PowerOfTwo, textureCoords: List[TextureAndCoords]) derives CanEqual
final case class TextureAndCoords(imageRef: ImageRef, coords: Point) derives CanEqual

sealed trait AtlasQuadTree {
  val size: PowerOfTwo
  def canAccommodate(requiredSize: PowerOfTwo): Boolean
  def insert(tree: AtlasQuadTree): AtlasQuadTree

  def +(other: AtlasQuadTree): AtlasQuadTree = AtlasQuadTree.append(this, other)

  def toTextureCoordsList(offset: Point): List[TextureAndCoords]
}

object AtlasQuadTree {

  def identity: AtlasQuadTree = AtlasQuadEmpty(PowerOfTwo._2)

  def append(first: AtlasQuadTree, second: AtlasQuadTree): AtlasQuadTree =
    TextureAtlasFunctions.mergeTrees(first, second, PowerOfTwo.Max).getOrElse(first)

}

final case class AtlasQuadNode(size: PowerOfTwo, atlas: AtlasSum) extends AtlasQuadTree derives CanEqual {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean =
    if (size < requiredSize) false
    else atlas.canAccommodate(requiredSize)

  def insert(tree: AtlasQuadTree): AtlasQuadTree =
    this.copy(atlas = atlas match {
      case AtlasTexture(_) => this.atlas

      case d @ AtlasQuadDivision(AtlasQuadEmpty(s), _, _, _) if s == tree.size =>
        d.copy(q1 = tree)
      case d @ AtlasQuadDivision(_, AtlasQuadEmpty(s), _, _) if s == tree.size =>
        d.copy(q2 = tree)
      case d @ AtlasQuadDivision(_, _, AtlasQuadEmpty(s), _) if s == tree.size =>
        d.copy(q3 = tree)
      case d @ AtlasQuadDivision(_, _, _, AtlasQuadEmpty(s)) if s == tree.size =>
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
        IndigoLogger.info("Unexpected failure to insert tree")
        this.atlas
    })

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

final case class AtlasQuadEmpty(size: PowerOfTwo) extends AtlasQuadTree derives CanEqual {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean = size >= requiredSize
  def insert(tree: AtlasQuadTree): AtlasQuadTree        = this

  def toTextureCoordsList(offset: Point): List[TextureAndCoords] = Nil
}

sealed trait AtlasSum {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean
}

final case class AtlasTexture(imageRef: ImageRef) extends AtlasSum derives CanEqual {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean = false
}

final case class AtlasQuadDivision(q1: AtlasQuadTree, q2: AtlasQuadTree, q3: AtlasQuadTree, q4: AtlasQuadTree)
    extends AtlasSum derives CanEqual {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean =
    q1.canAccommodate(requiredSize) || q2.canAccommodate(requiredSize) || q3.canAccommodate(requiredSize) || q4
      .canAccommodate(
        requiredSize
      )
}

object AtlasQuadDivision {
  def empty(size: PowerOfTwo): AtlasQuadDivision =
    AtlasQuadDivision(AtlasQuadEmpty(size), AtlasQuadEmpty(size), AtlasQuadEmpty(size), AtlasQuadEmpty(size))
}
