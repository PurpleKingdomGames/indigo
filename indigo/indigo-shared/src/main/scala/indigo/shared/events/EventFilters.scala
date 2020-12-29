package indigo.shared.events

/**
  * EventFilter's control which events will be processed by your model
  * or view model. You can think of event filters like a firewall for
  * events, that only permit the wanted events into the model and
  * view model update functions to avoid conflicts, duplicate, and
  * needless work.
  *
  * Events are filtered by mapping from a specific event to an optional
  * event.
  *
  * Although the name says "filter", the action is really filter and map,
  * since there is no requirement to maintain the original event as the
  * resultant event. For example, you could map `FrameTick` to
  * `CustomEvents.Update` if it make more sense in your domain model.
  *
  * @param modelFilter The filter map for the events going into model update
  * @param viewModelFilter The filter map for the events going into view model update
  */
final case class EventFilters(
    modelFilter: GlobalEvent => Option[GlobalEvent],
    viewModelFilter: GlobalEvent => Option[GlobalEvent]
) {

  /**
    * Modify the existing model event filter.
    */
  def withModelFilter(filter: GlobalEvent => Option[GlobalEvent]): EventFilters =
    this.copy(modelFilter = filter)

  /**
    * Modify the existing view model event filter.
    */
  def withViewModelFilter(filter: GlobalEvent => Option[GlobalEvent]): EventFilters =
    this.copy(viewModelFilter = filter)
}
object EventFilters {

  private def fromAccessControl(ac: AccessControl): GlobalEvent => Option[GlobalEvent] = {
    case e: AssetEvent if ac.allowAssetEvents            => Some(e)
    case _: AssetEvent                                   => None
    case FrameTick if ac.allowFrameTick                  => Some(FrameTick)
    case FrameTick                                       => None
    case e: KeyboardEvent if ac.allowKeyboardEvents      => Some(e)
    case _: KeyboardEvent                                => None
    case e: MouseEvent if ac.allowMouseEvents            => Some(e)
    case _: MouseEvent                                   => None
    case e: NetworkReceiveEvent if ac.allowNetworkEvents => Some(e)
    case _: NetworkReceiveEvent                          => None
    case e: StorageEvent if ac.allowStorageEvents        => Some(e)
    case _: StorageEvent                                 => None
    case e: SubSystemEvent if ac.allowSubSystemEvents    => Some(e)
    case _: SubSystemEvent                               => None
    case e: ViewEvent if ac.allowViewEvents              => Some(e)
    case _: ViewEvent                                    => None
    case e if ac.allowCustomEvents                       => Some(e)
    case _ if ac.allowCustomEvents                       => None
  }

  /**
    * Access controlled event filters are a convienient way to have explicit
    * control which events arrive at which function.
    *
    * @param model An AccessControl instance defining what type of events can
    *              reach the model update function.
    * @param viewModel An AccessControl instance defining what type of events can
    *                  reach the view model update function.
    */
  def withAccessControl(model: AccessControl, viewModel: AccessControl): EventFilters =
    EventFilters(fromAccessControl(model), fromAccessControl(viewModel))

  /**
    * Allow all events to model and view model. This is likely
    * not the desired effect since your game will hear about
    * events intended for things like subsystems, and do
    * unnecessary processing.
    */
  val AllowAll: EventFilters =
    EventFilters(
      (e: GlobalEvent) => Some(e),
      (e: GlobalEvent) => Some(e)
    )

  /**
    * Block all events to model and view model. This is likely
    * not the effect you want since your game will not hear
    * about any events at all. However, one use case is a
    * game with scenes where no global processing is required.
    */
  val BlockAll: EventFilters =
    EventFilters(
      { case _: GlobalEvent => None },
      { case _: GlobalEvent => None }
    )

  /**
    * Model and view model receive all events _apart_ from
    * messages intended for subsystems. Inefficient, but
    * easy to develop against since you can listen for anything
    * anywhere.
    */
  val Permissive: EventFilters =
    EventFilters(
      {
        case _: SubSystemEvent =>
          None

        case e =>
          Some(e)
      },
      {
        case _: SubSystemEvent =>
          None

        case e =>
          Some(e)
      }
    )

  /**
    * The model receives all events that are not subsystem and view specific
    * events. The view model only receives view events and the frametick.
    *
    * These settings are a good default - and used to be the default - but can
    * be confusing during development, particularly since custom events are not
    * handed off to the view model.
    */
  val Restricted: EventFilters =
    EventFilters(
      {
        case _: SubSystemEvent =>
          None

        case _: ViewEvent =>
          None

        case e =>
          Some(e)
      },
      {
        case e: ViewEvent =>
          Some(e)

        case FrameTick =>
          Some(FrameTick)

        case _ =>
          None
      }
    )

  /**
    * Block all events to model and view model. Useful for games
    * that only require a frame tick to update and, for example,
    * process input via input mapping rather than events.
    */
  val FrameTickOnly: EventFilters =
    EventFilters(
      {
        case FrameTick =>
          Some(FrameTick)

        case _ =>
          None
      },
      {
        case FrameTick =>
          Some(FrameTick)

        case _ =>
          None
      }
    )

}

/**
  * A simple type containing flags allowing exact control over the access
  * rights of different types of events.
  */
final case class AccessControl(
    allowAssetEvents: Boolean,
    allowCustomEvents: Boolean,
    allowFrameTick: Boolean,
    allowKeyboardEvents: Boolean,
    allowMouseEvents: Boolean,
    allowNetworkEvents: Boolean,
    allowStorageEvents: Boolean,
    allowSubSystemEvents: Boolean,
    allowViewEvents: Boolean
)
