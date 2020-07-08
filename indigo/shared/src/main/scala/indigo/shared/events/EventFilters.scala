package indigo.shared.events

/**
  * EventFilter's control which events will be processed by your model
  * or view model. They are a bit like subscribing or listening to events
  * in an GUI OO system.
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
  def withModelFilter(filter: GlobalEvent => Option[GlobalEvent]): EventFilters =
    this.copy(modelFilter = filter)

  def withViewModelFilter(filter: GlobalEvent => Option[GlobalEvent]): EventFilters =
    this.copy(viewModelFilter = filter)
}
object EventFilters {

  /**
    * The default mapping for model events
    */
  val defaultModelFilter: GlobalEvent => Option[GlobalEvent] = {
    case _: SubSystemEvent =>
      None

    case _: ViewEvent =>
      None

    case e =>
      Some(e)
  }

  /**
    * The default mapping for view model events
    */
  val defaultViewModelFilter: GlobalEvent => Option[GlobalEvent] = {
    case e: ViewEvent =>
      Some(e)

    case FrameTick =>
      Some(FrameTick)

    case _ =>
      None
  }

  /**
    * Allow all events to the model, except for SubSystemEvents and ViewEvents.
    * Block everything to the view model except view events.
    */
  val Default: EventFilters =
    EventFilters(defaultModelFilter, defaultViewModelFilter)

  /**
    * Allow all events to model and view model
    */
  val NoFilter: EventFilters =
    EventFilters(
      (e: GlobalEvent) => Some(e),
      (e: GlobalEvent) => Some(e)
    )

  /**
    * Block all events to model and view model
    */
  val Block: EventFilters =
    EventFilters(
      (_: GlobalEvent) => None,
      (_: GlobalEvent) => None
    )

  /**
    * Custom model filter, default view model filter
    */
  def ModelOnly(filter: GlobalEvent => Option[GlobalEvent]): EventFilters =
    EventFilters(
      filter,
      defaultViewModelFilter
    )

  /**
    * Default model filter, custom view model filter
    */
  def ViewModelOnly(filter: GlobalEvent => Option[GlobalEvent]): EventFilters =
    EventFilters(
      defaultModelFilter,
      filter
    )

}
