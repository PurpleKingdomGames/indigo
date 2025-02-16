package indigoextras.ui.components

import indigo.*
import indigoextras.ui.component.*
import indigoextras.ui.components.datatypes.Anchor
import indigoextras.ui.components.datatypes.BoundsMode
import indigoextras.ui.components.datatypes.ComponentEntry
import indigoextras.ui.components.datatypes.ComponentId
import indigoextras.ui.components.datatypes.ContainerLikeFunctions
import indigoextras.ui.components.datatypes.FitMode
import indigoextras.ui.datatypes.*
import indigoextras.ui.shaders.LayerMask

/** Describes a fixed arrangement of components, manages their layout, which may include anchored components, and masks
  * the content outside of the pane. Like a ScrollPane without the scrolling!
  */
final case class MaskedPane[A, ReferenceData] private[components] (
    boundsMode: BoundsMode,
    dimensions: Dimensions, // The actual cached dimensions of the scroll pane
    contentBounds: Bounds,  // The calculated and cached bounds of the content
    // Components
    content: ComponentEntry[A, ReferenceData]
):

  def withContent[B](component: B)(using
      c: Component[B, ReferenceData]
  ): MaskedPane[B, ReferenceData] =
    this.copy(
      content = MaskedPane.makeComponentEntry(component)
    )

  def withDimensions(value: Dimensions): MaskedPane[A, ReferenceData] =
    this.copy(dimensions = value)

  def withBoundsMode(value: BoundsMode): MaskedPane[A, ReferenceData] =
    this.copy(boundsMode = value)

