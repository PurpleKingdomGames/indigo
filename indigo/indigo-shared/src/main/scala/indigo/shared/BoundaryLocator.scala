package indigo.shared

import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.TextLine
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.TextAlignment
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.Sprite
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Clone
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Transformer
import indigo.shared.datatypes.FontKey
import indigo.shared.scenegraph.Shape

final class BoundaryLocator(animationsRegister: AnimationsRegister, fontRegister: FontRegister) {

  implicit private val boundsCache: QuickCache[Rectangle]         = QuickCache.empty
  implicit private val textLinesCache: QuickCache[List[TextLine]] = QuickCache.empty

  def purgeCache(): Unit = {
    boundsCache.purgeAllNow()
    textLinesCache.purgeAllNow()
  }

  // General
  def findBounds(sceneGraphNode: SceneGraphNode): Rectangle =
    sceneGraphNode match {
      case s: Shape =>
        s.bounds

      case g: Group =>
        groupBounds(g)

      case _: Transformer =>
        Rectangle.zero

      case _: Clone =>
        Rectangle.zero

      case _: CloneBatch =>
        Rectangle.zero

      case g: Graphic =>
        graphicBounds(g)

      case s: Sprite =>
        spriteBounds(s)

      case t: Text =>
        textBounds(t)
    }

  def groupBounds(group: Group): Rectangle =
    group.bounds(this)

  def graphicBounds(graphic: Graphic): Rectangle =
    graphic.lazyBounds

  def spriteBounds(sprite: Sprite): Rectangle =
    QuickCache(s"""sprite-${sprite.bindingKey.value}-${sprite.animationKey.value}""") {
      animationsRegister.fetchAnimationInLastState(sprite.bindingKey, sprite.animationKey) match {
        case Some(animation) =>
          Rectangle(sprite.position, animation.currentFrame.crop.size)

        case None =>
          IndigoLogger.errorOnce(s"Cannot build bounds for Sprite with bindingKey: ${sprite.bindingKey.toString()}")
          Rectangle(sprite.position, Point.zero)
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
    QuickCache(s"""text-lines-${fontKey.key}-${text}""") {
      fontRegister
        .findByFontKey(fontKey)
        .map { fontInfo =>
          text.linesIterator.toList
            .map(lineText => new TextLine(lineText, textLineBounds(lineText, fontInfo)))
            .foldLeft((0, List[TextLine]())) {
              case ((yPos, lines), textLine) =>
                (yPos + textLine.lineBounds.height, lines ++ List(textLine.moveTo(0, yPos)))
            }
            ._2
        }
        .getOrElse {
          IndigoLogger.errorOnce(s"Cannot build Text lines, missing Font with key: ${fontKey.toString()}")
          Nil
        }
    }

  def textBounds(text: Text): Rectangle =
    QuickCache(s"""text-bounds-${text.fontKey.key}-${text.text}""") {
      val unaligned =
        textAsLinesWithBounds(text.text, text.fontKey)
          .map(_.lineBounds)
          .fold(Rectangle.zero) { (acc, next) =>
            acc.resize(Point(Math.max(acc.width, next.width), acc.height + next.height))
          }
          .moveTo(text.position)

      (text.alignment, unaligned) match {
        case (TextAlignment.Left, b)   => b
        case (TextAlignment.Center, b) => b.moveTo(Point(b.x - (b.width / 2), b.y))
        case (TextAlignment.Right, b)  => b.moveTo(Point(b.x - b.width, b.y))
      }
    }

}
