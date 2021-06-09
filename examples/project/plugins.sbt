lazy val sbtIndigoVersion = SbtIndigoVersion.getVersion

addSbtPlugin("org.scala-js"    %% "sbt-scalajs"     % "1.6.0")
addSbtPlugin("io.indigoengine" %% "sbt-indigo"      % sbtIndigoVersion)
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.17")
