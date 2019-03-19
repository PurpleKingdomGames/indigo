package indigoexts.jobs

trait Job extends Product with Serializable {
  val jobName: JobName
  val isLocal: Boolean
}

final case class JobName(value: String) extends AnyVal
