

lazy val sbtIndigoVersion = SbtIndigoVersion.getVersion

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1")
addSbtPlugin("io.indigoengine" % "sbt-indigo"      % sbtIndigoVersion)
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.7")
addSbtPlugin("com.eed3si9n"    % "sbt-assembly"    % "0.14.10")
