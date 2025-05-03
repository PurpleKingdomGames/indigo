package indigoextras.ui.window

import indigo.*
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.UIContext
import indigoextras.ui.datatypes.UIState

final case class WindowManager[StartUpData, Model, RefData](
    id: SubSystemId,
    initialMagnification: Int,
    snapGrid: Size,
    extractReference: Model => RefData,
    startUpData: StartUpData,
    layerKey: Option[LayerKey],
    windows: Batch[Window[?, RefData]]
) extends SubSystem[Model]:
  type EventType      = GlobalEvent
  type ReferenceData  = RefData
  type SubSystemModel = ModelHolder[ReferenceData]

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def reference(model: Model): ReferenceData =
    extractReference(model)

  def initialModel: Outcome[ModelHolder[ReferenceData]] =
    Outcome(
      ModelHolder.initial(windows, initialMagnification)
    )

  def update(
      context: SubSystemContext[ReferenceData],
      model: ModelHolder[ReferenceData]
  ): GlobalEvent => Outcome[ModelHolder[ReferenceData]] =
    e =>
      for {
        updatedModel <- WindowManager.updateModel[ReferenceData](
          UIContext(context, snapGrid, model.viewModel.magnification),
          model.model
        )(e)

        updatedViewModel <-
          WindowManager.updateViewModel[ReferenceData](
            UIContext(context, snapGrid, model.viewModel.magnification),
            updatedModel,
            model.viewModel
          )(e)
      } yield ModelHolder(updatedModel, updatedViewModel)

  def present(
      context: SubSystemContext[ReferenceData],
      model: ModelHolder[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
    WindowManager.present(
      layerKey,
      UIContext(context, snapGrid, model.viewModel.magnification),
      model.model,
      model.viewModel
    )

  /** Registers a window with the WindowManager. All Window's must be registered before the scene starts.
    */
  def register(
      newWindows: Window[?, ReferenceData]*
  ): WindowManager[StartUpData, Model, ReferenceData] =
    register(Batch.fromSeq(newWindows))
  def register(
      newWindows: Batch[Window[?, ReferenceData]]
  ): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(windows = windows ++ newWindows)

  /** Sets which windows are initially open. Once the scene is running, opening and closing is managed by the
    * WindowManagerModel via events.
    */
  def open(ids: WindowId*): WindowManager[StartUpData, Model, ReferenceData] =
    open(Batch.fromSeq(ids))
  def open(ids: Batch[WindowId]): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(windows = windows.map(w => if ids.exists(_ == w.id) then w.open else w))

  /** Sets which window is initially focused. Once the scene is running, focusing is managed by the WindowManagerModel
    * via events.
    */
  def focus(id: WindowId): WindowManager[StartUpData, Model, ReferenceData] =
    val reordered =
      windows.find(_.id == id) match
        case None =>
          windows

        case Some(w) =>
          windows.filterNot(_.id == w.id).map(_.blur) :+ w.focus

    this.copy(windows = reordered)

  def withStartupData[A](newStartupData: A): WindowManager[A, Model, ReferenceData] =
    WindowManager(
      id,
      initialMagnification,
      snapGrid,
      extractReference,
      newStartupData,
      layerKey,
      windows
    )

  /** Allows you to set the layer key that the WindowManager will use to present the windows.
    */
  def withLayerKey(newLayerKey: LayerKey): WindowManager[StartUpData, Model, ReferenceData] =
    this.copy(layerKey = Option(newLayerKey))

object WindowManager:

  /** Creates a WindowManager instance with no snap grid, that respects the magnification specified.
    */
  def apply[Model](id: SubSystemId): WindowManager[Unit, Model, Unit] =
    WindowManager(id, 1, Size(1), _ => (), (), None, Batch.empty)

  /** Creates a WindowManager instance with no snap grid, that respects the magnification specified.
    */
  def apply[Model](
      id: SubSystemId,
      magnification: Int
  ): WindowManager[Unit, Model, Unit] =
    WindowManager(id, magnification, Size(1), _ => (), (), None, Batch.empty)

  /** Creates a WindowManager instance with no snap grid, that respects the magnification specified.
    */
  def apply[Model](
      id: SubSystemId,
      magnification: Int,
      snapGrid: Size
  ): WindowManager[Unit, Model, Unit] =
    WindowManager(id, magnification, Size(1), _ => (), (), None, Batch.empty)

  def apply[Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      snapGrid: Size,
      extractReference: Model => ReferenceData
  ): WindowManager[Unit, Model, ReferenceData] =
    WindowManager(id, magnification, snapGrid, extractReference, (), None, Batch.empty)

  def apply[StartUpData, Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      snapGrid: Size,
      extractReference: Model => ReferenceData,
      startUpData: StartUpData
  ): WindowManager[StartUpData, Model, ReferenceData] =
    WindowManager(
      id,
      magnification,
      snapGrid,
      extractReference,
      startUpData,
      None,
      Batch.empty
    )

  def apply[StartUpData, Model, ReferenceData](
      id: SubSystemId,
      magnification: Int,
      snapGrid: Size,
      extractReference: Model => ReferenceData,
      startUpData: StartUpData,
      layerKey: LayerKey
  ): WindowManager[StartUpData, Model, ReferenceData] =
    WindowManager(
      id,
      magnification,
      snapGrid,
      extractReference,
      startUpData,
      Option(layerKey),
      Batch.empty
    )

  private def modalWindowOpen[ReferenceData](
      model: WindowManagerModel[ReferenceData]
  ): Option[WindowId] =
    model.windows.find(w => w.isOpen && w.mode == WindowMode.Modal).map(_.id)

  private[window] def updateModel[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerModel[ReferenceData]] =
    case e: WindowEvent =>
      modalWindowOpen(model) match
        case None =>
          handleWindowEvents(context, model)(e)

        case modelId =>
          if modelId == e.windowId then handleWindowEvents(context, model)(e)
          else Outcome(model)

    case e: PointerEvent.Down =>
      updateWindows(context, model, modalWindowOpen(model))(e)
        .addGlobalEvents(WindowEvent.GiveFocusAt(context.pointerCoords))

    case FrameTick =>
      modalWindowOpen(model) match
        case None =>
          updateWindows(context, model, None)(FrameTick)

        case _id @ Some(id) =>
          updateWindows(context, model, _id)(FrameTick).map(_.focusOn(id))

    case e =>
      updateWindows(context, model, modalWindowOpen(model))(e)

  private def updateWindows[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      modalWindow: Option[WindowId]
  ): GlobalEvent => Outcome[WindowManagerModel[ReferenceData]] =
    e =>
      val windowUnderPointer =
        model.windowAt(context.pointerCoords, context.frame.viewport.toSize, context.magnification)

      model.windows
        .map { w =>
          val windowActive =
            w.activeCheck(context)

          val ctx =
            windowActive match
              case WindowActive.Active =>
                context.withState(
                  modalWindow match
                    case Some(id) if id == w.id =>
                      UIState.Active

                    case Some(_) =>
                      UIState.InActive

                    case None =>
                      if w.hasFocus || windowUnderPointer.exists(_ == w.id) then UIState.Active
                      else UIState.InActive
                )

              case WindowActive.InActive =>
                context.withState(UIState.InActive)

          val windowUpdateFocus =
            if w.hasFocus && windowActive.isInActive then w.blur
            else w

          Window.updateModel(ctx, windowUpdateFocus)(e)
        }
        .sequence
        .map(m => model.copy(windows = m))

  private def handleWindowEvents[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData]
  ): WindowEvent => Outcome[WindowManagerModel[ReferenceData]] =
    case WindowEvent.Refresh(id) =>
      model.refresh(context, id)

    case WindowEvent.Focus(id) =>
      Outcome(model.focusOn(id))

    case WindowEvent.GiveFocusAt(position) =>
      Outcome(model.focusAt(position, context))
        .addGlobalEvents(WindowInternalEvent.Redraw)

    case WindowEvent.Open(id) =>
      model.open(id).addGlobalEvents(WindowEvent.Focus(id))

    case WindowEvent.OpenAt(id, coords) =>
      model
        .open(id)
        .map(_.moveTo(id, coords, Space.Screen, context.frame.viewport.toSize, context.magnification))
        .addGlobalEvents(WindowEvent.Focus(id))

    case WindowEvent.Close(id) =>
      model.close(id)

    case WindowEvent.Toggle(id) =>
      model.toggle(id)

    case WindowEvent.Move(id, coords, space) =>
      Outcome(model.moveTo(id, coords, space, context.frame.viewport.toSize, context.magnification))

    case WindowEvent.Anchor(id, anchor) =>
      Outcome(model.anchor(id, anchor))

    case WindowEvent.Resize(id, dimensions, space) =>
      model.resizeTo(id, dimensions, space, context.frame.viewport.toSize, context.magnification).refresh(context, id)

    case WindowEvent.Transform(id, bounds, space) =>
      model.transformTo(id, bounds, space, context.frame.viewport.toSize, context.magnification).refresh(context, id)

    case WindowEvent.Opened(_) =>
      Outcome(model)

    case WindowEvent.Closed(_) =>
      Outcome(model)

    case WindowEvent.Resized(_) =>
      Outcome(model)

    case WindowEvent.PointerOver(_) =>
      Outcome(model)

    case WindowEvent.PointerOut(_) =>
      Outcome(model)

    case WindowEvent.ChangeMagnification(_) =>
      Outcome(model)

    case WindowEvent.CloseFocused =>
      model.focused match
        case None =>
          Outcome(model)

        case Some(window) =>
          model.close(window.id)

  private[window] def updateViewModel[ReferenceData](
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowManagerViewModel[ReferenceData]] =
    case WindowEvent.ChangeMagnification(next) =>
      Outcome(viewModel.changeMagnification(next))

    case e =>
      val windowUnderPointer =
        model.windowAt(context.pointerCoords, context.frame.viewport.toSize, context.magnification)

      val updated =
        val prunedVM = viewModel.prune(model)
        model.windows.flatMap { m =>
          if m.isClosed then Batch.empty
          else
            prunedVM.windows.find(_.id == m.id) match
              case None =>
                Batch(Outcome(WindowViewModel.initial(m.id, viewModel.magnification)))

              case Some(vm) =>
                Batch(
                  vm.update(
                    context.copy(state =
                      if m.hasFocus || windowUnderPointer.exists(_ == m.id) then UIState.Active
                      else UIState.InActive
                    ),
                    m,
                    e
                  )
                )
        }

      updated.sequence.map(vm => viewModel.copy(windows = vm))

  private[window] def present[ReferenceData](
      layerKey: Option[LayerKey],
      context: UIContext[ReferenceData],
      model: WindowManagerModel[ReferenceData],
      viewModel: WindowManagerViewModel[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
    val windowUnderPointer = model.windowAt(context.pointerCoords, context.frame.viewport.toSize, context.magnification)

    val windowLayers: Outcome[Batch[Layer]] =
      model.windows
        .filter(_.isOpen)
        .flatMap { m =>
          viewModel.windows.find(_.id == m.id) match
            case None =>
              // Shouldn't get here.
              Batch.empty

            case Some(vm) =>
              Batch(
                WindowView
                  .present(
                    context.copy(state =
                      if m.hasFocus || windowUnderPointer.exists(_ == m.id) then UIState.Active
                      else UIState.InActive
                    ),
                    m,
                    vm
                  )
              )
        }
        .sequence

    windowLayers.map { layers =>
      layerKey match
        case None =>
          SceneUpdateFragment(
            LayerEntry(Layer.Stack(layers))
          )

        case Some(key) =>
          SceneUpdateFragment(
            LayerEntry(key -> Layer.Stack(layers))
          )
    }

final case class ModelHolder[ReferenceData](
    model: WindowManagerModel[ReferenceData],
    viewModel: WindowManagerViewModel[ReferenceData]
)
object ModelHolder:
  def initial[ReferenceData](
      windows: Batch[Window[?, ReferenceData]],
      magnification: Int
  ): ModelHolder[ReferenceData] =
    ModelHolder(
      WindowManagerModel.initial.register(windows),
      WindowManagerViewModel.initial(magnification)
    )
