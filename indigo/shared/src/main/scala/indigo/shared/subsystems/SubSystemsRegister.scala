package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemFrameContext
import scala.collection.mutable

import java.util.UUID

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class SubSystemsRegister(subSystems: List[SubSystem]) {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val stateMap: mutable.HashMap[String, Object] = new mutable.HashMap[String, Object]()

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var registeredSubSystems: List[RegisteredSubSystem] =
    subSystems.map(initialiseSubSystem)

  def register(newSubSystems: List[SubSystem]): Unit =
    registeredSubSystems = registeredSubSystems ++ newSubSystems.map(initialiseSubSystem)

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def initialiseSubSystem(subSystem: SubSystem): RegisteredSubSystem = {
    val key = UUID.randomUUID().toString
    val res = RegisteredSubSystem(key, subSystem)

    stateMap.put(key, subSystem.initialModel.asInstanceOf[Object])

    res
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def update(frameContext: SubSystemFrameContext): GlobalEvent => Outcome[SubSystemsRegister] =
    (e: GlobalEvent) => {
      val statelessEvents: List[GlobalEvent] =
        registeredSubSystems.flatMap { rss =>
          rss.subSystem.eventFilter(e) match {
            case None =>
              Nil

            case Some(ee) =>
              val key                                        = rss.id
              val model: rss.subSystem.SubSystemModel        = stateMap(key).asInstanceOf[rss.subSystem.SubSystemModel]
              val out: Outcome[rss.subSystem.SubSystemModel] = rss.subSystem.update(frameContext, model.asInstanceOf[rss.subSystem.SubSystemModel])(ee)
              stateMap.put(key, out.state.asInstanceOf[Object])
              out.globalEvents
          }
        }

      Outcome(this).addGlobalEvents(statelessEvents)
    }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def present(frameContext: SubSystemFrameContext): SceneUpdateFragment =
    registeredSubSystems
      .map { rss =>
        rss.subSystem.present(
          frameContext,
          stateMap(rss.id).asInstanceOf[rss.subSystem.SubSystemModel]
        )
      }
      .foldLeft(SceneUpdateFragment.empty)(_ |+| _)

  def size: Int =
    registeredSubSystems.length

}

final case class RegisteredSubSystem(id: String, subSystem: SubSystem)
