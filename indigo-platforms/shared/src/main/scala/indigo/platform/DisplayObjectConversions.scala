package indigo.platform

import indigo.shared.display.{DisplayObject, SpriteSheetFrame, DisplayClone, DisplayCloneBatch}
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
import indigo.shared.QuickCache

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import indigo.shared.display.DisplayEntity
import indigo.shared.scenegraph.Clone
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.display.DisplayClone
import indigo.shared.scenegraph.CloneTransformData
import indigo.shared.display.DisplayCloneBatchData

object DisplayObjectConversions {

  implicit private val stringCache: QuickCache[String]                           = QuickCache.empty
  implicit private val vector2Cache: QuickCache[Vector2]                         = QuickCache.empty
  implicit private val frameCache: QuickCache[SpriteSheetFrameCoordinateOffsets] = QuickCache.empty
  implicit private val doCache: QuickCache[DisplayObject]                        = QuickCache.empty
  implicit private val listDoCache: QuickCache[List[DisplayObject]]              = QuickCache.empty

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def lookupTextureOffset(assetMapping: AssetMapping, name: String): Vector2 =
    QuickCache("tex-offset-" + name) {
      assetMapping.mappings
        .find(p => p._1 === name)
        .map(_._2.offset)
        .map(pt => Vector2(pt.x.toDouble, pt.y.toDouble))
        .getOrElse {
          throw new Exception("Failed to find atlas offset for texture: " + name)
        }
    }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def lookupAtlasName(assetMapping: AssetMapping, name: String): String =
    QuickCache("atlas-" + name) {
      assetMapping.mappings.find(p => p._1 === name).map(_._2.atlasName).getOrElse {
        throw new Exception("Failed to find atlas name for texture: " + name)
      }
    }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  private def lookupAtlasSize(assetMapping: AssetMapping, name: String): Vector2 =
    QuickCache("atlas-size-" + name) {
      assetMapping.mappings.find(p => p._1 === name).map(_._2.atlasSize).getOrElse {
        throw new Exception("Failed to find atlas size for texture: " + name)
      }
    }

  private def cloneDataToDisplayEntity(id: String, cloneDepth: Double, data: CloneTransformData): DisplayClone =
    new DisplayClone(
      id = id.value,
      x = data.position.x.toDouble,
      y = data.position.y.toDouble,
      z = cloneDepth,
      rotation = data.rotation.value,
      scaleX = data.scale.x,
      scaleY = data.scale.y
    )

  private def cloneBatchDataToDisplayEntities(batch: CloneBatch): DisplayCloneBatch =
    new DisplayCloneBatch(
      id = batch.id.value,
      z = batch.depth.zIndex.toDouble,
      clones = batch.clones.map { td =>
        new DisplayCloneBatchData(
          x = batch.transform.position.x + td.position.x.toDouble,
          y = batch.transform.position.y + td.position.y.toDouble,
          rotation = batch.transform.rotation.value + td.rotation.value,
          scaleX = batch.transform.scale.x * td.scale.x,
          scaleY = batch.transform.scale.x * td.scale.y
        )
      }
    )

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var accDisplayObjects: ListBuffer[DisplayEntity] = new ListBuffer()

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def sceneNodesToDisplayObjects(sceneNodes: List[SceneGraphNode], gameTime: GameTime, assetMapping: AssetMapping, metrics: Metrics): ListBuffer[DisplayEntity] = {
    @tailrec
    def rec(remaining: List[SceneGraphNode]): ListBuffer[DisplayEntity] =
      remaining match {
        case Nil =>
          accDisplayObjects

        case (c: Clone) :: xs =>
          accDisplayObjects += cloneDataToDisplayEntity(c.id.value, c.depth.zIndex.toDouble, c.transform)
          rec(xs)

        case (c: CloneBatch) :: xs =>
          accDisplayObjects += cloneBatchDataToDisplayEntities(c)
          rec(xs)

        case (x: Group) :: xs =>
          val childNodes =
            x.children
              .map { c =>
                c.withDepth(c.depth + x.depth)
                  .transformBy(x.positionOffset, x.rotation, x.scale)
              }

          rec(childNodes ++ xs)

        case (x: Graphic) :: xs =>
          accDisplayObjects += graphicToDisplayObject(x, assetMapping)
          rec(xs)

        case (x: Sprite) :: xs =>
          AnimationsRegister.fetchFromCache(gameTime, x.bindingKey, x.animationsKey, metrics) match {
            case None =>
              IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${x.animationsKey}")
              rec(xs)

            case Some(anim) =>
              accDisplayObjects += spriteToDisplayObject(x, assetMapping, anim)
              rec(xs)
          }

        case (x: Text) :: xs =>
          val alignmentOffsetX: Rectangle => Int = lineBounds =>
            x.alignment match {
              case TextAlignment.Left => 0

              case TextAlignment.Center => -(lineBounds.size.x / 2)

              case TextAlignment.Right => -lineBounds.size.x
            }

          val converterFunc: (TextLine, Int, Int) => List[DisplayObject] =
            DisplayObjectConversions.textLineToDisplayObjects(x, assetMapping)

          val letters =
            x.lines
              .foldLeft(0 -> List[DisplayObject]()) { (acc, textLine) =>
                (acc._1 + textLine.lineBounds.height, acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1))
              }
              ._2

          accDisplayObjects ++= letters
          rec(xs)
      }

