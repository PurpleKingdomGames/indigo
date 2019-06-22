package indigo.gameengine

import indigo.shared.display.{DisplayObject, SpriteSheetFrame}
import indigo.shared.datatypes.{FontInfo, Rectangle, TextAlignment, FontChar}
import indigo.shared.animation.Animation
import indigo.shared.display.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.shared.IndigoLogger
import indigo.shared.metrics.Metrics
import indigo.shared.time.GameTime
import indigo.shared.datatypes.Vector2
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.platform.AssetMapping
import indigo.shared.scenegraph.{Graphic, Sprite, Text, TextLine}
import indigo.shared.EqualTo._
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.Group

import scala.annotation.tailrec

object DisplayObjectConversions {

  implicit private val stringCache: QuickCache[String]                           = QuickCache.empty
  implicit private val vector2Cache: QuickCache[Vector2]                         = QuickCache.empty
  implicit private val frameCache: QuickCache[SpriteSheetFrameCoordinateOffsets] = QuickCache.empty

  private val lookupTextureOffset: (AssetMapping, String) => Vector2 = (assetMapping, name) =>
    QuickCache("tex-offset-" + name) {
      assetMapping.mappings
        .find(p => p._1 === name)
        .map(_._2.offset)
        .map(pt => Vector2(pt.x.toDouble, pt.y.toDouble))
        .getOrElse {
          IndigoLogger.info("Failed to find atlas offset for texture: " + name)
          Vector2.zero
        }
    }

  private val lookupAtlasName: (AssetMapping, String) => String = (assetMapping, name) =>
    QuickCache("atlas-" + name) {
      assetMapping.mappings.find(p => p._1 === name).map(_._2.atlasName).getOrElse {
        IndigoLogger.info("Failed to find atlas name for texture: " + name)
        ""
      }
    }

