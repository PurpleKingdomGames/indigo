package indigoextras.jobs

/** A trait that when extended represents a job that can be done. Jobs have names and can be local to the worker, or
  * globalablly available
  */
trait Job extends Product with Serializable derives CanEqual {
  val jobName: JobName
  val isLocal: Boolean
  val priority: Int
}

/** A simple type to distingush job names from other strings.
  *
  * @param value
  *   the name of the job
  */
opaque type JobName = String
object JobName:
  inline def apply(value: String): JobName = value
