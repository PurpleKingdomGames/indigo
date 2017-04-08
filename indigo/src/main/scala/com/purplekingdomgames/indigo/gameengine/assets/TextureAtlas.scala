package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.util.Logger

object TextureAtlas {

  val MaxTextureSize: PowerOfTwo = PowerOfTwo._4096

  val supportedSizes: Set[PowerOfTwo] = PowerOfTwo.all

  def create(images: List[LoadedImageAsset]): TextureAtlas = {
    images.flatMap { i =>
      if(TextureAtlasFunctions.isTooBig(MaxTextureSize, i.data.width, i.data.height)) {
        // I think we'll still access assets through the asset collection, which can try for the atlas first and fallback
        Logger.info(s"Image ${i.name} is too large and will not be added to the texture atlas - may cause performance penalties")
        Nil
      } else List(i)
    }

    TextureAtlas()
  }

}

case class TextureAtlas()

case class AtlasQuadTree(textureSize: PowerOfTwo, leaf: AtlasSum)

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

}

sealed trait PowerOfTwo { val value: Int }
object PowerOfTwo {
  case object _1 extends PowerOfTwo { val value: Int = 1 }
  case object _2 extends PowerOfTwo { val value: Int = 2 }
  case object _4 extends PowerOfTwo { val value: Int = 4 }
  case object _8 extends PowerOfTwo { val value: Int = 8 }
  case object _16 extends PowerOfTwo { val value: Int = 16 }
  case object _32 extends PowerOfTwo { val value: Int = 32 }
  case object _64 extends PowerOfTwo { val value: Int = 64 }
  case object _128 extends PowerOfTwo { val value: Int = 128 }
  case object _256 extends PowerOfTwo { val value: Int = 256 }
  case object _512 extends PowerOfTwo { val value: Int = 512 }
  case object _1024 extends PowerOfTwo { val value: Int = 1024 }
  case object _2048 extends PowerOfTwo { val value: Int = 2048 }
  case object _4096 extends PowerOfTwo { val value: Int = 4096 }

  val Max: PowerOfTwo = _4096

  val all: Set[PowerOfTwo] = Set(_1, _2, _4, _8, _16, _32, _64, _128, _256, _512, _1024, _2048, _4096)

  def isValidPowerOfTwo(i: Int): Boolean = all.exists(p => p.value == i)

  def fromInt(i: Int): Option[PowerOfTwo] = all.find(_.value == i)

  def min(a: PowerOfTwo, b: PowerOfTwo): PowerOfTwo = if(a.value <= b.value) a else b
  def max(a: PowerOfTwo, b: PowerOfTwo): PowerOfTwo = if(a.value > b.value) a else b

}