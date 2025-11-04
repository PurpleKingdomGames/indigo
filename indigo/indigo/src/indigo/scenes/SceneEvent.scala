package indigo.scenes

import indigo.shared.events.GlobalEvent
import indigo.shared.time.Seconds

/** The events used by the scene manager to move between scenes.
  */
enum SceneEvent extends GlobalEvent derives CanEqual:
  case Next
  case Previous
  case LoopNext
  case LoopPrevious
  case JumpTo(name: SceneName)
  case SceneChange(from: SceneName, to: SceneName, at: Seconds)
  case First
  case Last
