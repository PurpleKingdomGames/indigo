lazy val sbtIndigoVersion = SbtIndigoVersion.getVersion

addSbtPlugin("org.scala-js"    %% "sbt-scalajs"     % "1.3.1")
addSbtPlugin("io.indigoengine" %% "sbt-indigo"      % sbtIndigoVersion)
addSbtPlugin("org.wartremover" %% "sbt-wartremover" % "2.4.13")
