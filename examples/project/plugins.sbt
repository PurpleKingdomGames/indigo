lazy val sbtIndigoVersion = SbtIndigoVersion.getVersion

addSbtPlugin("org.scala-js"             %% "sbt-scalajs"  % "1.13.0")
addSbtPlugin("io.indigoengine"          %% "sbt-indigo"   % sbtIndigoVersion)
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.3.0")
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix" % "0.9.31")
addSbtPlugin("com.github.reibitto"       % "sbt-welcome"  % "0.2.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
