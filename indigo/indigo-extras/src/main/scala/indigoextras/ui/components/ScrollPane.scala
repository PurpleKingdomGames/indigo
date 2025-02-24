package indigoextras.ui.components

import indigo.*
import indigoextras.ui.component.*
import indigoextras.ui.components.datatypes.Anchor
import indigoextras.ui.components.datatypes.BoundsMode
import indigoextras.ui.components.datatypes.ComponentEntry
import indigoextras.ui.components.datatypes.ComponentId
import indigoextras.ui.components.datatypes.ContainerLikeFunctions
import indigoextras.ui.components.datatypes.FitMode
import indigoextras.ui.components.datatypes.ScrollMode
import indigoextras.ui.components.datatypes.ScrollOptions
import indigoextras.ui.datatypes.*
import indigoextras.ui.shaders.LayerMask

/** Describes a fixed arrangement of components, manages their layout, which may include anchored components, and masks
  * the content outside of the pane, providing a vertical scroll bar to make it visible.
  */
final case class ScrollPane[A, ReferenceData] private[components] (
    bindingKey: BindingKey,
    boundsMode: BoundsMode,
    dimensions: Dimensions, // The actual cached dimensions of the scroll pane
    contentBounds: Bounds,  // The calculated and cached bounds of the content
    scrollAmount: Double,
    // Components
    content: ComponentEntry[A, ReferenceData],
    background: Bounds => Layer,
    scrollBar: Button[Unit],
    scrollBarBackground: Bounds => Layer,
    scrollOptions: ScrollOptions
):

  def withContent[B](component: B)(using
      c: Component[B, ReferenceData]
  ): ScrollPane[B, ReferenceData] =
    this.copy(
      content = ScrollPane.makeComponentEntry(component)
    )

  def withDimensions(value: Dimensions): ScrollPane[A, ReferenceData] =
    this.copy(dimensions = value)

  def withBoundsMode(value: BoundsMode): ScrollPane[A, ReferenceData] =
    this.copy(boundsMode = value)

  def withBackground(value: Bounds => Layer): ScrollPane[A, ReferenceData] =
    this.copy(background = value)

  def withScrollBar(value: Button[Unit]): ScrollPane[A, ReferenceData] =
    this.copy(scrollBar = value)

  def withScrollBackground(value: Bounds => Layer): ScrollPane[A, ReferenceData] =
    this.copy(scrollBarBackground = value)

  def withScrollOptions(value: ScrollOptions): ScrollPane[A, ReferenceData] =
    this.copy(scrollOptions = value)

  def enableScrolling: ScrollPane[A, ReferenceData] =
    this.copy(scrollOptions = scrollOptions.withScrollMode(ScrollMode.Vertical))

  def disableScrolling: ScrollPane[A, ReferenceData] =
    this.copy(scrollOptions = scrollOptions.withScrollMode(ScrollMode.None))

  def withMaxScrollSpeed(value: Int): ScrollPane[A, ReferenceData] =
    this.copy(scrollOptions = scrollOptions.withMaxScrollSpeed(value))

  def withMinScrollSpeed(value: Int): ScrollPane[A, ReferenceData] =
    this.copy(scrollOptions = scrollOptions.withMinScrollSpeed(value))

  def withScrollMode(value: ScrollMode): ScrollPane[A, ReferenceData] =
    this.copy(scrollOptions = scrollOptions.withScrollMode(value))

