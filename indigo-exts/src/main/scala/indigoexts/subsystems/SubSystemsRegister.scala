package indigoexts.subsystems

import indigo.time.GameTime
import indigo.gameengine.Outcome
import indigo.gameengine.Outcome._
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.dice.Dice

final class SubSystemsRegister(val registeredSubSystems: List[SubSystem]) {

  def add(subSystems: SubSystem*): SubSystemsRegister =
    SubSystemsRegister.add(this, subSystems.toList)

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[SubSystemsRegister] =
    SubSystemsRegister.update(this, gameTime, dice)

  def render(gameTime: GameTime): SceneUpdateFragment =
    SubSystemsRegister.render(this, gameTime)

  def reports: List[String] =
    SubSystemsRegister.reports(this)

}

object SubSystemsRegister {

  def apply(subSystems: List[SubSystem]): SubSystemsRegister =
    new SubSystemsRegister(subSystems)

  val empty: SubSystemsRegister =
    SubSystemsRegister(Nil)

  def add(register: SubSystemsRegister, subSystems: List[SubSystem]): SubSystemsRegister =
    SubSystemsRegister(register.registeredSubSystems ++ subSystems)

  def update(register: SubSystemsRegister, gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[SubSystemsRegister] = {
    case e: GlobalEvent =>
      register.registeredSubSystems
        .map { ss =>
          ss.eventFilter(e)
            .map(ee => ss.update(gameTime, dice)(ee))
            .getOrElse(Outcome(ss, Nil))
        }
        .sequence
        .mapState(ss => SubSystemsRegister(ss))

    case _ =>
      Outcome(register)
  }

  def render(register: SubSystemsRegister, gameTime: GameTime): SceneUpdateFragment =
    register.registeredSubSystems.map(_.render(gameTime)).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def reports(register: SubSystemsRegister): List[String] =
    register.registeredSubSystems.map(_.report)

}
