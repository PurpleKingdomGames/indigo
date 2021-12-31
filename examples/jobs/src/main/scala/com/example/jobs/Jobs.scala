package com.example.jobs

import indigo._
import indigoextras.datatypes.IncreaseTo
import indigoextras.jobs.Job
import indigoextras.jobs.JobName

final case class ChopDown(index: Int, position: Point) extends Job {
  val isLocal: Boolean = false
  val jobName: JobName = JobName("chop down tree")
  val priority: Int    = 25
}

final case class CollectWood(wood: Wood) extends Job {
  val isLocal: Boolean = false
  val jobName: JobName = JobName("collect wood")
  val priority: Int    = 10
}

final case class RemoveTree(index: Int)        extends GlobalEvent
final case class DropWood(treePosition: Point) extends GlobalEvent
final case class RemoveWood(id: BindingKey)    extends GlobalEvent

final case class Wander(to: Point) extends Job {
  val isLocal: Boolean = true
  val jobName: JobName = JobName("bob is pacing")
  val priority: Int    = 50
}

final case class Idle(percentDone: IncreaseTo) extends Job {
  val isLocal: Boolean = true
  val jobName: JobName = JobName("bob is pacing")
  val priority: Int    = 100
}