  private def lookupAtlasSize(assetMapping: AssetMapping, name: String): Vector2 =
    QuickCache("atlas-size-" + name) {
      assetMapping.mappings.find(p => p._1 === name).map(_._2.atlasSize).getOrElse {
        IndigoLogger.info("Failed to find atlas size for texture: " + name)
        Vector2.one
      }
    }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def leafToDisplayObject(gameTime: GameTime, assetMapping: AssetMapping, metrics: Metrics): SceneGraphNode => List[DisplayObject] = {
    case g: Group =>
      g.children
        .map(c => c.withDepth(c.depth + g.depth).moveBy(g.positionOffset))
        .flatMap(leafToDisplayObject(gameTime, assetMapping, metrics))

    case leaf: Graphic =>
      List(graphicToDisplayObject(leaf, assetMapping))

    case leaf: Sprite =>
      AnimationsRegister
        .fetchFromCache(gameTime, leaf.bindingKey, leaf.animationsKey, metrics)
        .map { anim =>
          List(spriteToDisplayObjects(leaf, assetMapping, anim))
        }
        .getOrElse {
          IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${leaf.animationsKey}")
          Nil
        }

    case leaf: Text =>
      val alignmentOffsetX: Rectangle => Int = lineBounds =>
        leaf.alignment match {
          case TextAlignment.Left => 0

          case TextAlignment.Center => -(lineBounds.size.x / 2)

          case TextAlignment.Right => -lineBounds.size.x
        }

      val converterFunc: (TextLine, Int, Int) => List[DisplayObject] =
        DisplayObjectConversions.textLineToDisplayObjects(leaf, assetMapping)

      leaf.lines
        .foldLeft(0 -> List[DisplayObject]()) { (acc, textLine) =>
          (acc._1 + textLine.lineBounds.height, acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1))
        }
        ._2

  }

  def graphicToDisplayObject(leaf: Graphic, assetMapping: AssetMapping): DisplayObject =
    DisplayObject(
      x = leaf.x,
      y = leaf.y,
      z = leaf.depth.zIndex,
      width = leaf.crop.size.x,
      height = leaf.crop.size.y,
      imageRef = lookupAtlasName(assetMapping, leaf.imageAssetRef),
      alpha = leaf.effects.alpha,
      tintR = leaf.effects.tint.r,
      tintG = leaf.effects.tint.g,
      tintB = leaf.effects.tint.b,
      flipHorizontal = leaf.effects.flip.horizontal,
      flipVertical = leaf.effects.flip.vertical,
      frame = QuickCache(s"${leaf.crop.hash}_${leaf.imageAssetRef}") {
        SpriteSheetFrame.calculateFrameOffset(
          imageSize = lookupAtlasSize(assetMapping, leaf.imageAssetRef),
          frameSize = Vector2(leaf.crop.size.x.toDouble, leaf.crop.size.y.toDouble),
          framePosition = Vector2(leaf.crop.position.x.toDouble, leaf.crop.position.y.toDouble),
          textureOffset = lookupTextureOffset(assetMapping, leaf.imageAssetRef)
        )
      }
    )

  def spriteToDisplayObjects(leaf: Sprite, assetMapping: AssetMapping, anim: Animation): DisplayObject =
    DisplayObject(
      x = leaf.x,
      y = leaf.y,
      z = leaf.depth.zIndex,
      width = leaf.bounds.size.x,
      height = leaf.bounds.size.y,
      imageRef = lookupAtlasName(assetMapping, anim.imageAssetRef.ref),
      alpha = leaf.effects.alpha,
      tintR = leaf.effects.tint.r,
      tintG = leaf.effects.tint.g,
      tintB = leaf.effects.tint.b,
      flipHorizontal = leaf.effects.flip.horizontal,
      flipVertical = leaf.effects.flip.vertical,
      frame = QuickCache(anim.frameHash) {
        SpriteSheetFrame.calculateFrameOffset(
          imageSize = lookupAtlasSize(assetMapping, anim.imageAssetRef.ref),
          frameSize = Vector2(anim.currentFrame.bounds.size.x.toDouble, anim.currentFrame.bounds.size.y.toDouble),
          framePosition = Vector2(anim.currentFrame.bounds.position.x.toDouble, anim.currentFrame.bounds.position.y.toDouble),
          textureOffset = lookupTextureOffset(assetMapping, anim.imageAssetRef.ref)
        )
      }
    )

  def textLineToDisplayObjects(leaf: Text, assetMapping: AssetMapping): (TextLine, Int, Int) => List[DisplayObject] =
    (line, alignmentOffsetX, yOffset) =>
      FontRegister
        .findByFontKey(leaf.fontKey)
        .map { fontInfo =>
          zipWithCharDetails(line.text.toList, fontInfo).map {
            case (fontChar, xPosition) =>
              DisplayObject(
                x = leaf.position.x + xPosition + alignmentOffsetX,
                y = leaf.position.y + yOffset,
                z = leaf.depth.zIndex,
                width = fontChar.bounds.width,
                height = fontChar.bounds.height,
                imageRef = lookupAtlasName(assetMapping, fontInfo.fontSpriteSheet.imageAssetRef),
                alpha = leaf.effects.alpha,
                tintR = leaf.effects.tint.r,
                tintG = leaf.effects.tint.g,
                tintB = leaf.effects.tint.b,
                flipHorizontal = leaf.effects.flip.horizontal,
                flipVertical = leaf.effects.flip.vertical,
                frame = QuickCache(fontChar.bounds.hash + "_" + fontInfo.fontSpriteSheet.imageAssetRef) {
                  SpriteSheetFrame.calculateFrameOffset(
                    imageSize = lookupAtlasSize(assetMapping, fontInfo.fontSpriteSheet.imageAssetRef),
                    frameSize = Vector2(fontChar.bounds.width.toDouble, fontChar.bounds.height.toDouble),
                    framePosition = Vector2(fontChar.bounds.x.toDouble, fontChar.bounds.y.toDouble),
                    textureOffset = lookupTextureOffset(assetMapping, fontInfo.fontSpriteSheet.imageAssetRef)
                  )
                }
              )
          }
        }
        .getOrElse {
          IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${leaf.fontKey}")
          Nil
        }

  private def zipWithCharDetails(charList: List[Char], fontInfo: FontInfo): List[(FontChar, Int)] = {
    @tailrec
    def rec(remaining: List[(Char, FontChar)], nextX: Int, acc: List[(FontChar, Int)]): List[(FontChar, Int)] =
      remaining match {
        case Nil     => acc
        case x :: xs => rec(xs, nextX + x._2.bounds.width, (x._2, nextX) :: acc)
      }

    rec(charList.map(c => (c, fontInfo.findByCharacter(c))), 0, Nil)
  }

}
