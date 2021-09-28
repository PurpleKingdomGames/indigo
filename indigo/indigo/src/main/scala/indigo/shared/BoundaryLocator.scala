package indigo.shared

import indigo.shared.scenegraph.SceneNode
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.TextLine
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.TextAlignment
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.TextBox
import indigo.shared.scenegraph.Sprite
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Clone
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Transformer
import indigo.shared.datatypes.FontKey
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.EntityNode
import indigo.shared.scenegraph.SceneNode
import indigo.platform.assets.DynamicText
import indigo.shared.datatypes.Vector3
import indigo.shared.platform.DisplayObjectConversions
import indigo.shared.datatypes.mutable.CheapMatrix4

final class BoundaryLocator(
    animationsRegister: AnimationsRegister,
    fontRegister: FontRegister,
    dynamicText: DynamicText
) {

  implicit private val maybeBoundsCache: QuickCache[Option[Rectangle]] = QuickCache.empty
  implicit private val boundsCache: QuickCache[Rectangle]              = QuickCache.empty
  implicit private val textLinesCache: QuickCache[List[TextLine]]      = QuickCache.empty

  def purgeCache(): Unit = {
    maybeBoundsCache.purgeAllNow()
    boundsCache.purgeAllNow()
    textLinesCache.purgeAllNow()
  }

  def measureText(t: TextBox): Rectangle =
    val rect =
      dynamicText
        .measureText(
          t.text,
          t.style,
          t.size.width,
          t.size.height
        )
        .moveTo(t.position)

    BoundaryLocator.findBounds(t, rect.position, rect.size, t.ref)

  def findBounds(sceneGraphNode: SceneNode): Option[Rectangle] =
    sceneGraphNode match {
      case s: Shape =>
        Option(shapeBounds(s)).map(rect => BoundaryLocator.findBounds(s, rect.position, rect.size, s.ref))

      case g: Graphic[_] =>
        Option(g.bounds)

      case t: TextBox =>
        Option(t.bounds)

      case s: EntityNode =>
        Option(BoundaryLocator.findBounds(s, s.position, s.size, s.ref))

      case g: Group =>
        Option(groupBounds(g)).map(rect => BoundaryLocator.findBounds(g, rect.position, rect.size, g.ref))

      case _: Clone =>
        None

      case _: CloneBatch =>
        None

      case s: Sprite[_] =>
        spriteBounds(s).map(rect => BoundaryLocator.findBounds(s, rect.position, rect.size, s.ref))

      case t: Text[_] =>
        Option(textBounds(t)).map { rect =>

          val offset: Int =
            t.alignment match {
              case TextAlignment.Left   => 0
              case TextAlignment.Center => rect.size.width / 2
              case TextAlignment.Right  => rect.size.width
            }

          BoundaryLocator.findBounds(t, rect.position, rect.size, t.ref + Point(offset, 0))
        }

      case _ =>
        None
    }

  def groupBounds(group: Group): Rectangle =
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

  def spriteBounds(sprite: Sprite[_]): Option[Rectangle] =
    QuickCache(s"""sprite-${sprite.bindingKey.toString}-${sprite.animationKey.toString}""") {
      animationsRegister.fetchAnimationInLastState(sprite.bindingKey, sprite.animationKey) match {
        case Some(animation) =>
          Option(Rectangle(sprite.position, animation.currentFrame.crop.size))

        case None =>
          IndigoLogger.errorOnce(s"Cannot build bounds for Sprite with bindingKey: ${sprite.bindingKey.toString()}")
          None
      }
    }

  // Text / Fonts

  private def textLineBounds(lineText: String, fontInfo: FontInfo): Rectangle =
    lineText
      .toCharArray()
      .map(c => fontInfo.findByCharacter(c).bounds)
      .foldLeft(Rectangle.zero) { (acc, curr) =>
        Rectangle(0, 0, acc.width + curr.width, Math.max(acc.height, curr.height))
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

  def textBounds(text: Text[_]): Rectangle =
    val unaligned =
      textAsLinesWithBounds(text.text, text.fontKey)
        .map(_.lineBounds)
        .fold(Rectangle.zero) { (acc, next) =>
          acc.resize(Size(Math.max(acc.width, next.width), acc.height + next.height))
        }

    unaligned.moveTo(text.position)

  def shapeBounds(shape: Shape): Rectangle =
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

}

object BoundaryLocator:
  def findBounds(entity: SceneNode, position: Point, size: Size, ref: Point): Rectangle =
    val m =
      CheapMatrix4.identity
        .translate(-ref.x.toFloat, -ref.y.toFloat, 0.0f)
        .rotate(entity.rotation.toFloat)
        .scale(entity.scale.x.toFloat, entity.scale.y.toFloat, 1.0f)
        .translate(position.x.toFloat, position.y.toFloat, 0.0f)

    Rectangle.fromPointCloud(
      List(
        m.transform(Vector3(0, 0, 0)).toPoint,
        m.transform(Vector3(size.width, 0, 0)).toPoint,
        m.transform(Vector3(size.width, size.height, 0)).toPoint,
        m.transform(Vector3(0, size.height, 0)).toPoint
      )
    )
