package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.renderer.{AssetMapping, DisplayObject, SpriteSheetFrame, Vector2}
import com.purplekingdomgames.indigo.util.Logger

import scala.language.implicitConversions

object DisplayObjectConversions {

  private implicit def displayObjectToList(displayObject: DisplayObject): List[DisplayObject] = List(displayObject)

  private val lookupTextureOffset: (AssetMapping, String) => Vector2 = (assetMapping, name) =>
    assetMapping.mappings.find(p => p._1 == name).map(_._2.offset).map(pt => Vector2(pt.x, pt.y)).getOrElse {
      Logger.info("Failed to find atlas offset for texture: " + name)
      Vector2.zero
    }

  private val lookupAtlasName: (AssetMapping, String) => String = (assetMapping, name) =>
    assetMapping.mappings.find(p => p._1 == name).map(_._2.atlasName).getOrElse {
      Logger.info("Failed to find atlas name for texture: " + name)
      ""
    }

  def leafToDisplayObject[ViewEventDataType](assetMapping: AssetMapping): SceneGraphNodeLeafInternal[ViewEventDataType] => List[DisplayObject] = {
    case leaf: GraphicInternal[ViewEventDataType] =>
      DisplayObject(
        x = leaf.x,
        y = leaf.y,
        z = -leaf.depth.zIndex,
        width = leaf.crop.size.x,
        height = leaf.crop.size.y,
        imageRef = lookupAtlasName(assetMapping, leaf.imageAssetRef),
        alpha = leaf.effects.alpha,
        tintR = leaf.effects.tint.r,
        tintG = leaf.effects.tint.g,
        tintB = leaf.effects.tint.b,
        flipHorizontal = leaf.effects.flip.horizontal,
        flipVertical = leaf.effects.flip.vertical,
        frame =
          SpriteSheetFrame.calculateFrameOffset(
            imageSize = Vector2(leaf.bounds.size.x, leaf.bounds.size.y),
            frameSize = Vector2(leaf.crop.size.x, leaf.crop.size.y),
            framePosition = Vector2(leaf.crop.position.x, leaf.crop.position.y),
            textureOffset = lookupTextureOffset(assetMapping, leaf.imageAssetRef)
          )
      )

    case leaf: SpriteInternal[ViewEventDataType] =>
      DisplayObject(
        x = leaf.x,
        y = leaf.y,
        z = -leaf.depth.zIndex,
        width = leaf.bounds.size.x,
        height = leaf.bounds.size.y,
        imageRef = lookupAtlasName(assetMapping, leaf.imageAssetRef),
        alpha = leaf.effects.alpha,
        tintR = leaf.effects.tint.r,
        tintG = leaf.effects.tint.g,
        tintB = leaf.effects.tint.b,
        flipHorizontal = leaf.effects.flip.horizontal,
        flipVertical = leaf.effects.flip.vertical,
        frame = SpriteSheetFrame.calculateFrameOffset(
          imageSize = Vector2(leaf.animations.spriteSheetSize.x, leaf.animations.spriteSheetSize.y),
          frameSize = Vector2(leaf.animations.currentFrame.bounds.size.x, leaf.animations.currentFrame.bounds.size.y),
          framePosition = Vector2(leaf.animations.currentFrame.bounds.position.x, leaf.animations.currentFrame.bounds.position.y),
          textureOffset = lookupTextureOffset(assetMapping, leaf.imageAssetRef)
        )
      )

    case leaf: TextInternal[ViewEventDataType] =>

      val alignmentOffsetX: Rectangle => Int = lineBounds =>
        leaf.alignment match {
          case AlignLeft => 0

          case AlignCenter => -(lineBounds.size.x / 2)

          case AlignRight => -lineBounds.size.x
        }

      val converterFunc: (TextLine, Int, Int) => List[DisplayObject] =
        DisplayObjectConversions.textLineToDisplayObjects[ViewEventDataType](leaf, assetMapping)

      leaf.lines.foldLeft(0 -> List[DisplayObject]()) { (acc, textLine) =>
        (acc._1 + textLine.lineBounds.height, acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1))
      }._2

  }


  def textLineToDisplayObjects[ViewEventDataType](leaf: TextInternal[ViewEventDataType], assetMapping: AssetMapping): (TextLine, Int, Int) => List[DisplayObject] = (line, alignmentOffsetX, yOffset) =>
    zipWithCharDetails(line.text.toList, leaf.fontInfo).map { case (fontChar, xPosition) =>
      DisplayObject(
        x = leaf.position.x + xPosition + alignmentOffsetX,
        y = leaf.position.y + yOffset,
        z = leaf.depth.zIndex,
        width = fontChar.bounds.width,
        height = fontChar.bounds.height,
        imageRef = lookupAtlasName(assetMapping, leaf.imageAssetRef),
        alpha = leaf.effects.alpha,
        tintR = leaf.effects.tint.r,
        tintG = leaf.effects.tint.g,
        tintB = leaf.effects.tint.b,
        flipHorizontal = leaf.effects.flip.horizontal,
        flipVertical = leaf.effects.flip.vertical,
        frame = SpriteSheetFrame.calculateFrameOffset(
          imageSize = Vector2(leaf.fontInfo.fontSpriteSheet.size.x, leaf.fontInfo.fontSpriteSheet.size.y),
          frameSize = Vector2(fontChar.bounds.width, fontChar.bounds.height),
          framePosition = Vector2(fontChar.bounds.x, fontChar.bounds.y),
          textureOffset = lookupTextureOffset(assetMapping, leaf.imageAssetRef)
        )
      )
    }

  private def zipWithCharDetails(charList: List[Char], fontInfo: FontInfo): List[(FontChar, Int)] = {
    def rec(remaining: List[(Char, FontChar)], nextX: Int, acc: List[(FontChar, Int)]): List[(FontChar, Int)] =
      remaining match {
        case Nil => acc
        case x :: xs => rec(xs, nextX + x._2.bounds.width, (x._2, nextX) :: acc)
      }

    rec(charList.map(c => (c, fontInfo.findByCharacter(c))), 0, Nil)
  }

}
