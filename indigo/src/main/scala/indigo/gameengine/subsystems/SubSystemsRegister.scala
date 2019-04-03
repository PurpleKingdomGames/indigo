package indigo.gameengine.subsystems

import indigo.time.GameTime
import indigo.gameengine.Outcome
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.dice.Dice

final case class SubSystemsRegister(registeredSubSystems: List[SubSystem]) {

  def add(subSystems: SubSystem*): SubSystemsRegister =
    SubSystemsRegister.add(this, subSystems.toList)

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => OutcomesRegister =
    SubSystemsRegister.update(this, gameTime, dice)

  def render(gameTime: GameTime): SceneUpdateFragment =
    SubSystemsRegister.render(this, gameTime)

  def reports: List[String] =
    SubSystemsRegister.reports(this)

}

object SubSystemsRegister {
  val empty: SubSystemsRegister =
    SubSystemsRegister(Nil)

  def add(register: SubSystemsRegister, subSystems: List[SubSystem]): SubSystemsRegister =
    register.copy(registeredSubSystems = register.registeredSubSystems ++ subSystems)

  def update(register: SubSystemsRegister, gameTime: GameTime, dice: Dice): GlobalEvent => OutcomesRegister = {
    case e: GlobalEvent =>
      val updated = register.registeredSubSystems.map { ss =>
        ss.eventFilter(e).map(ee => ss.update(gameTime, dice)(ee)).getOrElse(Outcome(ss, Nil))
      }

      OutcomesRegister(
        register.copy(registeredSubSystems = updated.map(_.state)),
        updated.flatMap(_.globalEvents)
      )

    case _ =>
      OutcomesRegister(register, Nil)
  }

  def render(register: SubSystemsRegister, gameTime: GameTime): SceneUpdateFragment =
    register.registeredSubSystems.map(_.render(gameTime)).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def reports(register: SubSystemsRegister): List[String] =
    register.registeredSubSystems.map(_.report)

}

final case class OutcomesRegister(register: SubSystemsRegister, events: List[GlobalEvent])
