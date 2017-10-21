package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.renderer.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import com.purplekingdomgames.indigo.renderer.{AssetMapping, DisplayObject, SpriteSheetFrame, Vector2}
import com.purplekingdomgames.indigo.util.Logger

import scala.collection.mutable

object DisplayObjectConversions {

  private val lookupTextureOffsetCache: mutable.Map[String, Vector2] = mutable.Map.empty[String, Vector2]
  private val lookupAtlasNameCache: mutable.Map[String, String] = mutable.Map.empty[String, String]
  private val lookupAtlasSizeCache: mutable.Map[String, Vector2] = mutable.Map.empty[String, Vector2]
  private val frameOffsetsCache: mutable.Map[String, SpriteSheetFrameCoordinateOffsets] = mutable.Map.empty[String, SpriteSheetFrameCoordinateOffsets]

  private val lookupTextureOffset: (AssetMapping, String) => Vector2 = (assetMapping, name) =>
    lookupTextureOffsetCache.getOrElseUpdate(name, {
      assetMapping.mappings.find(p => p._1 == name).map(_._2.offset).map(pt => Vector2(pt.x.toDouble, pt.y.toDouble)).getOrElse {
        Logger.info("Failed to find atlas offset for texture: " + name)
        Vector2.zero
      }
    })

  private val lookupAtlasName: (AssetMapping, String) => String = (assetMapping, name) =>
    lookupAtlasNameCache.getOrElseUpdate(name, {
      assetMapping.mappings.find(p => p._1 == name).map(_._2.atlasName).getOrElse {
        Logger.info("Failed to find atlas name for texture: " + name)
        ""
      }
    })

  private val lookupAtlasSize: (AssetMapping, String) => Vector2 = (assetMapping, name) =>
    lookupAtlasSizeCache.getOrElseUpdate(name, {
      assetMapping.mappings.find(p => p._1 == name).map(_._2.atlasSize).getOrElse {
        Logger.info("Failed to find atlas size for texture: " + name)
        Vector2.one
      }
    })

  def leafToDisplayObject[ViewEventDataType](assetMapping: AssetMapping): SceneGraphNodeLeaf[ViewEventDataType] => List[DisplayObject] = {
    case leaf: Graphic[ViewEventDataType] =>
      List(
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
            frameOffsetsCache.getOrElseUpdate(leaf.frameHash, {
              SpriteSheetFrame.calculateFrameOffset(
                imageSize = lookupAtlasSize(assetMapping, leaf.imageAssetRef),
                frameSize = Vector2(leaf.crop.size.x.toDouble, leaf.crop.size.y.toDouble),
                framePosition = Vector2(leaf.crop.position.x.toDouble, leaf.crop.position.y.toDouble),
                textureOffset = lookupTextureOffset(assetMapping, leaf.imageAssetRef)
              )
            })
        )
      )

    case leaf: Sprite[ViewEventDataType] =>
      List(
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
          frame =
            frameOffsetsCache.getOrElseUpdate(leaf.frameHash, {
              SpriteSheetFrame.calculateFrameOffset(
                imageSize = lookupAtlasSize(assetMapping, leaf.imageAssetRef),
                frameSize = Vector2(leaf.animations.currentFrame.bounds.size.x.toDouble, leaf.animations.currentFrame.bounds.size.y.toDouble),
                framePosition = Vector2(leaf.animations.currentFrame.bounds.position.x.toDouble, leaf.animations.currentFrame.bounds.position.y.toDouble),
                textureOffset = lookupTextureOffset(assetMapping, leaf.imageAssetRef)
              )
            })
        )
      )

    case leaf: Text[ViewEventDataType] =>

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

  def textLineToDisplayObjects[ViewEventDataType](leaf: Text[ViewEventDataType], assetMapping: AssetMapping): (TextLine, Int, Int) => List[DisplayObject] = (line, alignmentOffsetX, yOffset) =>
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
        frame =
          frameOffsetsCache.getOrElseUpdate(fontChar.bounds.hash + "_" + leaf.imageAssetRef, {
            SpriteSheetFrame.calculateFrameOffset(
              imageSize = lookupAtlasSize(assetMapping, leaf.imageAssetRef),
              frameSize = Vector2(fontChar.bounds.width.toDouble, fontChar.bounds.height.toDouble),
              framePosition = Vector2(fontChar.bounds.x.toDouble, fontChar.bounds.y.toDouble),
              textureOffset = lookupTextureOffset(assetMapping, leaf.imageAssetRef)
            )
          })
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
