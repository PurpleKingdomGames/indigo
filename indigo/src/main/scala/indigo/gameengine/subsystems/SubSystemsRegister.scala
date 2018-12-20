package indigo.gameengine.subsystems

import indigo.gameengine.GameTime
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.SceneUpdateFragment

final case class SubSystemsRegister(registeredSubSystems: List[SubSystem]) {

  def add(subSystems: SubSystem*): SubSystemsRegister =
    SubSystemsRegister.add(this, subSystems.toList)

  def update(gameTime: GameTime): GlobalEvent => UpdatedSubSystemsRegister =
    SubSystemsRegister.update(this, gameTime)

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

  def update(register: SubSystemsRegister, gameTime: GameTime): GlobalEvent => UpdatedSubSystemsRegister = {
    case e: GlobalEvent =>
      val updated = register.registeredSubSystems.map { ss =>
        ss.eventFilter(e).map(ee => ss.update(gameTime)(ee)).getOrElse(UpdatedSubSystem(ss, Nil))
      }

      UpdatedSubSystemsRegister(
        register.copy(registeredSubSystems = updated.map(_.subSystem)),
        updated.flatMap(_.events)
      )

    case _ =>
      UpdatedSubSystemsRegister(register, Nil)
  }

  def render(register: SubSystemsRegister, gameTime: GameTime): SceneUpdateFragment =
    register.registeredSubSystems.map(_.render(gameTime)).foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def reports(register: SubSystemsRegister): List[String] =
    register.registeredSubSystems.map(_.report)

}

final case class UpdatedSubSystemsRegister(register: SubSystemsRegister, events: List[GlobalEvent])
