package indigo.shared.platform

import indigo.shared.display.{DisplayObject, SpriteSheetFrame, DisplayCloneBatch}
import indigo.shared.datatypes.{FontInfo, Rectangle, TextAlignment, FontChar}
import indigo.shared.display.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.shared.IndigoLogger
import indigo.shared.time.GameTime
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Vector3
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.platform.AssetMapping
import indigo.shared.scenegraph.{Graphic, Sprite, Text, TextLine}
import indigo.shared.scenegraph.Shape

import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Transformer
import indigo.shared.QuickCache

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import indigo.shared.display.DisplayEntity
import indigo.shared.scenegraph.Clone
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.display.DisplayClone
import indigo.shared.scenegraph.CloneTransformData
import indigo.shared.display.DisplayCloneBatchData
import indigo.shared.materials.GLSLShader
import indigo.shared.display.DisplayEffects
import indigo.shared.BoundaryLocator
import indigo.shared.animation.AnimationRef
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.assets.AssetName

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
  implicit private val uniformsCache: QuickCache[Array[Float]]                   = QuickCache.empty

  def purgeCaches(): Unit = {
    stringCache.purgeAllNow()
    vector2Cache.purgeAllNow()
    frameCache.purgeAllNow()
    listDoCache.purgeAllNow()
    cloneBatchCache.purgeAllNow()
    effectsCache.purgeAllNow()
    textureAmountsCache.purgeAllNow()
    uniformsCache.purgeAllNow()
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def lookupTextureOffset(assetMapping: AssetMapping, name: String): Vector2 =
    QuickCache("tex-offset-" + name) {
      assetMapping.mappings
        .find(p => p._1 == name)
        .map(_._2.offset)
        .map(pt => Vector2(pt.x.toDouble, pt.y.toDouble))
        .getOrElse {
          throw new Exception("Failed to find atlas offset for texture: " + name)
        }
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def lookupAtlasName(assetMapping: AssetMapping, name: String): String =
    QuickCache("atlas-" + name) {
      assetMapping.mappings.find(p => p._1 == name).map(_._2.atlasName).getOrElse {
        throw new Exception("Failed to find atlas name for texture: " + name)
      }
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def lookupAtlasSize(assetMapping: AssetMapping, name: String): Vector2 =
    QuickCache("atlas-size-" + name) {
      assetMapping.mappings.find(p => p._1 == name).map(_._2.atlasSize).getOrElse {
        throw new Exception("Failed to find atlas size for texture: " + name)
      }
    }

  private def cloneDataToDisplayEntity(id: String, cloneDepth: Double, data: CloneTransformData, blankTransform: CheapMatrix4): DisplayClone =
    new DisplayClone(
      id = id,
      transform = DisplayObjectConversions.cloneTransformDataToMatrix4(data, blankTransform),
      z = cloneDepth,
      alpha = data.alpha.toFloat
    )

  private def cloneBatchDataToDisplayEntities(batch: CloneBatch, blankTransform: CheapMatrix4): DisplayCloneBatch = {
    def convert(): DisplayCloneBatch =
      new DisplayCloneBatch(
        id = batch.id.value,
        z = batch.depth.zIndex.toDouble,
        clones = batch.clones.map { td =>
          new DisplayCloneBatchData(
            transform = DisplayObjectConversions.cloneTransformDataToMatrix4(batch.transform |+| td, blankTransform),
            alpha = batch.transform.alpha.toFloat
          )
        }
      )

    batch.staticBatchKey match {
      case None =>
        convert()

      case Some(bindingKey) =>
        QuickCache(bindingKey.value) {
          convert()
        }
    }
  }

  def sceneNodesToDisplayObjects(
      sceneNodes: List[SceneGraphNode],
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: Map[String, DisplayObject]
  ): ListBuffer[DisplayEntity] =
    deGroup(sceneNodes).flatMap { node =>
      sceneNodeToDisplayObject(node, gameTime, assetMapping, cloneBlankDisplayObjects)
    }

  private val accSceneNodes: ListBuffer[SceneGraphNode] = new ListBuffer()

  def deGroup(
      sceneNodes: List[SceneGraphNode]
  ): ListBuffer[SceneGraphNode] = {
    @tailrec
    def rec(remaining: List[SceneGraphNode]): ListBuffer[SceneGraphNode] =
      remaining match {
        case Nil =>
          accSceneNodes

        case Transformer(g: Group, mat) :: xs =>
          rec(g.toTransformers(mat) ++ xs)

        case Transformer(t: Transformer, mat) :: xs =>
          rec(t.addTransform(mat) :: xs)

        case (g: Group) :: xs =>
          rec(g.toTransformers ++ xs)

        case node :: xs =>
          accSceneNodes += node
          rec(xs)
      }

    accSceneNodes.clear()

    rec(sceneNodes)
  }

  def sceneNodeToDisplayObject(
      sceneNode: SceneGraphNode,
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: Map[String, DisplayObject]
  ): List[DisplayEntity] =
    sceneNode match {

      case s: Shape =>
        List(shapeToDisplayObject(s, assetMapping))

      case c: Clone =>
        cloneBlankDisplayObjects.get(c.id.value) match {
          case None =>
            Nil

          case Some(refDisplayObject) =>
            List(
              cloneDataToDisplayEntity(
                c.id.value,
                c.depth.zIndex.toDouble,
                c.transform,
                refDisplayObject.transform
              )
            )
        }

      case c: CloneBatch =>
        cloneBlankDisplayObjects.get(c.id.value) match {
          case None =>
            Nil

          case Some(refDisplayObject) =>
            List(cloneBatchDataToDisplayEntities(c, refDisplayObject.transform))
        }

      case _: Group =>
        Nil

      case t: Transformer =>
        sceneNodeToDisplayObject(t.node, gameTime, assetMapping, cloneBlankDisplayObjects)
          .map(_.applyTransform(t.transform))

      case x: Graphic =>
        List(graphicToDisplayObject(x, assetMapping))

      case x: Sprite =>
        animationsRegister.fetchAnimationForSprite(gameTime, x.bindingKey, x.animationKey, x.animationActions) match {
          case None =>
            IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${x.animationKey.toString()}")
            Nil

          case Some(anim) =>
            List(spriteToDisplayObject(boundaryLocator, x, assetMapping, anim))
        }

      case x: Text =>
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

        letters
    }

  // def materialToEmissiveValues(assetMapping: AssetMapping, material: GLSLShader): (Vector2, Double) =
  //   (Vector2.zero, 0.0d)
  // QuickCache(material.hash + "_emissive") {
  //   material match {
  //     case _: GLSLShader =>
  //       (Vector2.zero, 0.0d)

  //     case _: Material.Basic =>
  //       (Vector2.zero, 0.0d)

  //     case _: Material.Textured =>
  //       (Vector2.zero, 0.0d)

  //     case t: Material.Lit =>
  //       optionalAssetToValues(assetMapping, t.emissive)
  //   }
  // }

  // def materialToNormalValues(assetMapping: AssetMapping, material: Material): (Vector2, Double) =
  //   QuickCache(material.hash + "_normal") {
  //     material match {
  //       case _: GLSLShader =>
  //         (Vector2.zero, 0.0d)

  //       case _: Material.Basic =>
  //         (Vector2.zero, 0.0d)

  //       case _: Material.Textured =>
  //         (Vector2.zero, 0.0d)

  //       case t: Material.Lit =>
  //         optionalAssetToValues(assetMapping, t.normal)
  //     }
  //   }

  // def materialToSpecularValues(assetMapping: AssetMapping, material: Material): (Vector2, Double) =
  //   QuickCache(material.hash + "_specular") {
  //     material match {
  //       case _: GLSLShader =>
  //         (Vector2.zero, 0.0d)

  //       case _: Material.Basic =>
  //         (Vector2.zero, 0.0d)

  //       case _: Material.Textured =>
  //         (Vector2.zero, 0.0d)

  //       case t: Material.Lit =>
  //         optionalAssetToValues(assetMapping, t.specular)
  //     }
  //   }

  // def optionalAssetToValues(assetMapping: AssetMapping, maybeAssetName: Option[Texture]): (Vector2, Double) =
  //   maybeAssetName
  //     .map { t =>
  //       (lookupTextureOffset(assetMapping, t.assetName.value), Math.min(1.0d, Math.max(0.0d, t.amount)))
  //     }
  //     .getOrElse((Vector2.zero, 0.0d))

  def optionalAssetToOffset(assetMapping: AssetMapping, maybeAssetName: Option[AssetName]): Vector2 =
    maybeAssetName match {
      case None =>
        Vector2.zero

      case Some(assetName) =>
        lookupTextureOffset(assetMapping, assetName.value)
    }

  def shapeToDisplayObject(leaf: Shape, assetMapping: AssetMapping): DisplayObject = {
    val material: GLSLShader = leaf.material

    val channelOffset1 = optionalAssetToOffset(assetMapping, material.channel1)
    val channelOffset2 = optionalAssetToOffset(assetMapping, material.channel2)
    val channelOffset3 = optionalAssetToOffset(assetMapping, material.channel3)

    val frameInfo: SpriteSheetFrameCoordinateOffsets =
      material.channel1 match {
        case None =>
          SpriteSheetFrame.defaultOffset
        case Some(assetName) =>
          QuickCache(s"${leaf.bounds.hash}_${leaf.material.hash}") {
            SpriteSheetFrame.calculateFrameOffset(
              atlasSize = lookupAtlasSize(assetMapping, assetName.value),
              frameCrop = leaf.bounds,
              textureOffset = lookupTextureOffset(assetMapping, assetName.value)
            )
          }
      }

    val shaderId          = material.shaderId
    val shaderUniformHash = material.uniformHash
    val shaderUBO = QuickCache(shaderUniformHash) {
      material.uniforms.toArray.map(_._2.toArray).flatten
    }

    DisplayObject(
      transform = DisplayObjectConversions.nodeToMatrix4(leaf, Vector3(leaf.bounds.size.x.toDouble, leaf.bounds.size.y.toDouble, 1.0d)),
      z = leaf.depth.zIndex.toDouble,
      width = leaf.bounds.size.x,
      height = leaf.bounds.size.y,
      atlasName = material.channel0.map(assetName => lookupAtlasName(assetMapping, assetName.value)),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(channelOffset1),
      channelOffset2 = frameInfo.offsetToCoords(channelOffset2),
      channelOffset3 = frameInfo.offsetToCoords(channelOffset3),
      isLit = 0.0f,
      shaderId = shaderId,
      shaderUniformHash = shaderUniformHash,
      shaderUBO = shaderUBO
    )
  }

  def graphicToDisplayObject(leaf: Graphic, assetMapping: AssetMapping): DisplayObject = {
    val asCustom     = leaf.material.toGLSLShader
    val materialName = asCustom.channel0.get.value

    // val albedoAmount                     = 1.0f
    val (emissiveOffset, _) = (Vector2.zero, 0.0d) //materialToEmissiveValues(assetMapping, leaf.material)
    val (normalOffset, _)   = (Vector2.zero, 0.0d) //materialToNormalValues(assetMapping, leaf.material)
    val (specularOffset, _) = (Vector2.zero, 0.0d) //materialToSpecularValues(assetMapping, leaf.material)

    val frameInfo =
      QuickCache(s"${leaf.crop.hash}_${leaf.material.hash}") {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = lookupAtlasSize(assetMapping, materialName),
          frameCrop = leaf.crop,
          textureOffset = lookupTextureOffset(assetMapping, materialName)
        )
      }

    // val effectsValues =
    //   QuickCache(leaf.effects.hash) {
    //     DisplayEffects.fromEffects(leaf.effects)
    //   }

    // val shaderId = leaf.material.shaderId
    // val (shaderUniformHash, shaderUBO) = leaf.material match {
    //   case s @ GLSLShader(_, uniforms, _) =>
    //     val hash = s.uniformHash
    //     val us = QuickCache(hash) {
    //       uniforms.toArray.map(_._2.toArray).flatten
    //     }

    //     (hash, us)

    //   case _ =>
    //     ("", Array[Float]())
    // }

    val shaderId          = asCustom.shaderId
    val shaderUniformHash = asCustom.uniformHash
    val shaderUBO = QuickCache(shaderUniformHash) {
      asCustom.uniforms.toArray.map(_._2.toArray).flatten
    }

    DisplayObject(
      transform = DisplayObjectConversions.nodeToMatrix4(leaf, Vector3(leaf.crop.size.x.toDouble, leaf.crop.size.y.toDouble, 1.0d)),
      z = leaf.depth.zIndex.toDouble,
      width = leaf.crop.size.x,
      height = leaf.crop.size.y,
      atlasName = Some(lookupAtlasName(assetMapping, materialName)),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      isLit = 0.0f, // if (leaf.material.isLit) 1.0f else 0.0f,
      shaderId = shaderId,
      shaderUniformHash = shaderUniformHash,
      shaderUBO = shaderUBO
    )
  }

  def spriteToDisplayObject(boundaryLocator: BoundaryLocator, leaf: Sprite, assetMapping: AssetMapping, anim: AnimationRef): DisplayObject = {
    val material     = anim.currentFrame.frameMaterial.getOrElse(anim.material)
    val asCustom     = material.toGLSLShader
    val materialName = asCustom.channel0.get.value

    // val albedoAmount                     = 1.0f
    val (emissiveOffset, _) = (Vector2.zero, 0.0d) //materialToEmissiveValues(assetMapping, material)
    val (normalOffset, _)   = (Vector2.zero, 0.0d) //materialToNormalValues(assetMapping, material)
    val (specularOffset, _) = (Vector2.zero, 0.0d) //materialToSpecularValues(assetMapping, material)

    val frameInfo =
      QuickCache(anim.frameHash) {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = lookupAtlasSize(assetMapping, materialName),
          frameCrop = anim.currentFrame.crop,
          textureOffset = lookupTextureOffset(assetMapping, materialName)
        )
      }

    // val effectsValues =
    //   QuickCache(leaf.effects.hash) {
    //     DisplayEffects.fromEffects(leaf.effects)
    //   }

    val width: Int  = leaf.bounds(boundaryLocator).size.x
    val height: Int = leaf.bounds(boundaryLocator).size.y

    // val shaderId = material.shaderId
    // val (shaderUniformHash, shaderUBO) = material match {
    //   case s @ GLSLShader(_, uniforms, _) =>
    //     val hash = s.uniformHash
    //     val us = QuickCache(hash) {
    //       uniforms.toArray.foldLeft(Array[Float]())(_ ++ _._2.toArray)
    //     }

    //     (hash, us)

    //   case _ =>
    //     ("", Array[Float]())
    // }

    val shaderId          = asCustom.shaderId
    val shaderUniformHash = asCustom.uniformHash
    val shaderUBO = QuickCache(shaderUniformHash) {
      asCustom.uniforms.toArray.map(_._2.toArray).flatten
    }

    DisplayObject(
      transform = DisplayObjectConversions.nodeToMatrix4(leaf, Vector3(width.toDouble, height.toDouble, 1.0d)),
      z = leaf.depth.zIndex.toDouble,
      width = width,
      height = height,
      atlasName = Some(lookupAtlasName(assetMapping, materialName)),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      isLit = 0.0f, // if (material.isLit) 1.0f else 0.0f,
      shaderId = shaderId,
      shaderUniformHash = shaderUniformHash,
      shaderUBO = shaderUBO
    )
  }

  def textLineToDisplayObjects(leaf: Text, assetMapping: AssetMapping, fontInfo: FontInfo): (TextLine, Int, Int) => List[DisplayObject] =
    (line, alignmentOffsetX, yOffset) => {

      val asCustom     = fontInfo.fontSpriteSheet.material.toGLSLShader
      val materialName = asCustom.channel0.get.value

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

      // val albedoAmount                     = 1.0f
      val (emissiveOffset, _) = (Vector2.zero, 0.0d) //materialToEmissiveValues(assetMapping, fontInfo.fontSpriteSheet.material)
      val (normalOffset, _)   = (Vector2.zero, 0.0d) //materialToNormalValues(assetMapping, fontInfo.fontSpriteSheet.material)
      val (specularOffset, _) = (Vector2.zero, 0.0d) //materialToSpecularValues(assetMapping, fontInfo.fontSpriteSheet.material)

      // val effectsValues =
      //   QuickCache(leaf.effects.hash) {
      //     DisplayEffects.fromEffects(leaf.effects)
      //   }

      // val shaderId = fontInfo.fontSpriteSheet.material.shaderId
      // val (shaderUniformHash, shaderUBO) = fontInfo.fontSpriteSheet.material match {
      //   case s @ GLSLShader(_, uniforms, _) =>
      //     val hash = s.uniformHash
      //     val us = QuickCache(hash) {
      //       uniforms.toArray.foldLeft(Array[Float]())(_ ++ _._2.toArray)
      //     }

      //     (hash, us)

      //   case _ =>
      //     ("", Array[Float]())
      // }

      val shaderId          = asCustom.shaderId
      val shaderUniformHash = asCustom.uniformHash
      val shaderUBO = QuickCache(shaderUniformHash) {
        asCustom.uniforms.toArray.map(_._2.toArray).flatten
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
              transform = DisplayObjectConversions.nodeToMatrix4(
                leaf.moveBy(xPosition + alignmentOffsetX, yOffset),
                Vector3(fontChar.bounds.width.toDouble, fontChar.bounds.height.toDouble, 1.0d)
              ),
              z = leaf.depth.zIndex.toDouble,
              width = fontChar.bounds.width,
              height = fontChar.bounds.height,
              atlasName = Some(lookupAtlasName(assetMapping, materialName)),
              frame = frameInfo,
              channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
              channelOffset2 = frameInfo.offsetToCoords(normalOffset),
              channelOffset3 = frameInfo.offsetToCoords(specularOffset),
              isLit = 0.0f, // if (fontInfo.fontSpriteSheet.material.isLit) 1.0f else 0.0f,
              shaderId = shaderId,
              shaderUniformHash = shaderUniformHash,
              shaderUBO = shaderUBO
            )
        }
      }
    }
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
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

