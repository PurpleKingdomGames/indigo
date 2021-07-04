package indigo.shared.platform

import indigo.shared.display.{DisplayObject, SpriteSheetFrame, DisplayCloneBatch}
import indigo.shared.datatypes.{FontInfo, Rectangle, TextAlignment, FontChar, Point}
import indigo.shared.display.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.shared.IndigoLogger
import indigo.shared.time.GameTime
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Vector3
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.platform.AssetMapping
import indigo.shared.scenegraph.{Graphic, Sprite, Text, TextLine, TextBox}

import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.RenderNode
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Transformer
import indigo.shared.QuickCache

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.display.DisplayText
import indigo.shared.scenegraph.Clone
import indigo.shared.scenegraph.CloneId
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.display.DisplayClone
import indigo.shared.scenegraph.CloneTransformData
import indigo.shared.materials.ShaderData
import indigo.shared.BoundaryLocator
import indigo.shared.animation.AnimationRef
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.assets.AssetName
import indigo.shared.scenegraph.EntityNode
import indigo.shared.scenegraph.SceneNode
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.scenegraph.Shape
import indigo.platform.assets.AtlasId

final class DisplayObjectConversions(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
) {

  implicit private val textureRefAndOffsetCache: QuickCache[TextureRefAndOffset] = QuickCache.empty
  implicit private val vector2Cache: QuickCache[Vector2]                         = QuickCache.empty
  implicit private val frameCache: QuickCache[SpriteSheetFrameCoordinateOffsets] = QuickCache.empty
  implicit private val listDoCache: QuickCache[List[DisplayObject]]              = QuickCache.empty
  implicit private val cloneBatchCache: QuickCache[DisplayCloneBatch]            = QuickCache.empty
  implicit private val uniformsCache: QuickCache[Array[Float]]                   = QuickCache.empty

  def purgeCaches(): Unit = {
    textureRefAndOffsetCache.purgeAllNow()
    vector2Cache.purgeAllNow()
    frameCache.purgeAllNow()
    listDoCache.purgeAllNow()
    cloneBatchCache.purgeAllNow()
    uniformsCache.purgeAllNow()
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def lookupTexture(assetMapping: AssetMapping, name: AssetName): TextureRefAndOffset =
    QuickCache("tex-" + name.toString) {
      assetMapping.mappings
        .find(p => p._1 == name)
        .map(_._2)
        .getOrElse {
          throw new Exception("Failed to find texture ref + offset for: " + name)
        }
    }

  private def cloneDataToDisplayEntity(
      id: CloneId,
      cloneDepth: Double,
      data: CloneTransformData,
      blankTransform: CheapMatrix4
  ): DisplayClone =
    new DisplayClone(
      id = id,
      transform = DisplayObjectConversions.cloneTransformDataToMatrix4(data, blankTransform),
      z = cloneDepth
    )

  private def cloneBatchDataToDisplayEntities(batch: CloneBatch, blankTransform: CheapMatrix4): DisplayCloneBatch = {
    def convert(): DisplayCloneBatch =
      new DisplayCloneBatch(
        id = batch.id,
        z = batch.depth.toDouble,
        clones = batch.clones.map { td =>
          DisplayObjectConversions.cloneTransformDataToMatrix4(batch.transform |+| td, blankTransform)
        }
      )

    batch.staticBatchKey match {
      case None =>
        convert()

      case Some(bindingKey) =>
        QuickCache(bindingKey.toString) {
          convert()
        }
    }
  }

  def sceneNodesToDisplayObjects(
      sceneNodes: List[SceneGraphNode],
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: Map[CloneId, DisplayObject]
  ): ListBuffer[DisplayEntity] =
    deGroup(sceneNodes).flatMap { node =>
      sceneNodeToDisplayObject(node, gameTime, assetMapping, cloneBlankDisplayObjects)
    }

  private val accSceneNodes: ListBuffer[SceneGraphNode] = new ListBuffer()

  private def groupToMatrix(group: Group): CheapMatrix4 =
    CheapMatrix4.identity
      .scale(
        if (group.flip.horizontal) -1.0 else 1.0,
        if (group.flip.vertical) -1.0 else 1.0,
        1.0d
      )
      .translate(
        -group.ref.x.toDouble,
        -group.ref.y.toDouble,
        0.0d
      )
      .scale(group.scale.x, group.scale.y, 1.0d)
      .rotate(group.rotation)
      .translate(
        group.position.x.toDouble,
        group.position.y.toDouble,
        0.0d
      )

  private def toTransformers(group: Group, parentTransform: CheapMatrix4): List[Transformer] = {
    val mat = groupToMatrix(group) * parentTransform // to avoid re-evaluation
    group.children.map { n =>
      Transformer(n.withDepth(n.depth + group.depth), mat)
    }
  }

  def deGroup(
      sceneNodes: List[SceneGraphNode]
  ): ListBuffer[SceneGraphNode] = {
    @tailrec
    def rec(remaining: List[SceneGraphNode]): ListBuffer[SceneGraphNode] =
      remaining match {
        case Nil =>
          accSceneNodes

        case Transformer(g: Group, mat) :: xs =>
          rec(toTransformers(g, mat) ++ xs)

        case Transformer(t: Transformer, mat) :: xs =>
          rec(t.addTransform(mat) :: xs)

        case (g: Group) :: xs =>
          rec(toTransformers(g, CheapMatrix4.identity) ++ xs)

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
      cloneBlankDisplayObjects: Map[CloneId, DisplayObject]
  ): List[DisplayEntity] =
    sceneNode match {

      case x: Graphic[_] =>
        List(graphicToDisplayObject(x, assetMapping))

      case s: Shape =>
        List(shapeToDisplayObject(s))

      case t: TextBox =>
        List(textBoxToDisplayText(t))

      case s: EntityNode =>
        List(sceneEntityToDisplayObject(s, assetMapping))

      case c: Clone =>
        cloneBlankDisplayObjects.get(c.id) match {
          case None =>
            Nil

          case Some(refDisplayObject) =>
            List(
              cloneDataToDisplayEntity(
                c.id,
                c.depth.toDouble,
                c.transform,
                refDisplayObject.transform
              )
            )
        }

      case c: CloneBatch =>
        cloneBlankDisplayObjects.get(c.id) match {
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

      case x: Sprite[_] =>
        animationsRegister.fetchAnimationForSprite(gameTime, x.bindingKey, x.animationKey, x.animationActions) match {
          case None =>
            IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${x.animationKey.toString()}")
            Nil

          case Some(anim) =>
            List(spriteToDisplayObject(boundaryLocator, x, assetMapping, anim))
        }

      case x: Text[_] =>
        val alignmentOffsetX: Rectangle => Int = lineBounds =>
          x.alignment match {
            case TextAlignment.Left => 0

            case TextAlignment.Center => -(lineBounds.size.width / 2)

            case TextAlignment.Right => -lineBounds.size.width
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
              (
                acc._1 + textLine.lineBounds.height,
                acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1)
              )
            }
            ._2

        letters

      case _ =>
        Nil
    }

  def optionalAssetToOffset(assetMapping: AssetMapping, maybeAssetName: Option[AssetName]): Vector2 =
    maybeAssetName match {
      case None =>
        Vector2.zero

      case Some(assetName) =>
        lookupTexture(assetMapping, assetName).offset
    }

  def shapeToDisplayObject(leaf: Shape): DisplayObject = {

    val offset = leaf match
      case s: Shape.Box =>
        val size = s.dimensions.size

        if size.width == size.height then Point.zero
        else if size.width < size.height then
          Point(-Math.round((size.height.toDouble - size.width.toDouble) / 2).toInt, 0)
        else Point(0, -Math.round((size.width.toDouble - size.height.toDouble) / 2).toInt)

      case _ =>
        Point.zero

    val boundsActual = boundaryLocator.shapeBounds(leaf)

    val shader: ShaderData = Shape.toShaderData(leaf, boundsActual)
    val bounds             = boundsActual.toSquare

    val vec2Zero = Vector2.zero
    val uniformData: List[DisplayObjectUniformData] =
      shader.uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName,
          data = DisplayObjectConversions.packUBO(ub.uniforms)
        )
      }

    DisplayObject(
      transform = DisplayObjectConversions
        .nodeToMatrix4(
          leaf.withRef(leaf.ref - offset),
          bounds.position.toVector,
          Vector3(bounds.size.width.toDouble, bounds.size.height.toDouble, 1.0d)
        ),
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = bounds.size.width,
      height = bounds.size.height,
      atlasName = None,
      frame = SpriteSheetFrame.defaultOffset,
      channelOffset1 = vec2Zero,
      channelOffset2 = vec2Zero,
      channelOffset3 = vec2Zero,
      textureSize = vec2Zero,
      atlasSize = vec2Zero,
      shaderId = shader.shaderId,
      shaderUniformData = uniformData
    )
  }

  private given CanEqual[Option[TextureRefAndOffset], Option[TextureRefAndOffset]] = CanEqual.derived

  def sceneEntityToDisplayObject(leaf: EntityNode, assetMapping: AssetMapping): DisplayObject = {
    val shader: ShaderData = leaf.toShaderData

    val channelOffset1 = optionalAssetToOffset(assetMapping, shader.channel1)
    val channelOffset2 = optionalAssetToOffset(assetMapping, shader.channel2)
    val channelOffset3 = optionalAssetToOffset(assetMapping, shader.channel3)

    val bounds = Rectangle(Point.zero, leaf.size)

    val texture =
      shader.channel0.map(assetName => lookupTexture(assetMapping, assetName))

    val frameInfo: SpriteSheetFrameCoordinateOffsets =
      texture match {
        case None =>
          SpriteSheetFrame.defaultOffset

        case Some(texture) =>
          QuickCache(s"${bounds.hashCode().toString}_${shader.hashCode().toString}") {
            SpriteSheetFrame.calculateFrameOffset(
              atlasSize = texture.atlasSize,
              frameCrop = bounds,
              textureOffset = texture.offset
            )
          }
      }

    val shaderId = shader.shaderId

    val uniformData: List[DisplayObjectUniformData] =
      shader.uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName,
          data = DisplayObjectConversions.packUBO(ub.uniforms)
        )
      }

    DisplayObject(
      transform = DisplayObjectConversions
        .nodeToMatrix4(
          leaf,
          leaf.position.toVector,
          Vector3(bounds.size.width.toDouble, bounds.size.height.toDouble, 1.0d)
        ),
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = bounds.size.width,
      height = bounds.size.height,
      atlasName = texture.map(_.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(channelOffset1),
      channelOffset2 = frameInfo.offsetToCoords(channelOffset2),
      channelOffset3 = frameInfo.offsetToCoords(channelOffset3),
      textureSize = texture.map(_.size).getOrElse(Vector2.zero),
      atlasSize = texture.map(_.atlasSize).getOrElse(Vector2.zero),
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }

  def textBoxToDisplayText(leaf: TextBox): DisplayText =
    DisplayText(
      text = leaf.text,
      style = leaf.style,
      transform = DisplayObjectConversions
        .nodeToMatrix4(
          leaf,
          leaf.position.toVector,
          Vector3(leaf.size.width.toDouble, leaf.size.height.toDouble, 1.0d)
        ),
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = leaf.size.width,
      height = leaf.size.height
    )

  def graphicToDisplayObject(leaf: Graphic[_], assetMapping: AssetMapping): DisplayObject = {
    val shaderData     = leaf.material.toShaderData
    val shaderDataHash = shaderData.hashCode().toString
    val materialName   = shaderData.channel0.get

    val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
    val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
    val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

    val texture = lookupTexture(assetMapping, materialName)

    val frameInfo =
      QuickCache(s"${leaf.crop.hashCode().toString}_$shaderDataHash") {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = texture.atlasSize,
          frameCrop = leaf.crop,
          textureOffset = texture.offset
        )
      }

    val shaderId = shaderData.shaderId

    val uniformData: List[DisplayObjectUniformData] =
      shaderData.uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName,
          data = DisplayObjectConversions.packUBO(ub.uniforms)
        )
      }

    DisplayObject(
      transform = DisplayObjectConversions
        .nodeToMatrix4(
          leaf,
          leaf.position.toVector,
          Vector3(leaf.crop.size.width.toDouble, leaf.crop.size.height.toDouble, 1.0d)
        ),
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = leaf.crop.size.width,
      height = leaf.crop.size.height,
      atlasName = Some(texture.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      textureSize = texture.size,
      atlasSize = texture.atlasSize,
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }

  def spriteToDisplayObject(
      boundaryLocator: BoundaryLocator,
      leaf: Sprite[_],
      assetMapping: AssetMapping,
      anim: AnimationRef
  ): DisplayObject = {
    val material       = leaf.material
    val shaderData     = material.toShaderData
    val shaderDataHash = shaderData.hashCode().toString
    val materialName   = shaderData.channel0.get

    val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
    val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
    val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

    val texture = lookupTexture(assetMapping, materialName)

    val frameInfo =
      QuickCache(anim.frameHash + shaderDataHash) {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = texture.atlasSize,
          frameCrop = anim.currentFrame.crop,
          textureOffset = texture.offset
        )
      }

    val bounds = boundaryLocator.spriteBounds(leaf).getOrElse(Rectangle.zero)

    val shaderId = shaderData.shaderId

    val uniformData: List[DisplayObjectUniformData] =
      shaderData.uniformBlocks.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName,
          data = DisplayObjectConversions.packUBO(ub.uniforms)
        )
      }

    DisplayObject(
      transform = DisplayObjectConversions.nodeToMatrix4(
        leaf,
        leaf.position.toVector,
        Vector3(bounds.width.toDouble, bounds.height.toDouble, 1.0d)
      ),
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = bounds.width,
      height = bounds.height,
      atlasName = Some(texture.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      textureSize = texture.size,
      atlasSize = texture.atlasSize,
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }

  def textLineToDisplayObjects(
      leaf: Text[_],
      assetMapping: AssetMapping,
      fontInfo: FontInfo
  ): (TextLine, Int, Int) => List[DisplayObject] =
    (line, alignmentOffsetX, yOffset) => {

      val material       = leaf.material
      val shaderData     = material.toShaderData
      val shaderDataHash = shaderData.hashCode().toString
      val materialName   = shaderData.channel0.get

      val lineHash: String =
        leaf.fontKey.toString +
          ":" + line.hashCode().toString +
          ":" + alignmentOffsetX.toString() +
          ":" + yOffset.toString() +
          ":" + leaf.position.hashCode().toString +
          ":" + leaf.ref.hashCode().toString +
          ":" + leaf.rotation.hashCode().toString +
          ":" + leaf.scale.hashCode().toString +
          ":" + shaderData.hashCode().toString

      val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
      val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
      val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

      val texture = lookupTexture(assetMapping, materialName)

      val shaderId = shaderData.shaderId

      val uniformData: List[DisplayObjectUniformData] =
        shaderData.uniformBlocks.map { ub =>
          DisplayObjectUniformData(
            uniformHash = ub.uniformHash,
            blockName = ub.blockName,
            data = DisplayObjectConversions.packUBO(ub.uniforms)
          )
        }

      QuickCache(lineHash) {
        zipWithCharDetails(line.text.toList, fontInfo).toList.map { case (fontChar, xPosition) =>
          val frameInfo =
            QuickCache(fontChar.bounds.hashCode().toString + "_" + shaderDataHash) {
              SpriteSheetFrame.calculateFrameOffset(
                atlasSize = texture.atlasSize,
                frameCrop = fontChar.bounds,
                textureOffset = texture.offset
              )
            }

          DisplayObject(
            transform = DisplayObjectConversions.nodeToMatrix4(
              leaf.withRef(leaf.ref.x + -(xPosition + alignmentOffsetX), leaf.ref.y - yOffset),
              leaf.position.toVector,
              Vector3(fontChar.bounds.width.toDouble, fontChar.bounds.height.toDouble, 1.0d)
            ),
            rotation = leaf.rotation,
            z = leaf.depth.toDouble,
            width = fontChar.bounds.width,
            height = fontChar.bounds.height,
            atlasName = Some(texture.atlasName),
            frame = frameInfo,
            channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
            channelOffset2 = frameInfo.offsetToCoords(normalOffset),
            channelOffset3 = frameInfo.offsetToCoords(specularOffset),
            textureSize = texture.size,
            atlasSize = texture.atlasSize,
            shaderId = shaderId,
            shaderUniformData = uniformData
          )
        }
      }
    }
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var accCharDetails: ListBuffer[(FontChar, Int)] = new ListBuffer()

  private given CanEqual[List[(Char, FontChar)], List[(Char, FontChar)]] = CanEqual.derived

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

  def findAssetOffsetValues(
      assetMapping: AssetMapping,
      maybeAssetName: Option[AssetName],
      cacheKey: String,
      cacheSuffix: String
  ): Vector2 =
    QuickCache[Vector2](cacheKey + cacheSuffix) {
      maybeAssetName
        .map { t =>
          lookupTexture(assetMapping, t).offset
        }
        .getOrElse(Vector2.zero)
    }
}

