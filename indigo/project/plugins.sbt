resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("org.scala-js"             %% "sbt-scalajs"        % "1.13.1")
addSbtPlugin("io.indigoengine"          %% "sbt-indigo"         % SbtIndigoVersion.getVersion)
addSbtPlugin("org.xerial.sbt"           %% "sbt-sonatype"       % "3.9.7")
addSbtPlugin("com.jsuereth"             %% "sbt-pgp"            % "2.0.1")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"       % "0.3.0")
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix"       % "0.9.31")
addSbtPlugin("org.scalameta"             % "sbt-mdoc"           % "2.3.4")
addSbtPlugin("com.github.sbt"            % "sbt-unidoc"         % "0.5.0")
addSbtPlugin("org.scala-js"              % "sbt-jsdependencies" % "1.0.2")
addSbtPlugin("com.github.reibitto"       % "sbt-welcome"        % "0.2.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