object DisplayObjectConversions {

  def nodeToMatrix4(node: SceneGraphNode, size: Vector3): CheapMatrix4 =
    CheapMatrix4.identity
      .scale(
        if (node.flip.horizontal) -1.0 else 1.0,
        if (node.flip.vertical) 1.0 else -1.0,
        1.0d
      )
      .translate(
        -(node.ref.x.toDouble / size.x) + 0.5d,
        -(node.ref.y.toDouble / size.y) + 0.5d,
        0.0d
      )
      .scale(
        size.x * node.scale.x,
        size.y * node.scale.y,
        size.z
      )
      .rotate(node.rotation.value)
      .translate(
        node.position.x.toDouble,
        node.position.y.toDouble,
        0.0d
      )

  def cloneTransformDataToMatrix4(data: CloneTransformData, blankTransform: CheapMatrix4): CheapMatrix4 =
    blankTransform.deepClone * CheapMatrix4.identity
      .translate(-blankTransform.x, -blankTransform.y, 0.0d)
      .scale(
        if (data.flipHorizontal) -1.0 else 1.0,
        if (!data.flipVertical) 1.0 else -1.0,
        1.0d
      )
      .scale(data.scale.x, data.scale.y, 1.0d)
      .rotate(data.rotation.value)
      .translate(
        data.position.x.toDouble,
        data.position.y.toDouble,
        0.0d
      )

}
