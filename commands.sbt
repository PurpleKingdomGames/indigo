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

// Rebuild ScalaDocs and open in Firefox
addCommandAlias(
  "readdocs",
  applyCommand(coreProjects, "doc", PlatformSuffix.JVMOnly) +
    (List(
      "openshareddocs",
      "openindigodocs",
      "openindigoextsdocs"
    ).mkString(";", ";", ""))
)

addCommandAlias(
  "buildIndigo",
  applyCommand(coreProjects, "compile", PlatformSuffix.All)
)
addCommandAlias(
  "buildDev",
  applyCommand(devProjects, "compile", List(PlatformSuffix.Ignore, PlatformSuffix.JVM)) +
    List(
      "framework/compile",
      "server/compile"
    ).mkString(";", ";", "")
)
addCommandAlias(
  "buildExamples",
  applyCommand(exampleProjects, "compile", PlatformSuffix.Omit)
)

addCommandAlias(
  "buildAllNoClean",
  List(
    "buildIndigo",
    "buildDev",
    "buildExamples"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "buildAll",
  List(
    "clean",
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
  applyCommand(exampleProjects, "test", PlatformSuffix.JVMOnly)
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
    "clean",
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
    "clean",
    "testAllNoCleanJVM"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testCompileIndigo",
  applyCommand(coreProjects, "test:compile", PlatformSuffix.All)
)
addCommandAlias(
  "testCompileDev",
  applyCommand(devProjects, "test:compile", List(PlatformSuffix.Ignore, PlatformSuffix.JVM)) +
    List(
      "framework/test",
      "server/test"
    ).mkString(";", ";", "")
)
addCommandAlias(
  "testCompileExamples",
  applyCommand(exampleProjects, "test:compile", PlatformSuffix.Omit)
)
addCommandAlias(
  "testCompileAllNoClean",
  List(
    "testCompileIndigo",
    "testCompileDev",
    "testCompileExamples"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testCompileAll",
  List(
    "clean",
    "testCompileAllNoClean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "localPublish",
  List(
    "clean",
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
