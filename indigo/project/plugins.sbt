lazy val sbtIndigoVersion = "0.0.12-SNAPSHOT"

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1")
addSbtPlugin("indigo"          % "sbt-indigo"      % sbtIndigoVersion)
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.3")
addSbtPlugin("com.eed3si9n"    % "sbt-assembly"    % "0.14.10")
