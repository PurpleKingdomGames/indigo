package indigoextras.ui.components.datatypes

import indigo.*
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

object ContainerLikeFunctions:

  extension (b: Bounds)
    def withPadding(p: Padding): Bounds =
      b.moveBy(p.left, p.top).resize(b.width + p.right, b.height + p.bottom)

  def calculateNextOffset[ReferenceData](containerDimensions: Dimensions, layout: ComponentLayout)(
      context: UIContext[ReferenceData],
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Coords =
    layout match
      case ComponentLayout.Horizontal(padding, Overflow.Hidden) =>
        components
          .takeRight(1)
          .headOption
          .map(c => c.offset + Coords(c.component.bounds(context, c.model).withPadding(padding).right, 0))
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Horizontal(padding, Overflow.Wrap) =>
        val maxY = components
          .map(c => c.offset.y + c.component.bounds(context, c.model).withPadding(padding).height)
          .sortWith(_ > _)
          .headOption
          .getOrElse(0)

        components
          .takeRight(1)
          .headOption
          .map { c =>
            val padded      = c.component.bounds(context, c.model).withPadding(padding)
            val maybeOffset = c.offset + Coords(padded.right, 0)

            if padded.moveBy(maybeOffset).right < containerDimensions.width then maybeOffset
            else Coords(padding.left, maxY)
          }
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Vertical(padding) =>
        components
          .takeRight(1)
          .headOption
          .map(c => c.offset + Coords(0, c.component.bounds(context, c.model).withPadding(padding).bottom))
          .getOrElse(Coords(padding.left, padding.top))

  def present[ReferenceData](
      context: UIContext[ReferenceData],
      dimensions: Dimensions,
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Outcome[Layer] =
    components
      .map { c =>
        c.component.present(
          context.withParentBounds(Bounds(context.parent.bounds.moveBy(c.offset).coords, dimensions)),
          c.model
        )
      }
      .sequence
      .map(_.foldLeft(Layer.Stack.empty)(_ :+ _))
