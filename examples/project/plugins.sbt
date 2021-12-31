lazy val sbtIndigoVersion = SbtIndigoVersion.getVersion

addSbtPlugin("org.scala-js"             %% "sbt-scalajs"  % "1.8.0")
addSbtPlugin("io.indigoengine"          %% "sbt-indigo"   % sbtIndigoVersion)
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.20")
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix" % "0.9.31")
addSbtPlugin("com.github.reibitto"       % "sbt-welcome"  % "0.2.2")
