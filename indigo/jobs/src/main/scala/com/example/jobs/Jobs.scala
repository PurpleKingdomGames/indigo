package com.example.jobs

import indigo._
import indigoextras.jobs.Job
import indigoextras.jobs.JobName
import indigoextras.datatypes.TimeVaryingValue

final case class ChopDown(index: Int, position: Point) extends Job {
  val isLocal: Boolean = false
  val jobName: JobName = JobName("chop down tree")
}

final case class CollectWood(wood: Wood) extends Job {
  val isLocal: Boolean = false
  val jobName: JobName = JobName("collect wood")
}

final case class RemoveTree(index: Int)        extends GlobalEvent
final case class DropWood(treePosition: Point) extends GlobalEvent
final case class RemoveWood(id: BindingKey)    extends GlobalEvent

final case class Pace(to: Point) extends Job {
  val isLocal: Boolean = true
  val jobName: JobName = JobName("bob is pacing")
}

final case class Idle(percentDone: TimeVaryingValue[Int]) extends Job {
  val isLocal: Boolean = true
  val jobName: JobName = JobName("bob is pacing")
}
