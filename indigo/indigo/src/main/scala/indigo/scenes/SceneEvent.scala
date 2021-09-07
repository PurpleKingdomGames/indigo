package indigo.scenes

import indigo.shared.events.GlobalEvent
import indigo.shared.time.Seconds

/** The events used by the scene manager to move between scenes.
  */
enum SceneEvent extends GlobalEvent derives CanEqual:
  case Next extends SceneEvent
  case Previous extends SceneEvent
  case JumpTo(name: SceneName) extends SceneEvent
  case SceneChange(from: SceneName, to: SceneName, at: Seconds) extends SceneEvent