object DisplayObjectConversions {

  def nodeToMatrix4(node: SceneNode, position: Vector2, size: Vector3): CheapMatrix4 =
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
      .rotate(node.rotation)
      .translate(
        position.x,
        position.y,
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
      .rotate(data.rotation)
      .translate(
        data.position.x.toDouble,
        data.position.y.toDouble,
        0.0d
      )

  private val empty0: Array[Float] = Array[Float]()
  private val empty1: Array[Float] = Array[Float](0.0f)
  private val empty2: Array[Float] = Array[Float](0.0f, 0.0f)
  private val empty3: Array[Float] = Array[Float](0.0f, 0.0f, 0.0f)

  def expandTo4(arr: Array[Float]): Array[Float] =
    arr.length match {
      case 0 => arr
      case 1 => arr ++ empty3
      case 2 => arr ++ empty2
      case 3 => arr ++ empty1
      case 4 => arr
      case _ => arr
    }

  def packUBO(uniforms: List[(Uniform, ShaderPrimitive)])(using QuickCache[Array[Float]]): Array[Float] = {
    def rec(remaining: List[ShaderPrimitive], current: Array[Float], acc: Array[Float]): Array[Float] =
      remaining match {
        case Nil =>
          // println(s"done, expanded: ${current.toList} to ${expandTo4(current).toList}")
          // println(s"result: ${(acc ++ expandTo4(current)).toList}")
          acc ++ expandTo4(current)

        case us if current.length == 4 =>
          // println(s"current full, sub-result: ${(acc ++ current).toList}")
          rec(us, empty0, acc ++ current)

        case u :: us if current.isEmpty && u.isArray =>
          // println(s"Found an array, current is empty, set current to: ${u.toArray.toList}")
          rec(us, u.toArray, acc)

        case u :: _ if current.length + u.length > 4 =>
          // println(s"doesn't fit, expanded: ${current.toList} to ${expandTo4(current).toList},  sub-result: ${(acc ++ expandTo4(current)).toList}")
          rec(remaining, empty0, acc ++ expandTo4(current))

        case u :: _ if u.isArray =>
          // println(s"fits but next value is array, expanded: ${current.toList} to ${expandTo4(current).toList},  sub-result: ${(acc ++ expandTo4(current)).toList}")
          rec(remaining, empty0, acc ++ current)

        case u :: us =>
          // println(s"fits, current is now: ${(current ++ u.toArray).toList}")
          rec(us, current ++ u.toArray, acc)
      }

    QuickCache("u" + uniforms.hashCode.toString) {
      rec(uniforms.map(_._2), empty0, empty0)
    }
  }

}
