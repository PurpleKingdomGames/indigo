package indigo.shared

import indigo.platform.assets.DynamicText
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.TextAlignment
import indigo.shared.datatypes.Vector3
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.CloneTiles
import indigo.shared.scenegraph.EntityNode
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Mutants
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Sprite
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.TextBox
import indigo.shared.scenegraph.TextLine

final class BoundaryLocator(
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister,
    dynamicText: DynamicText
):

  implicit private val maybeBoundsCache: QuickCache[Option[Rectangle]]      = QuickCache.empty
  implicit private val boundsCache: QuickCache[Rectangle]                   = QuickCache.empty
  implicit private val textLinesCache: QuickCache[List[TextLine]]           = QuickCache.empty
  implicit private val textAllLineBoundsCache: QuickCache[Array[Rectangle]] = QuickCache.empty

  private[indigo] def purgeCache(): Unit = {
    maybeBoundsCache.purgeAllNow()
    boundsCache.purgeAllNow()
    textLinesCache.purgeAllNow()
    textAllLineBoundsCache.purgeAllNow()
  }

  /** Measures the size of a `TextBox` using the browsers canvas APIs. This is a slow operation.
    */
  def measureText(textBox: TextBox): Rectangle =
    val rect =
      dynamicText
        .measureText(
          textBox.text,
          textBox.style,
          textBox.size.width,
          textBox.size.height
        )
        .moveTo(textBox.position)

    BoundaryLocator.findBounds(textBox, rect.position, rect.size, textBox.ref)

  /** Safely finds the bounds of any given scene node, if the node has bounds. It is not possible to sensibly measure the
    * bounds of some node types, such as clones, and some nodes are dependant on external data that may be missing.
    */
  def findBounds(sceneNode: SceneNode): Option[Rectangle] =
    sceneNode match {
      case s: Shape[_] =>
        Option(BoundaryLocator.findShapeBounds(s))

      case g: Graphic[_] =>
        Option(g.bounds)

      case t: TextBox =>
        Option(t.bounds)

      case s: EntityNode[_] =>
        Option(BoundaryLocator.findBounds(s, s.position, s.size, s.ref))

      case g: Group =>
        Option(groupBounds(g))

      case _: CloneBatch =>
        None

      case _: CloneTiles =>
        None

      case _: Mutants =>
        None

      case s: Sprite[_] =>
        spriteBounds(s)

      case t: Text[_] =>
        Option(textBounds(t))

      case _ =>
        None
    }

  /** Finds the bounds or returns a `Rectangle` of size zero for convenience.
    */
  def bounds(sceneNode: SceneNode): Rectangle =
    findBounds(sceneNode).getOrElse(Rectangle.zero)

  private def groupBounds(group: Group): Rectangle =
    val rect =
      group.children match {
        case Nil =>
          Rectangle.zero

        case x :: xs =>
          xs.foldLeft(findBounds(x)) { (acc, node) =>
            (acc, findBounds(node)) match
              case (Some(a), Some(b)) => Option(Rectangle.expandToInclude(a, b))
              case (r @ Some(_), _)   => r
              case (_, r @ Some(_))   => r
              case (r, _)             => r
          }.map(_.moveBy(group.position))
            .getOrElse(Rectangle.zero)
      }

    BoundaryLocator.findBounds(group, rect.position, rect.size, group.ref)
  end groupBounds

  def spriteFrameBounds(sprite: Sprite[_]): Option[Rectangle] =
    QuickCache(s"""sprite-${sprite.bindingKey.toString}-${sprite.animationKey.toString}""") {
      animationsRegister.fetchAnimationInLastState(sprite.bindingKey, sprite.animationKey) match {
        case Some(animation) =>
          Option(Rectangle(sprite.position, animation.currentFrame.crop.size))

        case None =>
          IndigoLogger.errorOnce(s"Cannot build bounds for Sprite with bindingKey: ${sprite.bindingKey.toString()}")
          None
      }
    }

  private def spriteBounds(sprite: Sprite[_]): Option[Rectangle] =
    spriteFrameBounds(sprite).map(rect => BoundaryLocator.findBounds(sprite, rect.position, rect.size, sprite.ref))

  // Text / Fonts

  def textLineBounds(lineText: String, fontInfo: FontInfo): Rectangle =
    QuickCache(s"""textline-${fontInfo.fontKey}-$lineText""") {
      lineText
        .toCharArray()
        .map(c => fontInfo.findByCharacter(c).bounds)
        .foldLeft(Rectangle.zero) { (acc, curr) =>
          Rectangle(0, 0, acc.width + curr.width, Math.max(acc.height, curr.height))
        }
    }

  def textAsLinesWithBounds(text: String, fontKey: FontKey): List[TextLine] =
    QuickCache(s"""text-lines-$fontKey-$text""") {
      fontRegister
        .findByFontKey(fontKey)
        .map { fontInfo =>
          text.linesIterator.toList
            .map(lineText => new TextLine(lineText, textLineBounds(lineText, fontInfo)))
            .foldLeft((0, List[TextLine]())) { case ((yPos, lines), textLine) =>
              (yPos + textLine.lineBounds.height, lines ++ List(textLine.moveTo(0, yPos)))
            }
            ._2
        }
        .getOrElse {
          IndigoLogger.errorOnce(s"Cannot build Text lines, missing Font with key: ${fontKey.toString()}")
          Nil
        }
    }

  def textAllLineBounds(text: String, fontKey: FontKey): Array[Rectangle] =
    QuickCache(s"""text-all-line-bounds-$fontKey-$text""") {
      fontRegister
        .findByFontKey(fontKey)
        .map { fontInfo =>
          text.linesIterator.toArray
            .map(lineText => textLineBounds(lineText, fontInfo))
            .foldLeft((0, Array[Rectangle]())) { case ((yPos, lines), lineBounds) =>
              (yPos + lineBounds.height, lines ++ Array(lineBounds.moveTo(0, yPos)))
            }
            ._2
        }
        .getOrElse {
          IndigoLogger.errorOnce(s"Cannot build Text line bounds, missing Font with key: ${fontKey.toString()}")
          Array()
        }
    }

  def textBounds(text: Text[_]): Rectangle =
    val unaligned =
      textAllLineBounds(text.text, text.fontKey)
        .fold(Rectangle.zero) { (acc, next) =>
          acc.resize(Size(Math.max(acc.width, next.width), acc.height + next.height))
        }

    val rect =
      unaligned.moveTo(text.position)

    val offset: Int =
      text.alignment match {
        case TextAlignment.Left   => 0
        case TextAlignment.Center => rect.size.width / 2
        case TextAlignment.Right  => rect.size.width
      }

    BoundaryLocator.findBounds(text, rect.position, rect.size, text.ref + Point(offset, 0))

