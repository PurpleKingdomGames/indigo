package pirate.scenes.loading

final case class LoadingModel(loadingState: LoadingState)
object LoadingModel:
  val initial: LoadingModel =
    LoadingModel(LoadingState.NotStarted)

// An ADT representing the states we can be in during loading.
enum LoadingState:
  case NotStarted               extends LoadingState
  case InProgress(percent: Int) extends LoadingState
  case Complete                 extends LoadingState
  case Error                    extends LoadingState
