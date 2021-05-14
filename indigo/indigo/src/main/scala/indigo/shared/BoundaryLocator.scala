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

  implicit private val boundsCache: QuickCache[Option[Rectangle]] = QuickCache.empty
  implicit private val textLinesCache: QuickCache[List[TextLine]] = QuickCache.empty

  def purgeCache(): Unit = {
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

    BoundaryLocator.findBounds(t, rect.position, rect.size)

  def findBounds(sceneGraphNode: SceneNode): Option[Rectangle] =
    sceneGraphNode match {
      case s: Shape =>
        Option(shapeBounds(s)).map(rect => BoundaryLocator.findBounds(s, rect.position, rect.size))

      case g: Graphic =>
        Option(g.bounds)

      case t: TextBox =>
        Option(t.bounds)

      case s: EntityNode =>
        Option(BoundaryLocator.findBounds(s, s.position, s.size))

      case g: Group =>
        Option(groupBounds(g)).map(rect => BoundaryLocator.findBounds(g, rect.position, rect.size))

      case _: Clone =>
        None

      case _: CloneBatch =>
        None

      case s: Sprite =>
        spriteBounds(s).map(rect => BoundaryLocator.findBounds(s, rect.position, rect.size))

      case t: Text =>
        textBounds(t).map { rect =>
          val tt = t.alignment match
            case TextAlignment.Left   => t
            case TextAlignment.Center => t.withRef((rect.width / 2), (rect.height / 2))
            case TextAlignment.Right  => t.withRef(rect.width, (rect.height / 2))

          BoundaryLocator.findBounds(
            tt,
            t.position,
            rect.size
          )
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

  def spriteBounds(sprite: Sprite): Option[Rectangle] =
    QuickCache(s"""sprite-${sprite.bindingKey}-${sprite.animationKey}""") {
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

  def textBounds(text: Text): Option[Rectangle] =
    QuickCache(s"""text-bounds-${text.fontKey}-${text.text}""") {
      val unaligned =
        textAsLinesWithBounds(text.text, text.fontKey)
          .map(_.lineBounds)
          .fold(Rectangle.zero) { (acc, next) =>
            acc.resize(Size(Math.max(acc.width, next.width), acc.height + next.height))
          }

      val res =
        (text.alignment, unaligned) match
          case (TextAlignment.Left, b) =>
            Option(b)

          case (TextAlignment.Center, b) =>
            Option(b.moveTo(Point(b.x - (b.width / 2), b.y)))

          case (TextAlignment.Right, b) =>
            Option(b.moveTo(Point(b.x - b.width, b.y)))

      res.map(_.moveTo(text.position))
    }

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

  def findBounds(entity: SceneNode, position: Point, size: Size): Rectangle =
    val m =
      CheapMatrix4.identity
        .translate(-entity.ref.x, -entity.ref.y, 0.0d)
        .rotate(entity.rotation)
        .scale(entity.scale.x, entity.scale.y, 1)
        .translate(position.x, position.y, 0.0d)

    Rectangle.fromPointCloud(
      List(
        m.transform(Vector3(0, 0, 0)).toPoint,
        m.transform(Vector3(size.width, 0, 0)).toPoint,
        m.transform(Vector3(size.width, size.height, 0)).toPoint,
        m.transform(Vector3(0, size.height, 0)).toPoint
      )
    )
