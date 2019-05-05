lazy val coreProjects: List[String] =
  List(
    "shared",
    "circe9",
    "indigoPlatforms",
    "indigo",
    "indigoExts"
  )

lazy val devProjects: List[String] =
  List("sandbox", "perf")

lazy val exampleProjects: List[String] =
  List(
    "basicSetup",
    "subSystems",
    "scenesSetup",
    "fullSetup",
    "button",
    "http",
    "text",
    "graphic",
    "sprite",
    "websocket",
    "inputfield",
    "audio",
    "group",
    "automata"
  )

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
    applyCommand(coreProjects, command, PlatformSuffix.All),
    applyCommand(devProjects, command, List(PlatformSuffix.Ignore, PlatformSuffix.JVM)),
    applyCommand(exampleProjects, command, PlatformSuffix.Omit),
    ";framework/clean",
    ";server/clean"
  ).mkString

// Rebuild ScalaDocs and open in Firefox
addCommandAlias(
  "readdocs",
  applyCommand(coreProjects, "doc", PlatformSuffix.JVMOnly) +
    List(
      "openshareddocs",
      "openindigodocs",
      "openindigoextsdocs"
    ).mkString(";", ";", "")
)

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
  "testIndigoJS",
  applyCommand(coreProjects, "test", PlatformSuffix.JSOnly)
)
addCommandAlias(
  "testIndigoJVM",
  applyCommand(coreProjects, "test", PlatformSuffix.JVMOnly)
)
addCommandAlias(
  "testDevJS",
  applyCommand(devProjects, "test", PlatformSuffix.JSOnly) +
    List(
      "framework/test",
      "server/test"
    ).mkString(";", ";", "")
)
addCommandAlias(
  "testDevJVM",
  applyCommand(devProjects, "test", PlatformSuffix.JVMOnly) +
    List(
      "framework/test",
      "server/test"
    ).mkString(";", ";", "")
)
addCommandAlias(
  "testExamplesJS",
  applyCommand(exampleProjects, "test", PlatformSuffix.Omit)
)
addCommandAlias(
  "testExamplesJVM",
  applyCommand(exampleProjects, "test", PlatformSuffix.Omit) // Currently not compiling examples to JVM
)
addCommandAlias(
  "testAllNoCleanJS",
  List(
    "testIndigoJS",
    "testDevJS",
    "testExamplesJS"
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
    "testIndigoJVM",
    "testDevJVM",
    "testExamplesJVM"
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
  "localPublish",
  List(
    "cleanAll",
    "buildIndigo"
  ).mkString(";", ";", "") +
    applyCommand(coreProjects, "test:publishLocal", PlatformSuffix.All)
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