    accDisplayObjects = new ListBuffer()
    rec(sceneNodes)
  }

  def graphicToDisplayObject(leaf: Graphic, assetMapping: AssetMapping): DisplayObject =
    DisplayObject(
      x = leaf.x,
      y = leaf.y,
      z = leaf.depth.zIndex,
      width = leaf.crop.size.x,
      height = leaf.crop.size.y,
      rotation = leaf.rotation.value,
      scaleX = leaf.scale.x,
      scaleY = leaf.scale.y,
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

  def spriteToDisplayObject(leaf: Sprite, assetMapping: AssetMapping, anim: Animation): DisplayObject =
    DisplayObject(
      x = leaf.x,
      y = leaf.y,
      z = leaf.depth.zIndex,
      width = leaf.bounds.size.x,
      height = leaf.bounds.size.y,
      rotation = leaf.rotation.value,
      scaleX = leaf.scale.x,
      scaleY = leaf.scale.y,
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
    (line, alignmentOffsetX, yOffset) => {
      val lineHash: String =
        leaf.fontKey.key + ":" + line.hash + ":" + alignmentOffsetX.toString() + ":" + yOffset.toString()

      QuickCache(lineHash) {
        FontRegister
          .findByFontKey(leaf.fontKey)
          .map { fontInfo =>
            zipWithCharDetails(line.text.toList, fontInfo).toList.map {
              case (fontChar, xPosition) =>
                QuickCache(lineHash + ":" + leaf.fontKey.key + ":" + fontChar.character.toString() + ":" + fontChar.bounds.hash + ":" + xPosition.toString()) {
                  DisplayObject(
                    x = leaf.position.x + xPosition + alignmentOffsetX,
                    y = leaf.position.y + yOffset,
                    z = leaf.depth.zIndex,
                    width = fontChar.bounds.width,
                    height = fontChar.bounds.height,
                    rotation = leaf.rotation.value,
                    scaleX = leaf.scale.x,
                    scaleY = leaf.scale.y,
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
          }
          .getOrElse {
            IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${leaf.fontKey}")
            Nil
          }
      }
    }
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var accCharDetails: ListBuffer[(FontChar, Int)] = new ListBuffer()

  private def zipWithCharDetails(charList: List[Char], fontInfo: FontInfo): ListBuffer[(FontChar, Int)] = {
    @tailrec
    def rec(remaining: List[(Char, FontChar)], nextX: Int): ListBuffer[(FontChar, Int)] =
      remaining match {
        case Nil =>
          accCharDetails

        case x :: xs =>
          (x._2, nextX) +=: accCharDetails
          rec(xs, nextX + x._2.bounds.width)
      }

    accCharDetails = new ListBuffer()
    rec(charList.map(c => (c, fontInfo.findByCharacter(c))), 0)
  }

}
