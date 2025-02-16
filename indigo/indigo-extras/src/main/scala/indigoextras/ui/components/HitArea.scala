package indigoextras.ui.components

import indigo.*
import indigo.syntax.*
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

import datatypes.BoundsType

/** The HitArea `Component` allows you to create invisible buttons for your UI.
  *
  * Functionally, a hit area is identical to a button that does not render anything. In fact a HitArea is isomorphic to
  * a Button that renders nothing, and its component instance is mostly implemented by delegating to the button
  * instance.
  *
  * All that said... for debug purposes, you can set a fill or stroke color to see the hit area.
  */
final case class HitArea[ReferenceData](
    bounds: Bounds,
    state: ButtonState,
    click: ReferenceData => Batch[GlobalEvent],
    press: ReferenceData => Batch[GlobalEvent],
    release: ReferenceData => Batch[GlobalEvent],
    drag: (ReferenceData, DragData) => Batch[GlobalEvent],
    boundsType: BoundsType[ReferenceData, Unit],
    isDown: Boolean,
    dragOptions: DragOptions,
    dragStart: Option[DragData],
    fill: Option[RGBA] = None,
    stroke: Option[Stroke] = None
):
  val isDragged: Boolean = dragStart.isDefined

  def onClick(events: ReferenceData => Batch[GlobalEvent]): HitArea[ReferenceData] =
    this.copy(click = events)
  def onClick(events: Batch[GlobalEvent]): HitArea[ReferenceData] =
    onClick(_ => events)
  def onClick(events: GlobalEvent*): HitArea[ReferenceData] =
    onClick(Batch.fromSeq(events))

  def onPress(events: ReferenceData => Batch[GlobalEvent]): HitArea[ReferenceData] =
    this.copy(press = events)
  def onPress(events: Batch[GlobalEvent]): HitArea[ReferenceData] =
    onPress(_ => events)
  def onPress(events: GlobalEvent*): HitArea[ReferenceData] =
    onPress(Batch.fromSeq(events))

  def onRelease(events: ReferenceData => Batch[GlobalEvent]): HitArea[ReferenceData] =
    this.copy(release = events)
  def onRelease(events: Batch[GlobalEvent]): HitArea[ReferenceData] =
    onRelease(_ => events)
  def onRelease(events: GlobalEvent*): HitArea[ReferenceData] =
    onRelease(Batch.fromSeq(events))

  def onDrag(
      events: (ReferenceData, DragData) => Batch[GlobalEvent]
  ): HitArea[ReferenceData] =
    this.copy(drag = events)
  def onDrag(events: Batch[GlobalEvent]): HitArea[ReferenceData] =
    onDrag((_, _) => events)
  def onDrag(events: GlobalEvent*): HitArea[ReferenceData] =
    onDrag(Batch.fromSeq(events))

  def withDragOptions(value: DragOptions): HitArea[ReferenceData] =
    this.copy(dragOptions = value)
  def makeDraggable: HitArea[ReferenceData] =
    withDragOptions(dragOptions.withMode(DragMode.Drag))
  def reportDrag: HitArea[ReferenceData] =
    withDragOptions(dragOptions.withMode(DragMode.ReportDrag))
  def notDraggable: HitArea[ReferenceData] =
    withDragOptions(dragOptions.withMode(DragMode.None))

  def withDragConstrain(value: DragConstrain): HitArea[ReferenceData] =
    this.copy(dragOptions = dragOptions.withConstraints(value))
  def constrainDragTo(bounds: Bounds): HitArea[ReferenceData] =
    withDragConstrain(DragConstrain.To(bounds))
  def constrainDragVertically: HitArea[ReferenceData] =
    withDragConstrain(DragConstrain.Vertical)
  def constrainDragVertically(from: Int, to: Int, x: Int): HitArea[ReferenceData] =
    withDragConstrain(DragConstrain.vertical(from, to, x))
  def constrainDragHorizontally: HitArea[ReferenceData] =
    withDragConstrain(DragConstrain.Horizontal)
  def constrainDragHorizontally(from: Int, to: Int, y: Int): HitArea[ReferenceData] =
    withDragConstrain(DragConstrain.horizontal(from, to, y))

  def withDragArea(value: DragArea): HitArea[ReferenceData] =
    this.copy(dragOptions = dragOptions.withArea(value))
  def noDragArea: HitArea[ReferenceData] =
    withDragArea(DragArea.None)
  def fixedDragArea(bounds: Bounds): HitArea[ReferenceData] =
    withDragArea(DragArea.Fixed(bounds))
  def inheritDragArea: HitArea[ReferenceData] =
    withDragArea(DragArea.Inherit)

  def withBoundsType(value: BoundsType[ReferenceData, Unit]): HitArea[ReferenceData] =
    this.copy(boundsType = value)

  def withFill(value: RGBA): HitArea[ReferenceData] =
    this.copy(fill = Option(value))
  def clearFill: HitArea[ReferenceData] =
    this.copy(fill = None)

  def withStroke(value: Stroke): HitArea[ReferenceData] =
    this.copy(stroke = Option(value))
  def clearStroke: HitArea[ReferenceData] =
    this.copy(stroke = None)

  def toButton: Button[ReferenceData] =
    Button(
      bounds,
      state,
      (_, _) => Outcome(Layer.empty),
      None,
      None,
      click,
      press,
      release,
      drag,
      boundsType,
      isDown,
      dragOptions,
      dragStart
    )