object BoundaryLocator:
  def findBounds(entity: SceneNode, position: Point, size: Size, ref: Point): Rectangle =
    val m =
      CheapMatrix4.identity
        .translate(-ref.x.toFloat, -ref.y.toFloat, 0.0f)
        .rotate(entity.rotation.toFloat)
        .scale(entity.scale.x.toFloat, entity.scale.y.toFloat, 1.0f)
        .translate(position.x.toFloat, position.y.toFloat, 0.0f)

    Rectangle.fromPoints(
      m.transform(Vector3(0, 0, 0)).toPoint,
      m.transform(Vector3(size.width, 0, 0)).toPoint,
      m.transform(Vector3(size.width, size.height, 0)).toPoint,
      m.transform(Vector3(0, size.height, 0)).toPoint
    )

  def untransformedShapeBounds(shape: Shape[_]): Rectangle =
    shape match
      case s: Shape.Box =>
        Rectangle(
          (s.dimensions.position - (s.stroke.width / 2)),
          s.dimensions.size + s.stroke.width
        )

      case s: Shape.Circle =>
        Rectangle(
          s.position,
          Size(s.radius * 2) + s.stroke.width
        )

      case s: Shape.Line =>
        Rectangle(s.position, s.size)

      case s: Shape.Polygon =>
        Rectangle.fromPointCloud(s.vertices).expand(s.stroke.width / 2)

  def findShapeBounds(shape: Shape[_]): Rectangle =
    val rect = untransformedShapeBounds(shape)
    findBounds(shape, rect.position, rect.size, shape.ref)
