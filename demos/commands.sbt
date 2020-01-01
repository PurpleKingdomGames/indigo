
lazy val demoProjects: List[String] =
  List("sandbox", "perf")

def applyCommand(projects: List[String], command: String, platforms: List[PlatformSuffix]): String =
  platforms match {
    case Nil =>
      projects.map(p => p + "/" + command).mkString(";", ";", "")

    case ps =>
      projects
        .flatMap { p =>
          ps.map { plt =>
            p + plt.suffix + "/" + command
          }
        }
        .mkString(";", ";", "")
  }

def applyToAll(command: String): String =
  List(
    applyCommand(demoProjects, command, List(PlatformSuffix.Ignore, PlatformSuffix.JVM))
  ).mkString

addCommandAlias(
  "cleanAll",
  applyToAll("clean")
)

addCommandAlias(
  "buildAllNoClean",
  applyToAll("compile")
)
addCommandAlias(
  "buildAll",
  List(
    "cleanAll",
    "buildAllNoClean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testDemoJS",
  applyCommand(demoProjects, "test", PlatformSuffix.Omit)
)
addCommandAlias(
  "testDemoJVM",
  applyCommand(demoProjects, "test", PlatformSuffix.JVMOnly)
)
addCommandAlias(
  "testAllNoCleanJS",
  List(
    "testDemoJS"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testAllJS",
  List(
    "cleanAll",
    "testAllNoCleanJS"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testAllNoCleanJVM",
  List(
    "testDemoJVM"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testAllJVM",
  List(
    "cleanAll",
    "testAllNoCleanJVM"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testCompileAllNoClean",
  applyToAll("test:compile")
)
addCommandAlias(
  "testCompileAll",
  List(
    "cleanAll",
    "testCompileAllNoClean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuildJS",
  List(
    "buildIndigo",
    "sandbox/test",
    "sandbox/fastOptJS",
    "sandbox/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuildJVM",
  List(
    "sandboxJVM/clean",
    "sandboxJVM/compile",
    "sandboxJVM/assembly",
    "sandboxJVM/indigoBuildJVM"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfBuildJS",
  List(
    "buildIndigo",
    "perf/test",
    "perf/fastOptJS",
    "perf/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfBuildJVM",
  List(
    "buildIndigo",
    "perfJVM/test",
    "perfJVM/assembly",
    "perfJVM/indigoBuildJVM"
  ).mkString(";", ";", "")
)
