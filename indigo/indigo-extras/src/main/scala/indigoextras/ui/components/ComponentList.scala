package indigoextras.ui.components

import indigo.*
import indigoextras.ui.component.*
import indigoextras.ui.components.datatypes.Anchor
import indigoextras.ui.components.datatypes.ComponentEntry
import indigoextras.ui.components.datatypes.ComponentId
import indigoextras.ui.components.datatypes.ComponentLayout
import indigoextras.ui.components.datatypes.ContainerLikeFunctions
import indigoextras.ui.components.datatypes.Padding
import indigoextras.ui.datatypes.*

/** Describes a dynamic list of components, and their realtive layout.
  */
final case class ComponentList[ReferenceData] private[components] (
    content: ReferenceData => Batch[ComponentEntry[?, ReferenceData]],
    stateMap: Map[ComponentId, Any],
    layout: ComponentLayout,
    dimensions: Dimensions,
    background: Bounds => Layer
):

  private def addSingle[A](entry: ReferenceData => (ComponentId, A))(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f =
      (r: ReferenceData) =>
        content(r) :+ {
          val (id, a) = entry(r)
          ComponentEntry(id, Coords.zero, a, c, None)
        }

    this.copy(
      content = f
    )

  def addOne[A](entry: ReferenceData => (ComponentId, A))(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    addSingle(entry)

  def addOne[A](entry: (ComponentId, A))(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    addSingle(_ => entry)

  def add[A](entries: Batch[ReferenceData => (ComponentId, A)])(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    entries.foldLeft(this) { case (acc, next) => acc.addSingle(next) }

  def add[A](entries: (ReferenceData => (ComponentId, A))*)(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    Batch.fromSeq(entries).foldLeft(this) { case (acc, next) => acc.addSingle(next) }

  def add[A](entries: ReferenceData => Batch[(ComponentId, A)])(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    this.copy(
      content =
        (r: ReferenceData) => content(r) ++ entries(r).map(v => ComponentEntry(v._1, Coords.zero, v._2, c, None))
    )

  def withDimensions(value: Dimensions): ComponentList[ReferenceData] =
    this.copy(dimensions = value)

  def withLayout(value: ComponentLayout): ComponentList[ReferenceData] =
    this.copy(layout = value)

  def resizeTo(size: Dimensions): ComponentList[ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): ComponentList[ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): ComponentList[ReferenceData] =
    withDimensions(dimensions + amount)
  def resizeBy(x: Int, y: Int): ComponentList[ReferenceData] =
    resizeBy(Dimensions(x, y))

  def withBackground(present: Bounds => Layer): ComponentList[ReferenceData] =
    this.copy(background = present)

object ComponentList:

  def apply[ReferenceData, A](
      dimensions: Dimensions
  )(contents: ReferenceData => Batch[(ComponentId, A)])(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: ReferenceData => Batch[ComponentEntry[A, ReferenceData]] =
      r => contents(r).map(v => ComponentEntry(v._1, Coords.zero, v._2, c, None))

    ComponentList(
      f,
      Map.empty,
      ComponentLayout.Vertical(Padding.zero),
      dimensions,
      _ => Layer.empty
    )

  def apply[ReferenceData, A](
      dimensions: Dimensions
  )(contents: (ComponentId, A)*)(using
      c: Component[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: ReferenceData => Batch[ComponentEntry[A, ReferenceData]] =
      _ => Batch.fromSeq(contents).map(v => ComponentEntry(v._1, Coords.zero, v._2, c, None))

    ComponentList(
      f,
      Map.empty,
      ComponentLayout.Vertical(Padding.zero),
      dimensions,
      _ => Layer.empty
    )

  given [ReferenceData]: Component[ComponentList[ReferenceData], ReferenceData] with

    def bounds(
        context: ReferenceData,
        model: ComponentList[ReferenceData]
    ): Bounds =
      Bounds(model.dimensions)

    def updateModel(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): GlobalEvent => Outcome[ComponentList[ReferenceData]] =
      case e =>
        // What we're doing here it updating the stateMap, not the content function.
        // However, to do that properly, we need to reflow the content too, to make sure things
        // like pointer clicks are still in the right place.
        val nextOffset =
          ContainerLikeFunctions
            .calculateNextOffset[ReferenceData](model.dimensions, model.layout)

        val entries =
          model.content(context.reference)

        val nextStateMap =
          entries
            .foldLeft(Outcome(Batch.empty[ComponentEntry[?, ReferenceData]])) { (accum, entry) =>
              accum.flatMap { acc =>
                val offset = nextOffset(context.reference, acc)

                val updated =
                  model.stateMap.get(entry.id) match
                    case None =>
                      // No entry, so we make one based on the component's default state
                      entry.component
                        .updateModel(
                          context.copy(bounds = context.bounds.moveBy(offset)),
                          entry.model
                        )(e)
                        .map(m => entry.copy(offset = offset, model = m))

                    case Some(savedState) =>
                      // We have an entry, so we update it
                      entry.component
                        .updateModel(
                          context.copy(bounds = context.bounds.moveBy(offset)),
                          savedState.asInstanceOf[entry.Out]
                        )(e)
                        .map(m => entry.copy(offset = offset, model = m))

                updated.map(u => acc :+ u)
              }
            }
            .map(_.map(e => e.id -> e.model).toMap)

        nextStateMap.map { newStateMap =>
          model.copy(stateMap = newStateMap)
        }

    def present(
        context: UIContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): Outcome[Layer] =
      // Pull the state out of the stateMap and present it
      val entries =
        model
          .content(context.reference)
          .map { entry =>
            model.stateMap.get(entry.id) match
              case None =>
                // No entry, so we use the default.
                entry

              case Some(savedState) =>
                // We have an entry, so overwrite the model with it.
                entry.copy(model = savedState.asInstanceOf[entry.Out])
          }

      ContainerLikeFunctions
        .present(
          context,
          model.dimensions,
          contentReflow(context.reference, model.dimensions, model.layout, entries)
        )
        .map { components =>
          val background = model.background(Bounds(context.bounds.coords, model.dimensions))
          Layer.Stack(background, components)
        }

    // ComponentList's have a fixed size, so we don't need to do anything here,
    // and since this component's size doesn't change, nor do we need to
    // propagate further.
    def refresh(
        reference: ReferenceData,
        model: ComponentList[ReferenceData],
        parentDimensions: Dimensions
    ): ComponentList[ReferenceData] =
      model

    private def contentReflow(
        reference: ReferenceData,
        dimensions: Dimensions,
        layout: ComponentLayout,
        entries: Batch[ComponentEntry[?, ReferenceData]]
    ): Batch[ComponentEntry[?, ReferenceData]] =
      val nextOffset =
        ContainerLikeFunctions
          .calculateNextOffset[ReferenceData](dimensions, layout)

      entries.foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) { (acc, entry) =>
        val reflowed = entry.copy(
          offset = nextOffset(reference, acc)
        )

        acc :+ reflowed
      }