object HitArea:

  /** Minimal hitarea constructor with no events.
    */
  def apply[ReferenceData](boundsType: BoundsType[ReferenceData, Unit]): HitArea[ReferenceData] =
    HitArea(
      Bounds.zero,
      ButtonState.Up,
      _ => Batch.empty,
      _ => Batch.empty,
      _ => Batch.empty,
      (_, _) => Batch.empty,
      boundsType,
      isDown = false,
      dragOptions = DragOptions.default,
      dragStart = None,
      fill = None,
      stroke = None
    )

  /** Minimal hitarea constructor with no events.
    */
  def apply[ReferenceData](bounds: Bounds): HitArea[ReferenceData] =
    HitArea(
      bounds,
      ButtonState.Up,
      _ => Batch.empty,
      _ => Batch.empty,
      _ => Batch.empty,
      (_, _) => Batch.empty,
      datatypes.BoundsType.Fixed(bounds),
      isDown = false,
      dragOptions = DragOptions.default,
      dragStart = None,
      fill = None,
      stroke = None
    )

  given [ReferenceData](using btn: Component[Button[ReferenceData], ReferenceData]): Component[
    HitArea[ReferenceData],
    ReferenceData
  ] with
    def bounds(context: UIContext[ReferenceData], model: HitArea[ReferenceData]): Bounds =
      btn.bounds(context, model.toButton)

    def updateModel(
        context: UIContext[ReferenceData],
        model: HitArea[ReferenceData]
    ): GlobalEvent => Outcome[HitArea[ReferenceData]] =
      e =>
        val f = model.fill
        val s = model.stroke
        btn.updateModel(context, model.toButton)(e).map(_.toHitArea.copy(fill = f, stroke = s))

    def present(
        context: UIContext[ReferenceData],
        model: HitArea[ReferenceData]
    ): Outcome[Layer] =
      (model.fill, model.stroke) match
        case (Some(fill), Some(stroke)) =>
          Outcome(
            Layer(
              Shape.Box(
                model.bounds.unsafeToRectangle.moveTo(context.parent.coords.unsafeToPoint),
                Fill.Color(fill),
                stroke
              )
            )
          )

        case (Some(fill), None) =>
          Outcome(
            Layer(
              Shape.Box(
                model.bounds.unsafeToRectangle.moveTo(context.parent.coords.unsafeToPoint),
                Fill.Color(fill)
              )
            )
          )

        case (None, Some(stroke)) =>
          Outcome(
            Layer(
              Shape.Box(
                model.bounds.unsafeToRectangle.moveTo(context.parent.coords.unsafeToPoint),
                Fill.None,
                stroke
              )
            )
          )

        case (None, None) =>
          Outcome(Layer.empty)

    def refresh(
        context: UIContext[ReferenceData],
        model: HitArea[ReferenceData]
    ): HitArea[ReferenceData] =
      val f = model.fill
      val s = model.stroke
      btn.refresh(context, model.toButton).toHitArea.copy(fill = f, stroke = s)
