package indigo.shared.platform

import indigo.shared.display.{DisplayObject, SpriteSheetFrame, DisplayCloneBatch}
import indigo.shared.datatypes.{FontInfo, Rectangle, TextAlignment, FontChar}
import indigo.shared.display.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.shared.IndigoLogger
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
import indigo.shared.datatypes.Material
import indigo.shared.display.DisplayEffects
import indigo.shared.datatypes.Texture
import indigo.shared.BoundaryLocator
import indigo.shared.animation.AnimationRef

final class DisplayObjectConversions(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
) {

  implicit private val stringCache: QuickCache[String]                           = QuickCache.empty
  implicit private val vector2Cache: QuickCache[Vector2]                         = QuickCache.empty
  implicit private val frameCache: QuickCache[SpriteSheetFrameCoordinateOffsets] = QuickCache.empty
  implicit private val listDoCache: QuickCache[List[DisplayObject]]              = QuickCache.empty
  implicit private val cloneBatchCache: QuickCache[DisplayCloneBatch]            = QuickCache.empty
  implicit private val effectsCache: QuickCache[DisplayEffects]                  = QuickCache.empty
  implicit private val textureAmountsCache: QuickCache[(Vector2, Double)]        = QuickCache.empty

  def purgeCaches(): Unit = {
    stringCache.purgeAllNow()
    vector2Cache.purgeAllNow()
    frameCache.purgeAllNow()
    listDoCache.purgeAllNow()
    cloneBatchCache.purgeAllNow()
    effectsCache.purgeAllNow()
    textureAmountsCache.purgeAllNow()
  }

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

  private def cloneDataToDisplayEntity(id: String, cloneDepth: Float, data: CloneTransformData): DisplayClone =
    new DisplayClone(
      id = id.value,
      x = data.position.x.toFloat,
      y = data.position.y.toFloat,
      z = cloneDepth,
      rotation = data.rotation.value.toFloat,
      scaleX = data.scale.x.toFloat,
      scaleY = data.scale.y.toFloat,
      alpha = data.alpha.toFloat,
      flipHorizontal = if (data.flipHorizontal) -1f else 1f,
      flipVertical = if (data.flipVertical) 1f else -1f
    )

  private def cloneBatchDataToDisplayEntities(batch: CloneBatch): DisplayCloneBatch = {
    def convert(): DisplayCloneBatch =
      new DisplayCloneBatch(
        id = batch.id.value,
        z = batch.depth.zIndex.toFloat,
        clones = batch.clones.map { td =>
          new DisplayCloneBatchData(
            x = batch.transform.position.x + td.position.x.toFloat,
            y = batch.transform.position.y + td.position.y.toFloat,
            rotation = batch.transform.rotation.value.toFloat + td.rotation.value.toFloat,
            scaleX = batch.transform.scale.x.toFloat * td.scale.x.toFloat,
            scaleY = batch.transform.scale.x.toFloat * td.scale.y.toFloat,
            alpha = batch.transform.alpha.toFloat,
            flipHorizontal = if (batch.transform.flipHorizontal) -1f else 1f,
            flipVertical = if (batch.transform.flipVertical) 1f else -1f
          )
        }
      )
    batch.staticBatchId match {
      case None =>
        convert()

      case Some(bindingKey) =>
        QuickCache(bindingKey.value) {
          convert()
        }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private val accDisplayObjects: ListBuffer[DisplayEntity] = new ListBuffer()

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def sceneNodesToDisplayObjects(
      sceneNodes: List[SceneGraphNode],
      gameTime: GameTime,
      assetMapping: AssetMapping
  ): ListBuffer[DisplayEntity] = {
    @tailrec
    def rec(remaining: List[SceneGraphNode]): ListBuffer[DisplayEntity] =
      remaining match {
        case Nil =>
          accDisplayObjects

        case (c: Clone) :: xs =>
          accDisplayObjects += cloneDataToDisplayEntity(c.id.value, c.depth.zIndex.toFloat, c.transform)
          rec(xs)

        case (c: CloneBatch) :: xs =>
          accDisplayObjects += cloneBatchDataToDisplayEntities(c)
          rec(xs)

        case (x: Group) :: xs =>
          val childNodes =
            x.children
              .map { c =>
                c.withDepth(c.depth + x.depth)
                  .moveBy(x.positionOffset)
              }

          rec(childNodes ++ xs)

        case (x: Graphic) :: xs =>
          accDisplayObjects += graphicToDisplayObject(x, assetMapping)
          rec(xs)

        case (x: Sprite) :: xs =>
          animationsRegister.fetchAnimationForSprite(gameTime, x.bindingKey, x.animationKey, x.animationActions) match {
            case None =>
              IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${x.animationKey.toString()}")
              rec(xs)

            case Some(anim) =>
              accDisplayObjects += spriteToDisplayObject(boundaryLocator, x, assetMapping, anim)
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
            fontRegister
              .findByFontKey(x.fontKey)
              .map { fontInfo =>
                textLineToDisplayObjects(x, assetMapping, fontInfo)
              }
              .getOrElse { (_, _, _) =>
                IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${x.fontKey.toString()}")
                Nil
              }

          val letters =
            boundaryLocator
              .textAsLinesWithBounds(x.text, x.fontKey)
              .foldLeft(0 -> List[DisplayObject]()) { (acc, textLine) =>
                (acc._1 + textLine.lineBounds.height, acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1))
              }
              ._2

          accDisplayObjects ++= letters
          rec(xs)
      }

    accDisplayObjects.clear()

    rec(sceneNodes)
  }

  def materialToEmissiveValues(assetMapping: AssetMapping, material: Material): (Vector2, Double) =
    QuickCache(material.hash + "_emissive") {
      material match {
        case _: Material.Textured =>
          (Vector2.zero, 0.0d)

        case t: Material.Lit =>
          optionalAssetToValues(assetMapping, t.emissive)
      }
    }

  def materialToNormalValues(assetMapping: AssetMapping, material: Material): (Vector2, Double) =
    QuickCache(material.hash + "_normal") {
      material match {
        case _: Material.Textured =>
          (Vector2.zero, 0.0d)

        case t: Material.Lit =>
          optionalAssetToValues(assetMapping, t.normal)
      }
    }

  def materialToSpecularValues(assetMapping: AssetMapping, material: Material): (Vector2, Double) =
    QuickCache(material.hash + "_specular") {
      material match {
        case _: Material.Textured =>
          (Vector2.zero, 0.0d)

        case t: Material.Lit =>
          optionalAssetToValues(assetMapping, t.specular)
      }
    }

  def optionalAssetToValues(assetMapping: AssetMapping, maybeAssetName: Option[Texture]): (Vector2, Double) =
    maybeAssetName
      .map { t =>
        (lookupTextureOffset(assetMapping, t.assetName.value), Math.min(1.0d, Math.max(0.0d, t.amount)))
      }
      .getOrElse((Vector2.zero, 0.0d))

  def graphicToDisplayObject(leaf: Graphic, assetMapping: AssetMapping): DisplayObject = {
    val materialName = leaf.material.default.value

    val albedoAmount                     = 1.0f
    val (emissiveOffset, emissiveAmount) = materialToEmissiveValues(assetMapping, leaf.material)
    val (normalOffset, normalAmount)     = materialToNormalValues(assetMapping, leaf.material)
    val (specularOffset, specularAmount) = materialToSpecularValues(assetMapping, leaf.material)

    val frameInfo =
      QuickCache(s"${leaf.crop.hash}_${leaf.material.hash}") {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = lookupAtlasSize(assetMapping, materialName),
          frameCrop = leaf.crop,
          textureOffset = lookupTextureOffset(assetMapping, materialName)
        )
      }

    val effectsValues =
      QuickCache(leaf.effects.hash) {
        DisplayEffects.fromEffects(leaf.effects)
      }

    DisplayObject(
      x = leaf.x,
      y = leaf.y,
      z = leaf.depth.zIndex,
      width = leaf.crop.size.x,
      height = leaf.crop.size.y,
      rotation = leaf.rotation.value.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      atlasName = lookupAtlasName(assetMapping, materialName),
      frame = frameInfo,
      albedoAmount = albedoAmount,
      emissiveOffset = frameInfo.offsetToCoords(emissiveOffset),
      emissiveAmount = emissiveAmount.toFloat,
      normalOffset = frameInfo.offsetToCoords(normalOffset),
      normalAmount = normalAmount.toFloat,
      specularOffset = frameInfo.offsetToCoords(specularOffset),
      specularAmount = specularAmount.toFloat,
      isLit = if (leaf.material.isLit) 1.0f else 0.0f,
      refX = leaf.ref.x,
      refY = leaf.ref.y,
      effects = effectsValues
    )
  }

  def spriteToDisplayObject(boundaryLocator: BoundaryLocator, leaf: Sprite, assetMapping: AssetMapping, anim: AnimationRef): DisplayObject = {
    val material = anim.currentFrame.frameMaterial.getOrElse(anim.material)

    val materialName = material.default.value

    val albedoAmount                     = 1.0f
    val (emissiveOffset, emissiveAmount) = materialToEmissiveValues(assetMapping, material)
    val (normalOffset, normalAmount)     = materialToNormalValues(assetMapping, material)
    val (specularOffset, specularAmount) = materialToSpecularValues(assetMapping, material)

    val frameInfo =
      QuickCache(anim.frameHash) {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = lookupAtlasSize(assetMapping, materialName),
          frameCrop = anim.currentFrame.crop,
          textureOffset = lookupTextureOffset(assetMapping, materialName)
        )
      }

    val effectsValues =
      QuickCache(leaf.effects.hash) {
        DisplayEffects.fromEffects(leaf.effects)
      }

    DisplayObject(
      x = leaf.x,
      y = leaf.y,
      z = leaf.depth.zIndex,
      width = leaf.bounds(boundaryLocator).size.x,
      height = leaf.bounds(boundaryLocator).size.y,
      rotation = leaf.rotation.value.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      atlasName = lookupAtlasName(assetMapping, materialName),
      frame = frameInfo,
      albedoAmount = albedoAmount,
      emissiveOffset = frameInfo.offsetToCoords(emissiveOffset),
      emissiveAmount = emissiveAmount.toFloat,
      normalOffset = frameInfo.offsetToCoords(normalOffset),
      normalAmount = normalAmount.toFloat,
      specularOffset = frameInfo.offsetToCoords(specularOffset),
      specularAmount = specularAmount.toFloat,
      isLit = if (material.isLit) 1.0f else 0.0f,
      refX = leaf.ref.x,
      refY = leaf.ref.y,
      effects = effectsValues
    )
  }

  def textLineToDisplayObjects(leaf: Text, assetMapping: AssetMapping, fontInfo: FontInfo): (TextLine, Int, Int) => List[DisplayObject] =
    (line, alignmentOffsetX, yOffset) => {

      val lineHash: String =
        leaf.fontKey.key +
          ":" + line.hash +
          ":" + alignmentOffsetX.toString() +
          ":" + yOffset.toString() +
          ":" + leaf.position.hash +
          ":" + leaf.rotation.hash +
          ":" + leaf.scale.hash +
          ":" + fontInfo.fontSpriteSheet.material.hash +
          ":" + leaf.effects.hash

      val materialName = fontInfo.fontSpriteSheet.material.default.value

      val albedoAmount                     = 1.0f
      val (emissiveOffset, emissiveAmount) = materialToEmissiveValues(assetMapping, fontInfo.fontSpriteSheet.material)
      val (normalOffset, normalAmount)     = materialToNormalValues(assetMapping, fontInfo.fontSpriteSheet.material)
      val (specularOffset, specularAmount) = materialToSpecularValues(assetMapping, fontInfo.fontSpriteSheet.material)

      val effectsValues =
        QuickCache(leaf.effects.hash) {
          DisplayEffects.fromEffects(leaf.effects)
        }

      QuickCache(lineHash) {
        zipWithCharDetails(line.text.toList, fontInfo).toList.map {
          case (fontChar, xPosition) =>
            val frameInfo =
              QuickCache(fontChar.bounds.hash + "_" + fontInfo.fontSpriteSheet.material.hash) {
                SpriteSheetFrame.calculateFrameOffset(
                  atlasSize = lookupAtlasSize(assetMapping, materialName),
                  frameCrop = fontChar.bounds,
                  textureOffset = lookupTextureOffset(assetMapping, materialName)
                )
              }

            DisplayObject(
              x = leaf.position.x + xPosition + alignmentOffsetX,
              y = leaf.position.y + yOffset,
              z = leaf.depth.zIndex,
              width = fontChar.bounds.width,
              height = fontChar.bounds.height,
              rotation = leaf.rotation.value.toFloat,
              scaleX = leaf.scale.x.toFloat,
              scaleY = leaf.scale.y.toFloat,
              atlasName = lookupAtlasName(assetMapping, materialName),
              frame = frameInfo,
              albedoAmount = albedoAmount,
              emissiveOffset = frameInfo.offsetToCoords(emissiveOffset),
              emissiveAmount = emissiveAmount.toFloat,
              normalOffset = frameInfo.offsetToCoords(normalOffset),
              normalAmount = normalAmount.toFloat,
              specularOffset = frameInfo.offsetToCoords(specularOffset),
              specularAmount = specularAmount.toFloat,
              isLit = if (fontInfo.fontSpriteSheet.material.isLit) 1.0f else 0.0f,
              refX = leaf.ref.x,
              refY = leaf.ref.y,
              effects = effectsValues
            )
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
