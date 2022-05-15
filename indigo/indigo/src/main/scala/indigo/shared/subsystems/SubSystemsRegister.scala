package indigo.shared.subsystems

import indigo.shared.IndigoLogger
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemFrameContext

import scalajs.js

final class SubSystemsRegister() {

  val stateMap: scalajs.js.Dictionary[Object] = scalajs.js.Dictionary.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var registeredSubSystems: js.Array[RegisteredSubSystem] = js.Array()

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def register(newSubSystems: Batch[SubSystem]): Batch[GlobalEvent] =
    newSubSystems.map(initialiseSubSystem).sequence match {
      case oe @ Outcome.Error(e, _) =>
        IndigoLogger.error("Error during subsystem setup - Halting.")
        IndigoLogger.error("Crash report:")
        IndigoLogger.error(oe.reportCrash)
        throw e

      case Outcome.Result(toBeRegistered, events) =>
        registeredSubSystems = registeredSubSystems ++ toBeRegistered.toJSArray
        events
    }

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  private def initialiseSubSystem(subSystem: SubSystem): Outcome[RegisteredSubSystem] = {
    val key = subSystem.id.toString
    val res = RegisteredSubSystem(key, subSystem)

    subSystem.initialModel.map { model =>
      stateMap.put(key, model.asInstanceOf[Object])

      res
    }
  }

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def update(frameContext: SubSystemFrameContext, globalEvents: js.Array[GlobalEvent]): Outcome[SubSystemsRegister] = {
    def outcomeEvents: Outcome[Batch[GlobalEvent]] =
      Outcome
        .sequence(
          Batch(
            registeredSubSystems
              .map { rss =>
                val key       = rss.id
                val subSystem = rss.subSystem

                val filteredEvents: js.Array[subSystem.EventType] =
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
          )
        )
        .map(_.flatMap(identity))

    outcomeEvents.flatMap(l => Outcome(this, l))
  }

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
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

final case class RegisteredSubSystem(id: String, subSystem: SubSystem) derives CanEqual
