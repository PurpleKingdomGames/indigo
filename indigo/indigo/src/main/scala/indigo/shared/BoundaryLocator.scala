package indigo.shared

import indigo.shared.scenegraph.SceneNode
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.TextLine
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.Point
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

final class BoundaryLocator(animationsRegister: AnimationsRegister, fontRegister: FontRegister) {

  implicit private val boundsCache: QuickCache[Boundary]          = QuickCache.empty
  implicit private val textLinesCache: QuickCache[List[TextLine]] = QuickCache.empty

  def purgeCache(): Unit = {
    boundsCache.purgeAllNow()
    textLinesCache.purgeAllNow()
  }

  // General
  def findBounds(sceneGraphNode: SceneNode): Boundary =
    sceneGraphNode match {
      case s: Shape =>
        Boundary.Found(s.bounds)

      case g: Graphic =>
        Boundary.Found(g.bounds)

      case t: TextBox =>
        Boundary.Unavailable

      case s: EntityNode =>
        Boundary.Found(s.bounds)

      case g: Group =>
        g.calculatedBounds(this)

      case _: Transformer =>
        Boundary.Unavailable

      case _: Clone =>
        Boundary.Unavailable

      case _: CloneBatch =>
        Boundary.Unavailable

      case s: Sprite =>
        spriteBounds(s)

      case t: Text =>
        textBounds(t)

      case _ =>
        Boundary.Unavailable
    }

  def spriteBounds(sprite: Sprite): Boundary =
    QuickCache(s"""sprite-${sprite.bindingKey}-${sprite.animationKey}""") {
      animationsRegister.fetchAnimationInLastState(sprite.bindingKey, sprite.animationKey) match {
        case Some(animation) =>
          Boundary.Found(Rectangle(sprite.position, animation.currentFrame.crop.size))

        case None =>
          IndigoLogger.errorOnce(s"Cannot build bounds for Sprite with bindingKey: ${sprite.bindingKey.toString()}")
          Boundary.Unavailable
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

  def textBounds(text: Text): Boundary =
    QuickCache(s"""text-bounds-${text.fontKey}-${text.text}""") {
      val unaligned =
        textAsLinesWithBounds(text.text, text.fontKey)
          .map(_.lineBounds)
          .fold(Rectangle.zero) { (acc, next) =>
            acc.resize(Point(Math.max(acc.width, next.width), acc.height + next.height))
          }
          .moveTo(text.position)

      (text.alignment, unaligned) match {
        case (TextAlignment.Left, b) =>
          Boundary.Found(b)

        case (TextAlignment.Center, b) =>
          Boundary.Found(b.moveTo(Point(b.x - (b.width / 2), b.y)))

        case (TextAlignment.Right, b) =>
          Boundary.Found(b.moveTo(Point(b.x - b.width, b.y)))
      }
    }

}

enum Boundary derives CanEqual:
  case Unavailable              extends Boundary
  case Calculating              extends Boundary
  case Found(bounds: Rectangle) extends Boundary

object Boundary:
  extension (b: Boundary)
    def toOption: Option[Rectangle] =
      b match
        case Boundary.Found(r) => Some(r)
        case _                 => None
