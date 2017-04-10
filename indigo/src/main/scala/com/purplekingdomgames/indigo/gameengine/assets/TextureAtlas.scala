package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.gameengine.PowerOfTwo
import com.purplekingdomgames.indigo.gameengine.assets.TextureAtlas.{MaxTextureSize, supportedSizes}
import com.purplekingdomgames.indigo.util.Logger

object TextureAtlas {

  import TextureAtlasFunctions._

  val MaxTextureSize: PowerOfTwo = PowerOfTwo._4096

  val supportedSizes: Set[PowerOfTwo] = PowerOfTwo.all

  def create(images: List[ImageRef]): TextureAtlas = {

    val q = filterTooLarge(MaxTextureSize) andThen inflateAndSortByPowerOfTwo

    TextureAtlas()
  }

}


case class ImageRef(name: String, width: Int, height: Int)

case class TextureDetails(imageRef: ImageRef, size: PowerOfTwo)

case class TextureAtlas()

sealed trait AtlasQuadTree
case class AtlasQuadNode(textureSize: PowerOfTwo, atlas: AtlasSum) extends AtlasQuadTree
case object AtlasQuadEmpty extends AtlasQuadTree

sealed trait AtlasSum
case class AtlasTexture() extends AtlasSum
case class AtlasQuadDivision(q1: AtlasQuadTree, q2: AtlasQuadTree, q3: AtlasQuadTree, q4: AtlasQuadTree) extends AtlasSum

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
}