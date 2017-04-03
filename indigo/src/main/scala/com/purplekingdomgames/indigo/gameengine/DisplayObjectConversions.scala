package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.renderer.{DisplayObject, SpriteSheetFrame, Vector2}

import scala.language.implicitConversions

object DisplayObjectConversions {

  private implicit def displayObjectToList(displayObject: DisplayObject): List[DisplayObject] = List(displayObject)

  def leafToDisplayObject[ViewEventDataType]: SceneGraphNodeLeafInternal[ViewEventDataType] => List[DisplayObject] = {
    case leaf: GraphicInternal[ViewEventDataType] =>
      DisplayObject(
        x = leaf.x,
        y = leaf.y,
        z = -leaf.depth.zIndex,
        width = leaf.crop.size.x,
        height = leaf.crop.size.y,
        imageRef = leaf.imageAssetRef,
        alpha = leaf.effects.alpha,
        tintR = leaf.effects.tint.r,
        tintG = leaf.effects.tint.g,
        tintB = leaf.effects.tint.b,
        flipHorizontal = leaf.effects.flip.horizontal,
        flipVertical = leaf.effects.flip.vertical,
        frame =
          if(leaf.bounds == leaf.crop) SpriteSheetFrame.defaultOffset
          else
            SpriteSheetFrame.calculateFrameOffset(
              imageSize = Vector2(leaf.bounds.size.x, leaf.bounds.size.y),
              frameSize = Vector2(leaf.crop.size.x, leaf.crop.size.y),
              framePosition = Vector2(leaf.crop.position.x, leaf.crop.position.y)
            )
      )

    case leaf: SpriteInternal[ViewEventDataType] =>
      DisplayObject(
        x = leaf.x,
        y = leaf.y,
        z = -leaf.depth.zIndex,
        width = leaf.bounds.size.x,
        height = leaf.bounds.size.y,
        imageRef = leaf.imageAssetRef,
        alpha = leaf.effects.alpha,
        tintR = leaf.effects.tint.r,
        tintG = leaf.effects.tint.g,
        tintB = leaf.effects.tint.b,
        flipHorizontal = leaf.effects.flip.horizontal,
        flipVertical = leaf.effects.flip.vertical,
        frame = SpriteSheetFrame.calculateFrameOffset(
          imageSize = Vector2(leaf.animations.spriteSheetSize.x, leaf.animations.spriteSheetSize.y),
          frameSize = Vector2(leaf.animations.currentFrame.bounds.size.x, leaf.animations.currentFrame.bounds.size.y),
          framePosition = Vector2(leaf.animations.currentFrame.bounds.position.x, leaf.animations.currentFrame.bounds.position.y)
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
        DisplayObjectConversions.textLineToDisplayObjects[ViewEventDataType](leaf)

      leaf.lines.foldLeft(0 -> List[DisplayObject]()) { (acc, textLine) =>
        (acc._1 + textLine.lineBounds.height, acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1))
      }._2

  }


  def textLineToDisplayObjects[ViewEventDataType](leaf: TextInternal[ViewEventDataType]): (TextLine, Int, Int) => List[DisplayObject] = (line, alignmentOffsetX, yOffset) =>
    zipWithCharDetails(line.text.toList, leaf.fontInfo).map { case (fontChar, xPosition) =>
      DisplayObject(
        x = leaf.position.x + xPosition + alignmentOffsetX,
        y = leaf.position.y + yOffset,
        z = leaf.depth.zIndex,
        width = fontChar.bounds.width,
        height = fontChar.bounds.height,
        imageRef = leaf.imageAssetRef,
        alpha = leaf.effects.alpha,
        tintR = leaf.effects.tint.r,
        tintG = leaf.effects.tint.g,
        tintB = leaf.effects.tint.b,
        flipHorizontal = leaf.effects.flip.horizontal,
        flipVertical = leaf.effects.flip.vertical,
        frame = SpriteSheetFrame.calculateFrameOffset(
          imageSize = Vector2(leaf.fontInfo.fontSpriteSheet.size.x, leaf.fontInfo.fontSpriteSheet.size.y),
          frameSize = Vector2(fontChar.bounds.width, fontChar.bounds.height),
          framePosition = Vector2(fontChar.bounds.x, fontChar.bounds.y)
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
