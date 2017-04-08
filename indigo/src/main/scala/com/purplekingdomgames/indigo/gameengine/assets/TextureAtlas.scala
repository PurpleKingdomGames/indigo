package com.purplekingdomgames.indigo.gameengine.assets

object TextureAtlas {

  // But how do we guarantee this is a PowerOf2 number in the type system. Need type literals
  val MaxTextureSize = 4096

  val supportedSizes: Set[Int] = TextureAtlasFunctions.generateSupportedSizes(MaxTextureSize)

  def create(images: List[LoadedImageAsset]): TextureAtlas = TextureAtlas()

}

case class TextureAtlas()

object TextureAtlasFunctions {

  // Another type fail, turns out sets aren't ordered?!?
  def generateSupportedSizes(max: Int): Set[Int] = {
    def rec(next: Int, acc: List[Int]): Set[Int] =
      next match {
        case 0 => acc.toSet
        case n => rec(n / 2, n :: acc)
      }

    rec(max, List.empty[Int])
  }

  /**
    * Type fails all over the place, no guarantee that this list is in the right order...
    */
  def pickPowerOfTwoSizeFor(supportedSizes: Set[Int], width: Int, height: Int): Int =
    supportedSizes
      .filter(s => s >= width && s >= height)
      .foldLeft(100000)(Math.min)

}