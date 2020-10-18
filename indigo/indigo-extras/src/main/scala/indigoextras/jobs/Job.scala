package indigoextras.jobs

/**
  * A trait that when extended represents a job that can be done.
  * Jobs have names and can be local to the worker, or globalablly available
  */
trait Job extends Product with Serializable {
  val jobName: JobName
  val isLocal: Boolean
  val priority: Int
}

/**
  * A simple type to distingush job names from other strings.
  *
  * @param value the name of the job
  */
final case class JobName(value: String) extends AnyVal
