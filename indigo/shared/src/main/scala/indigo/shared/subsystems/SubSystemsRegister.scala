package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemFrameContext
import scala.collection.mutable

import java.util.UUID

// @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class SubSystemsRegister(subSystems: List[SubSystem]) {

  // @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val stateMap: mutable.HashMap[String, Object] = new mutable.HashMap[String, Object]()

  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var registeredSubSystems: List[RegisteredSubSystem] =
    subSystems.map(initialiseSubSystem)

  def register(newSubSystems: List[SubSystem]): Unit =
    registeredSubSystems = registeredSubSystems ++ newSubSystems.map(initialiseSubSystem)

  // @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def initialiseSubSystem(subSystem: SubSystem): RegisteredSubSystem = {
    val key = UUID.randomUUID().toString
    val res = RegisteredSubSystem(key, subSystem)

    stateMap.put(key, subSystem.initialModel.asInstanceOf[Object])

    res
  }

  // @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def update(frameContext: SubSystemFrameContext, globalEvents: List[GlobalEvent]): Outcome[SubSystemsRegister] = {
    val outcomeEvents = registeredSubSystems.flatMap { rss =>
      val key       = rss.id
      val subSystem = rss.subSystem

      val filteredEvents: List[subSystem.EventType] =
        globalEvents
          .map(subSystem.eventFilter)
          .collect { case Some(e) => e }

      val model: subSystem.SubSystemModel = stateMap(key).asInstanceOf[subSystem.SubSystemModel]

      val out =
        filteredEvents.foldLeft(Outcome(model)) { (acc, e) =>
          acc.flatMapState { m =>
            subSystem.update(frameContext, m)(e)
          }
        }

      stateMap.put(key, out.state.asInstanceOf[Object])
      out.globalEvents
    }

    Outcome(this, outcomeEvents)
  }

  // @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def present(frameContext: SubSystemFrameContext): Outcome[SceneUpdateFragment] =
    registeredSubSystems
      .map { rss =>
        rss.subSystem.present(
          frameContext,
          stateMap(rss.id).asInstanceOf[rss.subSystem.SubSystemModel]
        )
      }
      .foldLeft(Outcome(SceneUpdateFragment.empty))((acc, next) => Outcome.merge(acc, next)(_ |+| _))

  def size: Int =
    registeredSubSystems.length

}

final case class RegisteredSubSystem(id: String, subSystem: SubSystem)
