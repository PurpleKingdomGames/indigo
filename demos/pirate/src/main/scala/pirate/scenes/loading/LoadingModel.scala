package pirate.scenes.loading

final case class LoadingModel(loadingState: LoadingState)
object LoadingModel {
  val initial: LoadingModel =
    LoadingModel(LoadingState.NotStarted)
}

// An ADT representing the states we can be in during loading.
sealed trait LoadingState
object LoadingState {
  case object NotStarted                    extends LoadingState
  final case class InProgress(percent: Int) extends LoadingState
  case object Complete                      extends LoadingState
  case object Error                         extends LoadingState
}
