package example

import sbt.{Def, _}
import Keys._

object SbtIndigo extends AutoPlugin {
  object autoImport {
    val greeting: SettingKey[String] = settingKey[String]("greeting")
    val hello: TaskKey[Unit] = taskKey[Unit]("say hello")
  }
  import autoImport._
  override def trigger = allRequirements
  override lazy val buildSettings = Seq(
    greeting := "Hi!",
    hello := helloTask.value)
  lazy val helloTask: Def.Initialize[Task[Unit]] =
    Def.task {
      println(greeting.value)
    }
}
