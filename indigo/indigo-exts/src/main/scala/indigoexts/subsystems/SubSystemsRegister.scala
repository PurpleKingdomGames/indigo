package indigoexts.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.Outcome._
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.dice.Dice
import scala.collection.mutable.ListBuffer

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class SubSystemsRegister(subSystems: List[SubSystem]) {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val registeredSubSystems: ListBuffer[SubSystem] = ListBuffer.from(subSystems)

  @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[SubSystemsRegister] = {
    case e: GlobalEvent =>
      registeredSubSystems.toList
        .map { ss =>
          ss.eventFilter(e)
            .map(ee => ss.update(gameTime, dice)(ee))
            .getOrElse(Outcome(ss, Nil))
        }
        .sequence
        .mapState { l =>
          registeredSubSystems.clear()
          registeredSubSystems ++= l
          this
        }

    case _ =>
      Outcome(this)
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    registeredSubSystems.map(_.render(gameTime)).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def size: Int =
    registeredSubSystems.size

}
