package indigo.gameengine
import indigo.gameengine.events.{GlobalEvent, InFrameEvent}

final case class UpdatedModel[Model](model: Model, globalEvents: List[GlobalEvent], inFrameEvents: List[InFrameEvent]) {
  def addGlobalEvents(events: GlobalEvent*): UpdatedModel[Model] =
    addGlobalEvents(events.toList)
  def addGlobalEvents(events: List[GlobalEvent]): UpdatedModel[Model] =
    this.copy(globalEvents = globalEvents ++ events)

  def addInFrameEvents(events: InFrameEvent*): UpdatedModel[Model] =
    addInFrameEvents(events.toList)
  def addInFrameEvents(events: List[InFrameEvent]): UpdatedModel[Model] =
    this.copy(inFrameEvents = inFrameEvents ++ events)
}
object UpdatedModel {
  def apply[Model](model: Model): UpdatedModel[Model] =
    UpdatedModel(model, Nil, Nil)
}

final case class UpdatedViewModel[Model](model: Model, globalEvents: List[GlobalEvent], inFrameEvents: List[InFrameEvent]) {
  def addGlobalEvents(events: GlobalEvent*): UpdatedViewModel[Model] =
    addGlobalEvents(events.toList)
  def addGlobalEvents(events: List[GlobalEvent]): UpdatedViewModel[Model] =
    this.copy(globalEvents = globalEvents ++ events)

  def addInFrameEvents(events: InFrameEvent*): UpdatedViewModel[Model] =
    addInFrameEvents(events.toList)
  def addInFrameEvents(events: List[InFrameEvent]): UpdatedViewModel[Model] =
    this.copy(inFrameEvents = inFrameEvents ++ events)
}
object UpdatedViewModel {
  def apply[Model](model: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(model, Nil, Nil)
}
