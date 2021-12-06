lazy val sbtIndigoVersion = SbtIndigoVersion.getVersion

libraryDependencies += "org.scala-js" %% "scalajs-env-nodejs" % "1.2.1"

addSbtPlugin("org.scala-js"    %% "sbt-scalajs"     % "1.7.1")
addSbtPlugin("io.indigoengine" %% "sbt-indigo"      % sbtIndigoVersion)
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.17")