object ScrollPane:

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

  private val scrollDragEvent: BindingKey => (Unit, DragData) => Batch[GlobalEvent] =
    key => (unit, dragData) => Batch(ScrollPaneEvent.Scroll(key, dragData.position.y))

  private val setupScrollButton: (BindingKey, Button[Unit]) => Button[Unit] =
    (key, button) =>
      button.reportDrag
        .fixedDragArea(Bounds.zero)
        .constrainDragVertically
        .onDrag(scrollDragEvent(key))

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      content: A,
      scrollBar: Button[Unit]
  )(using c: Component[A, ReferenceData]): ScrollPane[A, ReferenceData] =
    ScrollPane(
      bindingKey,
      indigoextras.ui.components.datatypes.BoundsMode.default,
      Dimensions.zero,
      Bounds.zero,
      0.0,
      ScrollPane.makeComponentEntry(content),
      _ => Layer.empty,
      setupScrollButton(bindingKey, scrollBar),
      _ => Layer.empty,
      indigoextras.ui.components.datatypes.ScrollOptions.default
    )

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      boundsMode: BoundsMode,
      content: A,
      scrollBar: Button[Unit]
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(
      bindingKey,
      boundsMode,
      Dimensions.zero,
      Bounds.zero,
      0.0,
      ScrollPane.makeComponentEntry(content),
      _ => Layer.empty,
      setupScrollButton(bindingKey, scrollBar),
      _ => Layer.empty,
      indigoextras.ui.components.datatypes.ScrollOptions.default
    )

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      dimensions: Dimensions,
      content: A,
      scrollBar: Button[Unit]
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(
      bindingKey,
      indigoextras.ui.components.datatypes.BoundsMode.fixed(dimensions),
      dimensions,
      Bounds.zero,
      0.0,
      ScrollPane.makeComponentEntry(content),
      _ => Layer.empty,
      setupScrollButton(bindingKey, scrollBar),
      _ => Layer.empty,
      indigoextras.ui.components.datatypes.ScrollOptions.default
    )

  def apply[A, ReferenceData](
      bindingKey: BindingKey,
      width: Int,
      height: Int,
      content: A,
      scrollBar: Button[Unit]
  )(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(
      bindingKey,
      Dimensions(width, height),
      content,
      setupScrollButton(bindingKey, scrollBar)
    )

  given [A, ReferenceData]: Component[ScrollPane[A, ReferenceData], ReferenceData] with

    def bounds(context: UIContext[ReferenceData], model: ScrollPane[A, ReferenceData]): Bounds =
      Bounds(model.dimensions)

    def updateModel(
        context: UIContext[ReferenceData],
        model: ScrollPane[A, ReferenceData]
    ): GlobalEvent => Outcome[ScrollPane[A, ReferenceData]] =
      case FrameTick =>
        // Sub-groups will naturally refresh themselves as needed
        updateComponents(context, model)(FrameTick).map { updated =>
          refresh(context, updated)
        }

      case ScrollPaneEvent.Scroll(bindingKey, yPos) if bindingKey == model.bindingKey =>
        val bounds    = Bounds(context.parent.coords, model.dimensions)
        val newAmount = (yPos - bounds.y).toDouble / (bounds.height - model.scrollBar.bounds.height).toDouble
        Outcome(model.copy(scrollAmount = newAmount))

      case e =>
        updateComponents(context, model)(e)

    private def updateComponents(
        context: UIContext[ReferenceData],
        model: ScrollPane[A, ReferenceData]
    ): GlobalEvent => Outcome[ScrollPane[A, ReferenceData]] =
      case MouseEvent.Wheel(pos, deltaY)
          if model.scrollOptions.isEnabled && Bounds(context.parent.coords, model.dimensions)
            .contains(context.pointerCoords) =>
        val scrollBy =
          val speed =
            if model.dimensions.height > 0 then model.dimensions.height / 10 else 1
          val clamped =
            Math.min(
              model.scrollOptions.maxScrollSpeed,
              Math.max(model.scrollOptions.minScrollSpeed, speed)
            )
          val coords =
            if deltaY < 0 then -clamped else clamped

          coords.toDouble / model.dimensions.height.toDouble

        Outcome(
          model.copy(
            scrollAmount = Math.min(1.0d, Math.max(0.0d, model.scrollAmount + scrollBy))
          )
        )

      case e =>
        val scrollingActive =
          model.scrollOptions.isEnabled && model.contentBounds.height > model.dimensions.height

        val ctx =
          context.withParentBounds(Bounds(context.parent.coords, model.dimensions))

        val scrollOffset: Coords =
          if scrollingActive then
            Coords(
              0,
              ((model.dimensions.height.toDouble - model.contentBounds.height.toDouble) * model.scrollAmount).toInt
            )
          else Coords.zero

        def updateScrollBar: Outcome[Button[Unit]] =
          val c: Component[Button[Unit], Unit] = summon[Component[Button[Unit], Unit]]
          val unitContext: UIContext[Unit] =
            ctx
              .copy(reference = ())
              .withParent(
                ctx.parent
                  .moveBy(
                    Coords(
                      model.dimensions.width - model.scrollBar.bounds.width,
                      0
                    )
                  )
                  .withAdditionalOffset(
                    Coords(
                      0,
                      ((model.dimensions.height - model.scrollBar.bounds.height).toDouble * model.scrollAmount).toInt
                    )
                  )
              )

          c.updateModel(unitContext, model.scrollBar)(e)

        for {
          updatedContent <- model.content.component
            .updateModel(ctx.withParentBounds(ctx.parent.bounds.moveBy(scrollOffset)), model.content.model)(e)
          updatedScrollBar <- if scrollingActive then updateScrollBar else Outcome(model.scrollBar)
        } yield model.copy(
          content = model.content.copy(model = updatedContent),
          scrollBar = updatedScrollBar
        )

    def present(
        context: UIContext[ReferenceData],
        model: ScrollPane[A, ReferenceData]
    ): Outcome[Layer] =
      val scrollingActive =
        model.scrollOptions.isEnabled && model.contentBounds.height > model.dimensions.height

      val ctx =
        context.withParentBounds(Bounds(context.parent.coords, model.dimensions))

      val scrollOffset: Coords =
        if scrollingActive then
          Coords(
            0,
            ((model.dimensions.height.toDouble - model.contentBounds.height.toDouble) * model.scrollAmount).toInt
          )
        else Coords.zero

      val content =
        ContainerLikeFunctions
          .present(
            ctx.withParentBounds(ctx.parent.bounds.moveBy(scrollOffset)),
            model.dimensions,
            Batch(model.content)
          )

      val layers: Outcome[Layer.Stack] =
        if scrollingActive then
          val c: Component[Button[Unit], Unit] = summon[Component[Button[Unit], Unit]]
          val unitContext: UIContext[Unit]     = ctx.copy(reference = ())
          val scrollbar =
            c.present(
              unitContext
                .withParentBounds(
                  unitContext.parent.bounds.moveBy(
                    Coords(
                      model.dimensions.width - model.scrollBar.bounds.width,
                      ((model.dimensions.height - model.scrollBar.bounds.height).toDouble * model.scrollAmount).toInt
                    )
                  )
                ),
              model.scrollBar
            )
          val scrollBg =
            model.scrollBarBackground(
              ctx.parent.bounds
                .moveBy(model.dimensions.width - model.scrollBar.bounds.width, 0)
                .resize(model.scrollBar.bounds.width, ctx.parent.bounds.height)
            )
          val bg =
            model.background(ctx.parent.bounds)

          (content, scrollbar)
            .map2 { (c, sb) =>
              Layer.Stack(
                bg,
                c,
                scrollBg,
                sb
              )
            }
        else content.map(c => Layer.Stack(c))

      layers
        .map { stack =>
          val masked =
            stack.toBatch.map {
              _.withBlendMaterial(
                LayerMask(
                  ctx.parent.bounds.toScreenSpace(ctx.snapGrid * ctx.magnification)
                )
              )
            }

          stack.copy(layers = masked)
        }

    def refresh(
        context: UIContext[ReferenceData],
        model: ScrollPane[A, ReferenceData]
    ): ScrollPane[A, ReferenceData] =
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

      val dragArea =
        Bounds(
          updatedBounds.width - model.scrollBar.bounds.width,
          0,
          0,
          updatedBounds.height - model.scrollBar.bounds.height
        )

      // Return the updated model with the new bounds and content bounds
      model.copy(
        contentBounds = contentBounds,
        dimensions = updatedBounds,
        content = updatedComponent,
        scrollBar = model.scrollBar.fixedDragArea(dragArea)
      )

enum ScrollPaneEvent extends GlobalEvent:
  case Scroll(bindingKey: BindingKey, amount: Int)
