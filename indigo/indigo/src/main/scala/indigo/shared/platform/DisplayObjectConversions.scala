package indigo.shared.platform

import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.IndigoLogger
import indigo.shared.QuickCache
import indigo.shared.animation.AnimationRef
import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.TextAlignment
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayCloneTiles
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayGroup
import indigo.shared.display.DisplayMutants
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.display.DisplayTextLetters
import indigo.shared.display.SpriteSheetFrame
import indigo.shared.display.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.CloneId
import indigo.shared.scenegraph.CloneTileData
import indigo.shared.scenegraph.CloneTiles
import indigo.shared.scenegraph.DependentNode
import indigo.shared.scenegraph.EntityNode
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Mutants
import indigo.shared.scenegraph.RenderNode
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Sprite
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.TextLine
import indigo.shared.shader.ShaderData
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.time.GameTime

import scala.annotation.nowarn
import scala.annotation.tailrec
import scala.scalajs.js.JSConverters.*

final class DisplayObjectConversions(
    boundaryLocator: BoundaryLocator,
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister
) {

  // Per asset load
  implicit private val textureRefAndOffsetCache: QuickCache[TextureRefAndOffset]           = QuickCache.empty
  implicit private val vector2Cache: QuickCache[Vector2]                                   = QuickCache.empty
  implicit private val frameCache: QuickCache[SpriteSheetFrameCoordinateOffsets]           = QuickCache.empty
  implicit private val listDoCache: QuickCache[scalajs.js.Array[DisplayEntity]]            = QuickCache.empty
  implicit private val cloneBatchCache: QuickCache[DisplayCloneBatch]                      = QuickCache.empty
  implicit private val cloneTilesCache: QuickCache[DisplayCloneTiles]                      = QuickCache.empty
  implicit private val uniformsCache: QuickCache[scalajs.js.Array[Float]]                  = QuickCache.empty
  implicit private val textCloneTileDataCache: QuickCache[scalajs.js.Array[CloneTileData]] = QuickCache.empty
  implicit private val displayObjectCache: QuickCache[DisplayObject]                       = QuickCache.empty

  // Per frame
  implicit private val perFrameAnimCache: QuickCache[Option[AnimationRef]] = QuickCache.empty

  // Called on asset load/reload to account for atlas rebuilding etc.
  def purgeCaches(): Unit = {
    textureRefAndOffsetCache.purgeAllNow()
    vector2Cache.purgeAllNow()
    frameCache.purgeAllNow()
    listDoCache.purgeAllNow()
    cloneBatchCache.purgeAllNow()
    cloneTilesCache.purgeAllNow()
    uniformsCache.purgeAllNow()
    textCloneTileDataCache.purgeAllNow()
    displayObjectCache.purgeAllNow()
    perFrameAnimCache.purgeAllNow()
  }

  def purgeEachFrame(): Unit =
    perFrameAnimCache.purgeAllNow()

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def lookupTexture(assetMapping: AssetMapping, name: AssetName): TextureRefAndOffset =
    QuickCache("tex-" + name.toString) {
      assetMapping.mappings
        .find(p => p._1 == name.toString)
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
          cloneData = batch.cloneData.toJSArray
        )
      }
    else
      new DisplayCloneBatch(
        id = batch.id,
        cloneData = batch.cloneData.toJSArray
      )

  private def cloneTilesDataToDisplayEntities(batch: CloneTiles): DisplayCloneTiles =
    if batch.staticBatchKey.isDefined then
      QuickCache(batch.staticBatchKey.get.toString) {
        new DisplayCloneTiles(
          id = batch.id,
          cloneData = batch.cloneData.toJSArray
        )
      }
    else
      new DisplayCloneTiles(
        id = batch.id,
        cloneData = batch.cloneData.toJSArray
      )

  private def mutantsToDisplayEntities(batch: Mutants): DisplayMutants =
    val uniformDataConvert: Batch[UniformBlock] => scalajs.js.Array[DisplayObjectUniformData] = uniformBlocks =>
      uniformBlocks.toJSArray.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
        )
      }

    new DisplayMutants(
      id = batch.id,
      cloneData = batch.uniformBlocks.toJSArray.map(uniformDataConvert)
    )

  def processSceneNodes(
      sceneNodes: scalajs.js.Array[SceneNode],
      gameTime: GameTime,
      assetMapping: AssetMapping,
      cloneBlankDisplayObjects: => scalajs.js.Dictionary[DisplayObject],
      renderingTechnology: RenderingTechnology,
      maxBatchSize: Int,
      inputEvents: => scalajs.js.Array[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): (scalajs.js.Array[DisplayEntity], scalajs.js.Array[(String, DisplayObject)]) =
    val f =
      sceneNodeToDisplayObject(
        gameTime,
        assetMapping,
        cloneBlankDisplayObjects,
        renderingTechnology,
        maxBatchSize,
        inputEvents,
        sendEvent
      )

    val l = sceneNodes.map { node =>
      node match
        case n: RenderNode[_] =>
          val nn = n.asInstanceOf[n.Out]
          if n.eventHandlerEnabled then
            inputEvents.foreach { e =>
              n.eventHandler((nn, e)).foreach { ee =>
                sendEvent(ee)
              }
            }

        case n: DependentNode[_] =>
          val nn = n.asInstanceOf[n.Out]
          if n.eventHandlerEnabled then
            inputEvents.foreach { e =>
              n.eventHandler((nn, e)).foreach { ee =>
                sendEvent(ee)
              }
            }

      f(node)
    }
    (l.map(_._1), l.foldLeft(scalajs.js.Array[(String, DisplayObject)]())(_ ++ _._2))

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
      cloneBlankDisplayObjects: => scalajs.js.Dictionary[DisplayObject],
      renderingTechnology: RenderingTechnology,
      maxBatchSize: Int,
      inputEvents: => scalajs.js.Array[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  )(sceneNode: SceneNode): (DisplayEntity, scalajs.js.Array[(String, DisplayObject)]) =
    val noClones = scalajs.js.Array[(String, DisplayObject)]()
    sceneNode match {
      case x: Graphic[_] =>
        (graphicToDisplayObject(x, assetMapping), noClones)

      case s: Shape[_] =>
        (shapeToDisplayObject(s), noClones)

      case s: EntityNode[_] =>
        (sceneEntityToDisplayObject(s, assetMapping), noClones)

      case c: CloneBatch =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              cloneBatchDataToDisplayEntities(c)
          },
          noClones
        )

      case c: CloneTiles =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              cloneTilesDataToDisplayEntities(c)
          },
          noClones
        )

      case c: Mutants =>
        (
          cloneBlankDisplayObjects.get(c.id.toString) match {
            case None =>
              DisplayGroup.empty

            case Some(_) =>
              mutantsToDisplayEntities(c)
          },
          noClones
        )

      case g: Group =>
        val children =
          processSceneNodes(
            g.children.toJSArray,
            gameTime,
            assetMapping,
            cloneBlankDisplayObjects,
            renderingTechnology,
            maxBatchSize,
            inputEvents,
            sendEvent
          )
        (
          DisplayGroup(
            groupToMatrix(g),
            children._1
          ),
          children._2
        )

      case x: Sprite[_] =>
        val animation = QuickCache("anim-" + x.bindingKey + x.animationKey + x.animationActions.hashCode.toString) {
          animationsRegister.fetchAnimationForSprite(gameTime, x.bindingKey, x.animationKey, x.animationActions)
        }

        (
          animation match {
            case None =>
              IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${x.animationKey.toString()}")
              DisplayGroup.empty

            case Some(anim) =>
              spriteToDisplayObject(boundaryLocator, x, assetMapping, anim)
          },
          noClones
        )

      case x: Text[_] if renderingTechnology.isWebGL1 || !(x.rotation ~== Radians.zero) =>
        val alignmentOffsetX: Rectangle => Int = lineBounds =>
          x.alignment match {
            case TextAlignment.Left => 0

            case TextAlignment.Center => -(lineBounds.size.width / 2)

            case TextAlignment.Right => -lineBounds.size.width
          }

        val converterFunc: (TextLine, Int, Int) => scalajs.js.Array[DisplayEntity] =
          fontRegister
            .findByFontKey(x.fontKey)
            .map { fontInfo =>
              textLineToDisplayObjects(x, assetMapping, fontInfo)
            }
            .getOrElse { (_, _, _) =>
              IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${x.fontKey.toString()}")
              scalajs.js.Array()
            }

        val letters: scalajs.js.Array[DisplayEntity] =
          boundaryLocator
            .textAsLinesWithBounds(x.text, x.fontKey, x.letterSpacing, x.lineHeight)
            .toJSArray
            .foldLeft(0 -> scalajs.js.Array[DisplayEntity]()) { (acc, textLine) =>
              (
                acc._1 + textLine.lineBounds.height,
                acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1)
              )
            }
            ._2

        (DisplayTextLetters(letters), noClones)

      case x: Text[_] if renderingTechnology.isWebGL2 =>
        val alignmentOffsetX: Rectangle => Int = lineBounds =>
          x.alignment match {
            case TextAlignment.Left => 0

            case TextAlignment.Center => -(lineBounds.size.width / 2)

            case TextAlignment.Right => -lineBounds.size.width
          }

        val converterFunc: (TextLine, Int, Int) => scalajs.js.Array[CloneTileData] =
          fontRegister
            .findByFontKey(x.fontKey)
            .map { fontInfo => (txtLn: TextLine, xPos: Int, yPos: Int) =>
              textLineToDisplayCloneTileData(x, fontInfo)(txtLn, xPos, yPos)
            }
            .getOrElse { (_, _, _) =>
              IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${x.fontKey.toString()}")
              scalajs.js.Array[CloneTileData]()
            }

        val (cloneId, clone) = makeTextCloneDisplayObject(x, assetMapping)

        val letters: scalajs.js.Array[CloneTileData] =
          boundaryLocator
            .textAsLinesWithBounds(x.text, x.fontKey, x.letterSpacing, x.lineHeight)
            .toJSArray
            .foldLeft(
              0 -> scalajs.js.Array[CloneTileData]()
            ) { (acc, textLine) =>
              (
                acc._1 + textLine.lineBounds.height,
                acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1)
              )
            }
            ._2

        (
          DisplayTextLetters(
            letters.grouped(maxBatchSize).toJSArray.map { d =>
              new DisplayCloneTiles(
                id = cloneId,
                cloneData = d
              )
            }
          ),
          scalajs.js.Array((cloneId.toString, clone))
        )

      case _: RenderNode[_] =>
        (DisplayGroup.empty, noClones)

      case _: DependentNode[_] =>
        (DisplayGroup.empty, noClones)
    }

  def optionalAssetToOffset(assetMapping: AssetMapping, maybeAssetName: Option[AssetName]): Vector2 =
    maybeAssetName match {
      case None =>
        Vector2.zero

      case Some(assetName) =>
        lookupTexture(assetMapping, assetName).offset
    }

  def shapeToDisplayObject(leaf: Shape[?]): DisplayObject = {

    val offset = leaf match
      case s: Shape.Box =>
        val size = s.dimensions.size

        if size.width == size.height then Point.zero
        else if size.width < size.height then
          Point(-Math.round((size.height.toDouble - size.width.toDouble) / 2).toInt, 0)
        else Point(0, -Math.round((size.width.toDouble - size.height.toDouble) / 2).toInt)

      case _ =>
        Point.zero

    val boundsActual = BoundaryLocator.untransformedShapeBounds(leaf)

    val shader: ShaderData = Shape.toShaderData(leaf, boundsActual)
    val bounds             = boundsActual.toSquare

    val vec2Zero = Vector2.zero
    val uniformData: scalajs.js.Array[DisplayObjectUniformData] =
      shader.uniformBlocks.toJSArray.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
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
      shaderUniformData = uniformData
    )
  }

  private given CanEqual[Option[TextureRefAndOffset], Option[TextureRefAndOffset]] = CanEqual.derived

  def sceneEntityToDisplayObject(leaf: EntityNode[?], assetMapping: AssetMapping): DisplayObject = {
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

    val uniformData: scalajs.js.Array[DisplayObjectUniformData] =
      shader.uniformBlocks.toJSArray.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
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
      shaderUniformData = uniformData
    )
  }

  def graphicToDisplayObject(leaf: Graphic[?], assetMapping: AssetMapping): DisplayObject = {
    val shaderData     = leaf.material.toShaderData
    val shaderDataHash = shaderData.toCacheKey
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

    val uniformData: scalajs.js.Array[DisplayObjectUniformData] =
      shaderData.uniformBlocks.toJSArray.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
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
      shaderUniformData = uniformData
    )
  }

  def spriteToDisplayObject(
      boundaryLocator: BoundaryLocator,
      leaf: Sprite[?],
      assetMapping: AssetMapping,
      anim: AnimationRef
  ): DisplayObject = {
    val material       = leaf.material
    val shaderData     = material.toShaderData
    val shaderDataHash = shaderData.toCacheKey
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

    val bounds = boundaryLocator.spriteFrameBounds(leaf).getOrElse(Rectangle.zero)

    val shaderId = shaderData.shaderId

    val uniformData: scalajs.js.Array[DisplayObjectUniformData] =
      shaderData.uniformBlocks.toJSArray.map { ub =>
        DisplayObjectUniformData(
          uniformHash = ub.uniformHash,
          blockName = ub.blockName.toString,
          data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
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
      shaderUniformData = uniformData
    )
  }

  def textLineToDisplayObjects(
      leaf: Text[?],
      assetMapping: AssetMapping,
      fontInfo: FontInfo
  ): (TextLine, Int, Int) => scalajs.js.Array[DisplayEntity] =
    (line, alignmentOffsetX, yOffset) => {

      val material       = leaf.material
      val shaderData     = material.toShaderData
      val shaderDataHash = shaderData.toCacheKey
      val materialName   = shaderData.channel0.get

      val lineHash: String =
        "[indigo_txt]" +
          leaf.material.hashCode.toString +
          leaf.position.hashCode.toString +
          leaf.scale.hashCode.toString +
          leaf.rotation.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.flip.horizontal.toString +
          leaf.flip.vertical.toString +
          leaf.fontKey.toString +
          line.hashCode.toString

      val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
      val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
      val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

      val texture = lookupTexture(assetMapping, materialName)

      val shaderId = shaderData.shaderId

      val uniformData: scalajs.js.Array[DisplayObjectUniformData] =
        shaderData.uniformBlocks.toJSArray.map { ub =>
          DisplayObjectUniformData(
            uniformHash = ub.uniformHash,
            blockName = ub.blockName.toString,
            data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
          )
        }

      QuickCache(lineHash) {
        zipWithCharDetails(line.text.toArray, fontInfo, leaf.letterSpacing).map { case (fontChar, xPosition) =>
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
            refX = (leaf.ref.x + -(xPosition + alignmentOffsetX)).toFloat,
            refY = (leaf.ref.y - yOffset).toFloat,
            flipX = if leaf.flip.horizontal then -1.0 else 1.0,
            flipY = if leaf.flip.vertical then -1.0 else 1.0,
            rotation = leaf.rotation,
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
            shaderUniformData = uniformData
          )
        }
      }
    }

  def makeTextCloneDisplayObject(
      leaf: Text[?],
      assetMapping: AssetMapping
  ): (CloneId, DisplayObject) = {

    val cloneId: CloneId =
      CloneId(
        "[indigo_txt_clone]" +
          leaf.material.hashCode.toString +
          leaf.position.hashCode.toString +
          leaf.scale.hashCode.toString +
          leaf.rotation.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.flip.horizontal.toString +
          leaf.flip.vertical.toString +
          leaf.fontKey.toString
      )

    val clone =
      QuickCache(s"[indigo_text_clone_ref][${cloneId.toString}]") {
        val material       = leaf.material
        val shaderData     = material.toShaderData
        val shaderDataHash = shaderData.toCacheKey
        val materialName   = shaderData.channel0.get
        val emissiveOffset = findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
        val normalOffset   = findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
        val specularOffset = findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")
        val texture        = lookupTexture(assetMapping, materialName)
        val shaderId       = shaderData.shaderId

        val uniformData: scalajs.js.Array[DisplayObjectUniformData] =
          shaderData.uniformBlocks.toJSArray.map { ub =>
            DisplayObjectUniformData(
              uniformHash = ub.uniformHash,
              blockName = ub.blockName.toString,
              data = DisplayObjectConversions.packUBO(ub.uniforms, ub.uniformHash, false)
            )
          }

        val frameInfo =
          SpriteSheetFrame.calculateFrameOffset(
            atlasSize = texture.atlasSize,
            frameCrop = Rectangle.one,
            textureOffset = texture.offset
          )

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
          width = 1,
          height = 1,
          atlasName = Some(texture.atlasName),
          frame = frameInfo,
          channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
          channelOffset2 = frameInfo.offsetToCoords(normalOffset),
          channelOffset3 = frameInfo.offsetToCoords(specularOffset),
          texturePosition = texture.offset,
          textureSize = texture.size,
          atlasSize = texture.atlasSize,
          shaderId = shaderId,
          shaderUniformData = uniformData
        )
      }

    (
      cloneId,
      clone
    )
  }

  def textLineToDisplayCloneTileData(
      leaf: Text[?],
      fontInfo: FontInfo
  ): (TextLine, Int, Int) => scalajs.js.Array[CloneTileData] =
    (line, alignmentOffsetX, yOffset) => {
      val lineHash: String =
        "[indigo_tln]" +
          leaf.position.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.scale.hashCode.toString +
          line.hashCode.toString +
          leaf.fontKey.toString

      QuickCache(lineHash) {
        zipWithCharDetails(line.text.toArray, fontInfo, leaf.letterSpacing).map { case (fontChar, xPosition) =>
          CloneTileData(
            x = leaf.position.x + leaf.ref.x + xPosition + alignmentOffsetX,
            y = leaf.position.y + leaf.ref.y + yOffset,
            rotation = Radians.zero,
            scaleX = leaf.scale.x.toFloat,
            scaleY = leaf.scale.y.toFloat,
            cropX = fontChar.bounds.x,
            cropY = fontChar.bounds.y,
            cropWidth = fontChar.bounds.width,
            cropHeight = fontChar.bounds.height
          )
        }
      }
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var accCharDetails: scalajs.js.Array[(FontChar, Int)] = new scalajs.js.Array()

  @nowarn("msg=unused")
  private def zipWithCharDetails(
      charList: Array[Char],
      fontInfo: FontInfo,
      letterSpacing: Int
  ): scalajs.js.Array[(FontChar, Int)] = {
    @tailrec
    def rec(remaining: scalajs.js.Array[(Char, FontChar)], nextX: Int): scalajs.js.Array[(FontChar, Int)] =
      if remaining.isEmpty then accCharDetails
      else
        val x  = remaining.head
        val xs = remaining.tail
        (x._2, nextX) +=: accCharDetails

        val ls = if xs.isEmpty then 0 else letterSpacing
        rec(xs, nextX + x._2.bounds.width + ls)

    accCharDetails = new scalajs.js.Array()
    rec(charList.toJSArray.map(c => (c, fontInfo.findByCharacter(c))), 0)
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

  extension (sd: ShaderData)
    def toCacheKey: String =
      sd.shaderId.toString +
        sd.channel0.map(_.toString).getOrElse("") +
        sd.channel1.map(_.toString).getOrElse("") +
        sd.channel2.map(_.toString).getOrElse("") +
        sd.channel3.map(_.toString).getOrElse("") +
        sd.uniformBlocks.map(_.uniformHash).mkString
}

object DisplayObjectConversions {

  private val empty0: scalajs.js.Array[Float] = scalajs.js.Array[Float]()
  private val empty1: scalajs.js.Array[Float] = scalajs.js.Array[Float](0.0f)
  private val empty2: scalajs.js.Array[Float] = scalajs.js.Array[Float](0.0f, 0.0f)
  private val empty3: scalajs.js.Array[Float] = scalajs.js.Array[Float](0.0f, 0.0f, 0.0f)

  def expandTo4(arr: scalajs.js.Array[Float]): scalajs.js.Array[Float] =
    arr.length match {
      case 0 => arr
      case 1 => arr ++ empty3
      case 2 => arr ++ empty2
      case 3 => arr ++ empty1
      case 4 => arr
      case _ => arr
    }

  // takes a list because only converted to JSArray if value not cached.
  def packUBO(
      uniforms: Batch[(Uniform, ShaderPrimitive)],
      cacheKey: String,
      disableCache: Boolean
  )(using QuickCache[scalajs.js.Array[Float]]): scalajs.js.Array[Float] = {
    @tailrec
    def rec(
        remaining: scalajs.js.Array[ShaderPrimitive],
        current: scalajs.js.Array[Float],
        acc: scalajs.js.Array[Float]
    ): scalajs.js.Array[Float] =
      remaining match
        case us if us.isEmpty =>
          // println(s"done, expanded: ${current.toList} to ${expandTo4(current).toList}")
          // println(s"result: ${(acc ++ expandTo4(current)).toList}")
          acc ++ expandTo4(current)

        case us if current.length == 4 =>
          // println(s"current full, sub-result: ${(acc ++ current).toList}")
          rec(us, empty0, acc ++ current)

        case us if current.isEmpty && us.head.isArray =>
          // println(s"Found an array, current is empty, set current to: ${u.toArray.toList}")
          rec(us.tail, us.head.toJSArray, acc)

        case us if current.length == 1 && us.head.length == 2 =>
          // println("Current value is float, must not straddle byte boundary when adding vec2")
          rec(us.tail, current ++ scalajs.js.Array(0.0f) ++ us.head.toJSArray, acc)

        case us if current.length + us.head.length > 4 =>
          // println(s"doesn't fit, expanded: ${current.toList} to ${expandTo4(current).toList},  sub-result: ${(acc ++ expandTo4(current)).toList}")
          rec(us, empty0, acc ++ expandTo4(current))

        case us if us.head.isArray =>
          // println(s"fits but next value is array, expanded: ${current.toList} to ${expandTo4(current).toList},  sub-result: ${(acc ++ expandTo4(current)).toList}")
          rec(us, empty0, acc ++ current)

        case us =>
          // println(s"fits, current is now: ${(current ++ u.toArray).toList}")
          rec(us.tail, current ++ us.head.toJSArray, acc)

    QuickCache(cacheKey, disableCache) {
      rec(uniforms.toJSArray.map(_._2), empty0, empty0)
    }
  }

}
