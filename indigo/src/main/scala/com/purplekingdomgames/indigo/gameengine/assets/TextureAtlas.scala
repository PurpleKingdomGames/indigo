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
        println(1)
        Some(a)

      case (AtlasQuadNode(_, _), AtlasQuadEmpty(_)) =>
        println(2)
        Some(a)

      case (AtlasQuadEmpty(_), AtlasQuadNode(_, _)) =>
        println(3)
        Some(b)

      case (AtlasQuadNode(_, _), AtlasQuadNode(sizeB, _)) if a.canAccommodate(sizeB) =>
        println(4)
        mergeTreeBIntoA(a, b)

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(_, _)) if b.canAccommodate(sizeA) =>
        println(5)
        mergeTreeBIntoA(b, a)

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(sizeB, _)) if sizeA >= sizeB =>
        println(6)
        mergeTreeBIntoA(createEmptyTree(calculateSizeNeededToHouseAB(sizeA, sizeB)), a).flatMap { c =>
          mergeTreeBIntoA(c, b)
        }

      case (AtlasQuadNode(sizeA, _), AtlasQuadNode(sizeB, _)) if sizeA < sizeB =>
        println(7)
        mergeTreeBIntoA(createEmptyTree(calculateSizeNeededToHouseAB(sizeA, sizeB)), b).flatMap { c =>
          mergeTreeBIntoA(c, a)
        }

      case _ =>
        println(8)
        Logger.info("Unexpectedly couldn't merge trees")
        None
    }

  /*
  6

B <- A
A: AtlasQuadNode(_2048,AtlasQuadDivision(AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024)))
B: AtlasQuadNode(_1024,AtlasTexture(ImageRef(a,1024,768)))
Result: Some(AtlasQuadNode(_2048,AtlasQuadDivision(AtlasQuadNode(_1024,AtlasTexture(ImageRef(a,1024,768))),AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024))))

B <- A
A: AtlasQuadNode(_2048,AtlasQuadDivision(AtlasQuadNode(_1024,AtlasTexture(ImageRef(a,1024,768))),AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024)))
B: AtlasQuadNode(_512,AtlasTexture(ImageRef(b,500,400)))
Result: Some(AtlasQuadNode(_2048,AtlasQuadDivision(AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024),AtlasQuadEmpty(_1024))))

Not subdividing, and it's probably doing an insert on an empty that return 'this'

   */

  def mergeTreeBIntoA(a: AtlasQuadTree, b: AtlasQuadTree): Option[AtlasQuadTree] = {
    println("B <- A")
    println("A: " + a)
    println("B: " + b)

    val res = if (!a.canAccommodate(b.size)) None
    else Option {
      a.insert(b)
    }

    println("Result: " + res)

    res
  }

  def calculateSizeNeededToHouseAB(sizeA: PowerOfTwo, sizeB: PowerOfTwo): PowerOfTwo =
    if(sizeA >= sizeB) sizeA.doubled else sizeB.doubled

  def createEmptyTree(size: PowerOfTwo): AtlasQuadNode = AtlasQuadNode(size, AtlasQuadDivision.empty(size.halved))

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
  def insert(tree: AtlasQuadTree): AtlasQuadTree
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

      case d @ AtlasQuadDivision(q, _, _, _) if q.canAccommodate(tree.size) =>
        d.copy(q1 = d.q1.insert(tree))
      case d @ AtlasQuadDivision(_, q, _, _) if q.canAccommodate(tree.size) =>
        d.copy(q1 = d.q2.insert(tree))
      case d @ AtlasQuadDivision(_, _, q, _) if q.canAccommodate(tree.size) =>
        d.copy(q1 = d.q3.insert(tree))
      case d @ AtlasQuadDivision(_, _, _, q) if q.canAccommodate(tree.size) =>
        d.copy(q1 = d.q4.insert(tree))
    }
  )
}

case class AtlasQuadEmpty(size: PowerOfTwo) extends AtlasQuadTree {
  def canAccommodate(requiredSize: PowerOfTwo): Boolean = size >= requiredSize
  def insert(tree: AtlasQuadTree): AtlasQuadTree = this
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