package indigoexts.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.Outcome._
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.dice.Dice
import scala.collection.mutable.ListBuffer

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class SubSystemsRegister {

  val registeredSubSystems: ListBuffer[SubSystem] = new ListBuffer()

  def add(subSystems: SubSystem*): SubSystemsRegister =
    add(subSystems.toList)
  def add(subSystems: List[SubSystem]): SubSystemsRegister = {
    registeredSubSystems ++= subSystems
    this
  }

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

object SubSystemsRegister {

  def apply(): SubSystemsRegister =
    new SubSystemsRegister()

  def empty: SubSystemsRegister =
    new SubSystemsRegister()

}
