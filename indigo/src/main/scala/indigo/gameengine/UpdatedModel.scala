package indigo.gameengine
import indigo.gameengine.events.{GlobalEvent, InFrameEvent}

final case class UpdatedModel[Model](model: Model, globalEvents: List[GlobalEvent], inFrameEvents: List[InFrameEvent]) {
  def addGlobalEvents(events: GlobalEvent*): UpdatedModel[Model] =
    addGlobalEvents(events.toList)
  def addGlobalEvents(events: List[GlobalEvent]): UpdatedModel[Model] =
    UpdatedModel(model, globalEvents ++ events, inFrameEvents)

  def addInFrameEvents(events: InFrameEvent*): UpdatedModel[Model] =
    addInFrameEvents(events.toList)
  def addInFrameEvents(events: List[InFrameEvent]): UpdatedModel[Model] =
    UpdatedModel(model, globalEvents, inFrameEvents ++ events)
}
object UpdatedModel {
  def apply[Model](model: Model): UpdatedModel[Model] =
    UpdatedModel(model, Nil, Nil)
}

final case class UpdatedViewModel[Model](model: Model, globalEvents: List[GlobalEvent], inFrameEvents: List[InFrameEvent]) {
  def addGlobalEvents(events: GlobalEvent*): UpdatedViewModel[Model] =
    addGlobalEvents(events.toList)
  def addGlobalEvents(events: List[GlobalEvent]): UpdatedViewModel[Model] =
    UpdatedViewModel(model, globalEvents ++ events, inFrameEvents)

  def addInFrameEvents(events: InFrameEvent*): UpdatedViewModel[Model] =
    addInFrameEvents(events.toList)
  def addInFrameEvents(events: List[InFrameEvent]): UpdatedViewModel[Model] =
    UpdatedViewModel(model, globalEvents, inFrameEvents ++ events)
}
object UpdatedViewModel {
  def apply[Model](model: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(model, Nil, Nil)
}
