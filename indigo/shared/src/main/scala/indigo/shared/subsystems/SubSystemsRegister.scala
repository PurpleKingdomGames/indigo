package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.Outcome._
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import scala.collection.mutable.ListBuffer
import indigo.shared.FrameContext

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class SubSystemsRegister(subSystems: List[SubSystem]) {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val registeredSubSystems: ListBuffer[SubSystem] = ListBuffer.from(subSystems)

  @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
  def update(frameContext: FrameContext): GlobalEvent => Outcome[SubSystemsRegister] =
    (e: GlobalEvent) => {
      registeredSubSystems.toList
        .map { ss =>
          ss.eventFilter(e)
            .map(ee => ss.update(frameContext)(ee))
            .getOrElse(Outcome(ss, Nil))
        }
        .sequence
        .mapState { l =>
          registeredSubSystems.clear()
          registeredSubSystems ++= l
          this
        }
    }

  def render(frameContext: FrameContext): SceneUpdateFragment =
    registeredSubSystems.map(_.render(frameContext)).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def size: Int =
    registeredSubSystems.size

}
