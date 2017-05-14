package com.purplekingdomgames.sbtindigo

import sbt._

object SbtIndigo extends sbt.AutoPlugin {

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {
    val hello: TaskKey[Unit] = taskKey[Unit]("say hello")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    hello := helloTask.value
  )

  lazy val helloTask: Def.Initialize[Task[Unit]] =
    Def.task {
      println("hello from indigo!")
    }
}
