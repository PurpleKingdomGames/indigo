package indigo.gameengine
import indigo.gameengine.events.FrameEvent

case class UpdatedModel[Model](model: Model, events: List[FrameEvent])
object UpdatedModel {
  implicit def liftModel[Model](m: Model): UpdatedModel[Model] =
    UpdatedModel(m)

  def apply[Model](model: Model): UpdatedModel[Model] =
    UpdatedModel(model, Nil)
}

case class UpdatedViewModel[Model](model: Model, events: List[FrameEvent])
object UpdatedViewModel {
  implicit def liftViewModel[Model](m: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(m)

  def apply[Model](model: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(model, Nil)
}
