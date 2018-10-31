package indigo.gameengine
import indigo.gameengine.events.{GlobalEvent, InFrameEvent}

case class UpdatedModel[Model](model: Model, globalEvents: List[GlobalEvent], inFrameEvents: List[InFrameEvent]) {
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
  implicit def liftModel[Model](m: Model): UpdatedModel[Model] =
    UpdatedModel(m)

  def apply[Model](model: Model): UpdatedModel[Model] =
    UpdatedModel(model, Nil, Nil)
}

case class UpdatedViewModel[Model](model: Model, globalEvents: List[GlobalEvent], inFrameEvents: List[InFrameEvent]) {
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
  implicit def liftViewModel[Model](m: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(m)

  def apply[Model](model: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(model, Nil, Nil)
}
