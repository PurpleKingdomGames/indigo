package indigo.gameengine
import indigo.gameengine.events.{GlobalEvent, InFrameEvent}

case class UpdatedModel[Model](model: Model, globalEvents: List[GlobalEvent], inFrameEvents: List[InFrameEvent])
object UpdatedModel {
  implicit def liftModel[Model](m: Model): UpdatedModel[Model] =
    UpdatedModel(m)

  def apply[Model](model: Model): UpdatedModel[Model] =
    UpdatedModel(model, Nil, Nil)
}

case class UpdatedViewModel[Model](model: Model, globalEvents: List[GlobalEvent], inFrameEvents: List[InFrameEvent])
object UpdatedViewModel {
  implicit def liftViewModel[Model](m: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(m)

  def apply[Model](model: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(model, Nil, Nil)
}
