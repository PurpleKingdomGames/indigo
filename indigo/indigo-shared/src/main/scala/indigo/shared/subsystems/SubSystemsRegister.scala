package indigo.shared.subsystems

import indigo.shared.IndigoLogger
import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemFrameContext
import scala.collection.mutable

import java.util.UUID

// @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class SubSystemsRegister() {

  // @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val stateMap: mutable.HashMap[String, Object] = new mutable.HashMap[String, Object]()

  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var registeredSubSystems: List[RegisteredSubSystem] = Nil

  def register(newSubSystems: List[SubSystem]): List[GlobalEvent] =
    newSubSystems.map(initialiseSubSystem).sequence match {
      case oe @ Outcome.Error(e, _) =>
        IndigoLogger.error("Error during subsystem setup - Halting.")
        IndigoLogger.error("Crash report:")
        IndigoLogger.error(oe.reportCrash)
        throw e

      case Outcome.Result(toBeRegistered, events) =>
        registeredSubSystems = registeredSubSystems ++ toBeRegistered
        events
    }

  // @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def initialiseSubSystem(subSystem: SubSystem): Outcome[RegisteredSubSystem] = {
    val key = UUID.randomUUID().toString
    val res = RegisteredSubSystem(key, subSystem)

    subSystem.initialModel.map { model =>
      stateMap.put(key, model.asInstanceOf[Object])

      res
    }
  }

  // @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def update(frameContext: SubSystemFrameContext, globalEvents: List[GlobalEvent]): Outcome[SubSystemsRegister] = {
    def outcomeEvents: Outcome[List[GlobalEvent]] =
      registeredSubSystems
        .map { rss =>
          val key       = rss.id
          val subSystem = rss.subSystem

          val filteredEvents: List[subSystem.EventType] =
            globalEvents
              .map(subSystem.eventFilter)
              .collect { case Some(e) => e }

          val model: subSystem.SubSystemModel = stateMap(key).asInstanceOf[subSystem.SubSystemModel]

          filteredEvents.foldLeft(Outcome(model)) { (acc, e) =>
            acc.flatMap { m =>
              subSystem.update(frameContext, m)(e)
            }
          } match {
            case Outcome.Error(e, _) =>
              Outcome.raiseError(e)

            case Outcome.Result(state, globalEvents) =>
              stateMap.put(key, state.asInstanceOf[Object])
              Outcome(globalEvents)
          }
        }
        .sequence
        .map(_.flatten)

    outcomeEvents.flatMap(l => Outcome(this, l))
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
