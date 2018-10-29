package indigo.gameengine
import indigo.gameengine.events.ViewEvent

case class UpdatedModel[Model](model: Model, events: List[ViewEvent])
object UpdatedModel {
  implicit def liftModel[Model](m: Model): UpdatedModel[Model] =
    UpdatedModel(m)

  def apply[Model](model: Model): UpdatedModel[Model] =
    UpdatedModel(model, Nil)
}

case class UpdatedViewModel[Model](model: Model, events: List[ViewEvent])
object UpdatedViewModel {
  implicit def liftViewModel[Model](m: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(m)

  def apply[Model](model: Model): UpdatedViewModel[Model] =
    UpdatedViewModel(model, Nil)
}
