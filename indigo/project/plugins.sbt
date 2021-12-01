libraryDependencies += "org.scala-js" %% "scalajs-env-nodejs" % "1.2.1"

addSbtPlugin("org.scala-js"             %% "sbt-scalajs"        % "1.7.1")
addSbtPlugin("io.indigoengine"          %% "sbt-indigo"         % SbtIndigoVersion.getVersion)
addSbtPlugin("org.xerial.sbt"           %% "sbt-sonatype"       % "3.9.7")
addSbtPlugin("com.jsuereth"             %% "sbt-pgp"            % "2.0.1")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"       % "0.1.20")
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix"       % "0.9.31")
addSbtPlugin("org.scalameta"             % "sbt-mdoc"           % "2.2.24")
addSbtPlugin("com.github.sbt"            % "sbt-unidoc"         % "0.5.0")
addSbtPlugin("org.scala-js"              % "sbt-jsdependencies" % "1.0.2")