object MaskedPane:

  private def makeComponentEntry[A, ReferenceData](
      content: A
  )(using c: Component[A, ReferenceData]): ComponentEntry[A, ReferenceData] =
    ComponentEntry(
      ComponentId("scroll pane component"),
      Coords.zero,
      content,
      c,
      None
    )

  def apply[A, ReferenceData](
      content: A
  )(using c: Component[A, ReferenceData]): MaskedPane[A, ReferenceData] =
    MaskedPane(
      indigoextras.ui.components.datatypes.BoundsMode.default,
      Dimensions.zero,
      Bounds.zero,
      MaskedPane.makeComponentEntry(content)
    )

  def apply[A, ReferenceData](
      boundsMode: BoundsMode,
      content: A
  )(using
      c: Component[A, ReferenceData]
  ): MaskedPane[A, ReferenceData] =
    MaskedPane(
      boundsMode,
      Dimensions.zero,
      Bounds.zero,
      MaskedPane.makeComponentEntry(content)
    )

  def apply[A, ReferenceData](
      dimensions: Dimensions,
      content: A
  )(using
      c: Component[A, ReferenceData]
  ): MaskedPane[A, ReferenceData] =
    MaskedPane(
      indigoextras.ui.components.datatypes.BoundsMode.fixed(dimensions),
      dimensions,
      Bounds.zero,
      MaskedPane.makeComponentEntry(content)
    )

  def apply[A, ReferenceData](
      width: Int,
      height: Int,
      content: A
  )(using
      c: Component[A, ReferenceData]
  ): MaskedPane[A, ReferenceData] =
    MaskedPane(
      Dimensions(width, height),
      content
    )

  given [A, ReferenceData]: Component[MaskedPane[A, ReferenceData], ReferenceData] with

    def bounds(context: UIContext[ReferenceData], model: MaskedPane[A, ReferenceData]): Bounds =
      Bounds(model.dimensions)

    def updateModel(
        context: UIContext[ReferenceData],
        model: MaskedPane[A, ReferenceData]
    ): GlobalEvent => Outcome[MaskedPane[A, ReferenceData]] =
      case FrameTick =>
        // Sub-groups will naturally refresh themselves as needed
        updateComponents(context, model)(FrameTick).map { updated =>
          refresh(context, updated)
        }

      case e =>
        updateComponents(context, model)(e)

    private def updateComponents(
        context: UIContext[ReferenceData],
        model: MaskedPane[A, ReferenceData]
    ): GlobalEvent => Outcome[MaskedPane[A, ReferenceData]] =
      case e =>
        val ctx = context.withParentBounds(Bounds(context.parent.bounds.coords, model.dimensions))

        model.content.component
          .updateModel(ctx, model.content.model)(e)
          .flatMap { updatedContent =>
            Outcome(
              model.copy(
                content = model.content.copy(model = updatedContent)
              )
            )
          }

    def present(
        context: UIContext[ReferenceData],
        model: MaskedPane[A, ReferenceData]
    ): Outcome[Layer] =
      val adjustBounds = Bounds(context.parent.bounds.coords, model.dimensions)
      val ctx          = context.withParentBounds(adjustBounds)

      val content =
        ContainerLikeFunctions
          .present(
            ctx,
            model.dimensions,
            Batch(model.content)
          )

      val layers: Outcome[Layer.Stack] =
        content.map(c => Layer.Stack(c))

      layers
        .map { stack =>
          val masked =
            stack.toBatch.map {
              _.withBlendMaterial(
                LayerMask(
                  Bounds(
                    ctx.parent.coords,
                    model.dimensions
                  ).toScreenSpace(ctx.snapGrid * ctx.magnification)
                )
              )
            }

          stack.copy(layers = masked)
        }

    def refresh(
        context: UIContext[ReferenceData],
        model: MaskedPane[A, ReferenceData]
    ): MaskedPane[A, ReferenceData] =
      // Note: This is note _quite_ the same process as found in ComponentGroup

      // First, calculate the bounds without content
      val boundsWithoutContent =
        model.boundsMode match

          // Available

          case BoundsMode(FitMode.Available, FitMode.Available) =>
            context.parent.dimensions

          case BoundsMode(FitMode.Available, FitMode.Content) =>
            context.parent.dimensions.withHeight(0)

          case BoundsMode(FitMode.Available, FitMode.Fixed(height)) =>
            context.parent.dimensions.withHeight(height)

          case BoundsMode(FitMode.Available, FitMode.Relative(amountH)) =>
            context.parent.dimensions.withHeight((context.parent.dimensions.height * amountH).toInt)

          case BoundsMode(FitMode.Available, FitMode.Offset(amount)) =>
            context.parent.dimensions.withHeight(context.parent.dimensions.height + amount)

          // Content

          case BoundsMode(FitMode.Content, FitMode.Available) =>
            Dimensions(0, context.parent.dimensions.height)

          case BoundsMode(FitMode.Content, FitMode.Content) =>
            Dimensions.zero

          case BoundsMode(FitMode.Content, FitMode.Fixed(height)) =>
            Dimensions(0, height)

          case BoundsMode(FitMode.Content, FitMode.Relative(amountH)) =>
            Dimensions(0, (context.parent.dimensions.height * amountH).toInt)

          case BoundsMode(FitMode.Content, FitMode.Offset(amount)) =>
            Dimensions(0, context.parent.dimensions.height + amount)

          // Fixed

          case BoundsMode(FitMode.Fixed(width), FitMode.Available) =>
            Dimensions(width, context.parent.dimensions.height)

          case BoundsMode(FitMode.Fixed(width), FitMode.Content) =>
            Dimensions(width, 0)

          case BoundsMode(FitMode.Fixed(width), FitMode.Fixed(height)) =>
            Dimensions(width, height)

          case BoundsMode(FitMode.Fixed(width), FitMode.Relative(amountH)) =>
            Dimensions(width, (context.parent.dimensions.height * amountH).toInt)

          case BoundsMode(FitMode.Fixed(width), FitMode.Offset(amount)) =>
            Dimensions(width, context.parent.dimensions.height + amount)

          // Relative

          case BoundsMode(FitMode.Relative(amountW), FitMode.Available) =>
            Dimensions((context.parent.dimensions.width * amountW).toInt, context.parent.dimensions.height)

          case BoundsMode(FitMode.Relative(amountW), FitMode.Content) =>
            Dimensions((context.parent.dimensions.width * amountW).toInt, 0)

          case BoundsMode(FitMode.Relative(amountW), FitMode.Fixed(height)) =>
            Dimensions((context.parent.dimensions.width * amountW).toInt, height)

          case BoundsMode(FitMode.Relative(amountW), FitMode.Relative(amountH)) =>
            Dimensions(
              (context.parent.dimensions.width * amountW).toInt,
              (context.parent.dimensions.height * amountH).toInt
            )

          case BoundsMode(FitMode.Relative(amountW), FitMode.Offset(amount)) =>
            Dimensions((context.parent.dimensions.width * amountW).toInt, context.parent.dimensions.height + amount)

          // Offset

          case BoundsMode(FitMode.Offset(amount), FitMode.Available) =>
            context.parent.dimensions.withWidth(context.parent.dimensions.width + amount)

          case BoundsMode(FitMode.Offset(amount), FitMode.Content) =>
            Dimensions(context.parent.dimensions.width + amount, 0)

          case BoundsMode(FitMode.Offset(amount), FitMode.Fixed(height)) =>
            Dimensions(context.parent.dimensions.width + amount, height)

          case BoundsMode(FitMode.Offset(amount), FitMode.Relative(amountH)) =>
            Dimensions(context.parent.dimensions.width + amount, (context.parent.dimensions.height * amountH).toInt)

          case BoundsMode(FitMode.Offset(w), FitMode.Offset(h)) =>
            context.parent.dimensions + Dimensions(w, h)

      // Next, call refresh on the component, and supplying the best guess for the bounds
      val updatedComponent =
        model.content.copy(
          model = model.content.component
            .refresh(context, model.content.model)
        )

      // Now we can calculate the content bounds
      val contentBounds: Bounds =
        model.content.component.bounds(context, updatedComponent.model)

      // We can now calculate the boundsWithoutContent updating in the FitMode.Content cases and leaving as-is in others
      val updatedBounds =
        model.boundsMode match
          case BoundsMode(FitMode.Content, FitMode.Content) =>
            contentBounds.dimensions

          case BoundsMode(FitMode.Content, _) =>
            boundsWithoutContent.withWidth(contentBounds.width)

          case BoundsMode(_, FitMode.Content) =>
            boundsWithoutContent.withHeight(contentBounds.height)

          case _ =>
            boundsWithoutContent

      // Return the updated model with the new bounds and content bounds and dirty flag reset
      model.copy(
        contentBounds = contentBounds,
        dimensions = updatedBounds,
        content = updatedComponent
      )
