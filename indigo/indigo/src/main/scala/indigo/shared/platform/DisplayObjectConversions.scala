package indigo.shared.platform

import indigo.platform.assets.AtlasId
import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.IndigoLogger
import indigo.shared.QuickCache
import indigo.shared.animation.AnimationRef
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.TextAlignment
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Vector3
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayCloneTiles
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayGroup
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.display.DisplayText
import indigo.shared.display.SpriteSheetFrame
import indigo.shared.display.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.shared.materials.ShaderData
import indigo.shared.platform.AssetMapping
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.CloneId
import indigo.shared.scenegraph.CloneTiles
import indigo.shared.scenegraph.DependentNode
import indigo.shared.scenegraph.EntityNode
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.RenderNode
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Sprite
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.TextBox
import indigo.shared.scenegraph.TextLine
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.shader.Uniform
import indigo.shared.time.GameTime

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

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
  implicit private val cloneTilesCache: QuickCache[DisplayCloneTiles]            = QuickCache.empty
  implicit private val uniformsCache: QuickCache[Array[Float]]                   = QuickCache.empty

  def purgeCaches(): Unit = {
    textureRefAndOffsetCache.purgeAllNow()
    vector2Cache.purgeAllNow()
    frameCache.purgeAllNow()
    listDoCache.purgeAllNow()
    cloneBatchCache.purgeAllNow()
    cloneTilesCache.purgeAllNow()
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

  private def cloneBatchDataToDisplayEntities(batch: CloneBatch): DisplayCloneBatch =
    if batch.staticBatchKey.isDefined then
      QuickCache(batch.staticBatchKey.get.toString) {
        new DisplayCloneBatch(
          id = batch.id,
          z = batch.depth.toDouble,
          cloneData = batch.cloneData
        )
      }
    else
      new DisplayCloneBatch(
        id = batch.id,
        z = batch.depth.toDouble,
        cloneData = batch.cloneData
      )

  private def cloneTilesDataToDisplayEntities(batch: CloneTiles): DisplayCloneTiles =
    if batch.staticBatchKey.isDefined then
      QuickCache(batch.staticBatchKey.get.toString) {
        new DisplayCloneTiles(
          id = batch.id,
          z = batch.depth.toDouble,
          cloneData = batch.cloneData
        )
      }
    else
      new DisplayCloneTiles(
        id = batch.id,
        z = batch.depth.toDouble,
        cloneData = batch.cloneData
      )

  def sceneNodesToDisplayObjects(
      sceneNodes: List[SceneGraphNode],
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: Map[CloneId, DisplayObject]
  ): Array[DisplayEntity] =
    val f = sceneNodeToDisplayObject(gameTime, assetMapping, cloneBlankDisplayObjects)
    sceneNodes.toArray.map(f)

  private def groupToMatrix(group: Group): CheapMatrix4 =
    CheapMatrix4.identity
      .scale(
        if (group.flip.horizontal) -1.0 else 1.0,
        if (group.flip.vertical) -1.0 else 1.0,
        1.0f
      )
      .translate(
        -group.ref.x.toFloat,
        -group.ref.y.toFloat,
        0.0f
      )
      .scale(group.scale.x.toFloat, group.scale.y.toFloat, 1.0f)
      .rotate(group.rotation.toFloat)
      .translate(
        group.position.x.toFloat,
        group.position.y.toFloat,
        0.0f
      )

  def sceneNodeToDisplayObject(
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: Map[CloneId, DisplayObject]
  )(sceneNode: SceneGraphNode): DisplayEntity =
    sceneNode match {
      case x: Graphic[_] =>
        graphicToDisplayObject(x, assetMapping)

      case s: Shape =>
        shapeToDisplayObject(s)

      case t: TextBox =>
        textBoxToDisplayText(t)

      case s: EntityNode =>
        sceneEntityToDisplayObject(s, assetMapping)

      case c: CloneBatch =>
        cloneBlankDisplayObjects.get(c.id) match {
          case None =>
            DisplayGroup.empty

          case Some(refDisplayObject) =>
            cloneBatchDataToDisplayEntities(c)
        }

      case c: CloneTiles =>
        cloneBlankDisplayObjects.get(c.id) match {
          case None =>
            DisplayGroup.empty

          case Some(refDisplayObject) =>
            cloneTilesDataToDisplayEntities(c)
        }

      case g: Group =>
        DisplayGroup(
          groupToMatrix(g),
          g.depth.toDouble,
          sceneNodesToDisplayObjects(g.children, gameTime, assetMapping, cloneBlankDisplayObjects)
        )

      case x: Sprite[_] =>
        animationsRegister.fetchAnimationForSprite(gameTime, x.bindingKey, x.animationKey, x.animationActions) match {
          case None =>
            IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${x.animationKey.toString()}")
            DisplayGroup.empty

          case Some(anim) =>
            spriteToDisplayObject(boundaryLocator, x, assetMapping, anim)
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

        DisplayGroup(CheapMatrix4.identity, x.depth.toDouble, letters.toArray)

      case _: RenderNode =>
        DisplayGroup.empty

      case _: DependentNode =>
        DisplayGroup.empty
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

    val offsetRef = leaf.ref - offset

    DisplayObject(
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = offsetRef.x.toFloat,
      refY = offsetRef.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = bounds.size.width,
      height = bounds.size.height,
      atlasName = None,
      frame = SpriteSheetFrame.defaultOffset,
      channelOffset1 = vec2Zero,
      channelOffset2 = vec2Zero,
      channelOffset3 = vec2Zero,
      texturePosition = vec2Zero,
      textureSize = vec2Zero,
      atlasSize = vec2Zero,
      shaderId = shader.shaderId,
      shaderUniformData = uniformData.toArray
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
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = leaf.ref.x.toFloat,
      refY = leaf.ref.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = bounds.size.width,
      height = bounds.size.height,
      atlasName = texture.map(_.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(channelOffset1),
      channelOffset2 = frameInfo.offsetToCoords(channelOffset2),
      channelOffset3 = frameInfo.offsetToCoords(channelOffset3),
      texturePosition = texture.map(_.offset).getOrElse(Vector2.zero),
      textureSize = texture.map(_.size).getOrElse(Vector2.zero),
      atlasSize = texture.map(_.atlasSize).getOrElse(Vector2.zero),
      shaderId = shaderId,
      shaderUniformData = uniformData.toArray
    )
  }

  def textBoxToDisplayText(leaf: TextBox): DisplayText =
    DisplayText(
      text = leaf.text,
      style = leaf.style,
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = leaf.ref.x.toFloat,
      refY = leaf.ref.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
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
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = leaf.ref.x.toFloat,
      refY = leaf.ref.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = leaf.crop.size.width,
      height = leaf.crop.size.height,
      atlasName = Some(texture.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      texturePosition = texture.offset,
      textureSize = texture.size,
      atlasSize = texture.atlasSize,
      shaderId = shaderId,
      shaderUniformData = uniformData.toArray
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
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = leaf.ref.x.toFloat,
      refY = leaf.ref.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      z = leaf.depth.toDouble,
      width = bounds.width,
      height = bounds.height,
      atlasName = Some(texture.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      texturePosition = texture.offset,
      textureSize = texture.size,
      atlasSize = texture.atlasSize,
      shaderId = shaderId,
      shaderUniformData = uniformData.toArray
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
        leaf.hashCode.toString + line.hashCode.toString

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
            x = leaf.position.x.toFloat,
            y = leaf.position.y.toFloat,
            scaleX = leaf.scale.x.toFloat,
            scaleY = leaf.scale.y.toFloat,
            refX = (leaf.ref.x + -(xPosition + alignmentOffsetX)).toFloat, //leaf.ref.x.toFloat,
            refY = (leaf.ref.y - yOffset).toFloat,
            flipX = if leaf.flip.horizontal then -1.0 else 1.0,
            flipY = if leaf.flip.vertical then -1.0 else 1.0,
            rotation = leaf.rotation,
            z = leaf.depth.toDouble,
            width = fontChar.bounds.width,
            height = fontChar.bounds.height,
            atlasName = Some(texture.atlasName),
            frame = frameInfo,
            channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
            channelOffset2 = frameInfo.offsetToCoords(normalOffset),
            channelOffset3 = frameInfo.offsetToCoords(specularOffset),
            texturePosition = texture.offset,
            textureSize = texture.size,
            atlasSize = texture.atlasSize,
            shaderId = shaderId,
            shaderUniformData = uniformData.toArray
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
