package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.gameengine.{PowerOfTwo, assets}
import com.purplekingdomgames.indigo.gameengine.assets.TextureAtlas.supportedSizes
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point
import com.purplekingdomgames.indigo.util.Logger

object TextureAtlas {

  import TextureAtlasFunctions._

  val MaxTextureSize: PowerOfTwo = PowerOfTwo._4096

  val supportedSizes: Set[PowerOfTwo] = PowerOfTwo.all

  def create(images: List[ImageRef]): TextureAtlas =
    (filterTooLarge(MaxTextureSize) andThen inflateAndSortByPowerOfTwo andThen convertToAtlas)(images)

  def lookUp(name: String, textureAtlas: TextureAtlas): Unit = ()


}


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

  def filterTooLarge(max: PowerOfTwo): List[ImageRef] => List[ImageRef] = images =>
    images.flatMap { i =>
      if(TextureAtlasFunctions.isTooBig(max, i.width, i.height)) {
        // I think we'll still access assets through the asset collection, which can try for the atlas first and fallback
        Logger.info(s"Image ${i.name} is too large and will not be added to the texture atlas - may cause performance penalties")
        Nil
      } else List(i)
    }

  val inflateAndSortByPowerOfTwo: List[ImageRef] => List[TextureDetails] = images =>
    images.map(i => TextureDetails(i, TextureAtlasFunctions.pickPowerOfTwoSizeFor(supportedSizes, i.width, i.height))).sortBy(_.size.value).reverse

  val convertTextureDetailsToTree: TextureDetails => AtlasQuadTree = textureDetails => {
    AtlasQuadNode(textureDetails.size, AtlasTexture(textureDetails.imageRef))
  }

  val convertToAtlas: List[TextureDetails] => TextureAtlas = list => {
    list.map(convertTextureDetailsToTree)

    TextureAtlas(Map(), Map())
  }

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

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(sizeB, _)) if sizeA >= sizeB =>
        mergeTreeBIntoA(createEmptyTree(calculateSizeNeededToHouseAB(sizeA, sizeB)), a).flatMap { c =>
          mergeTreeBIntoA(c, b)
        }

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(sizeB, _)) if sizeA < sizeB =>
        mergeTreeBIntoA(createEmptyTree(calculateSizeNeededToHouseAB(sizeA, sizeB)), b).flatMap { c =>
          mergeTreeBIntoA(c, a)
        }

      case _ =>
        Logger.info("Unexpectedly couldn't merge trees")
        None
    }

  def mergeTreeBIntoA(a: AtlasQuadTree, b: AtlasQuadTree): Option[AtlasQuadTree] = {
    if(!a.canAccommodate(b.size)) None
    else {
      //TODO: Use lens?

      None
    }
  }

  def calculateSizeNeededToHouseAB(sizeA: PowerOfTwo, sizeB: PowerOfTwo): PowerOfTwo =
    if(sizeA >= sizeB) sizeA.doubled else sizeB.doubled

  def createEmptyTree(size: PowerOfTwo): AtlasQuadNode = AtlasQuadNode(size, AtlasQuadDivision.empty(size))

}


// Input
case class ImageRef(name: String, width: Int, height: Int)

case class TextureDetails(imageRef: ImageRef, size: PowerOfTwo)

// Output
case class TextureAtlas(atlases: Map[AtlasId, Atlas], legend: Map[String, AtlasIndex])
case class AtlasId(id: String)
case class AtlasIndex(id: AtlasId, offset: Point)
case class Atlas(/*TODO: image data??*/)

// Intermediate tree structure
sealed trait AtlasQuadTree {
  val size: PowerOfTwo
  def canAccommodate(requiredSize: PowerOfTwo): Boolean
}
case class AtlasQuadNode(size: PowerOfTwo, atlas: AtlasSum) extends AtlasQuadTree {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean =
    if(size < requiredSize) false
    else atlas.canAccommodate(requiredSize)
}
case class AtlasQuadEmpty(size: PowerOfTwo) extends AtlasQuadTree {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean = size >= requiredSize
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