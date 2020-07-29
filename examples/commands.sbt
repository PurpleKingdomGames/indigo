lazy val exampleProjects: List[String] =
  List(
    "basicSetup",
    "subSystems",
    "scenesSetup",
    "button",
    "http",
    "text",
    "graphic",
    "sprite",
    "websocket",
    "inputfield",
    "audio",
    "group",
    "automata",
    "fireworks",
    "lighting",
    "distortion",
    "effects",
    "assetLoading",
    "tiled"
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
    applyCommand(exampleProjects, command, PlatformSuffix.Omit)
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
  "fastOptJSAll",
  applyToAll("fastOptJS")
)
addCommandAlias(
  "indigoBuildAll",
  applyToAll("indigoBuildJS")
)
addCommandAlias(
  "buildExamples",
  List(
    "buildAllNoClean",
    "fastOptJSAll",
    "indigoBuildAll"
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
  "buildExamples",
  applyCommand(exampleProjects, "compile", PlatformSuffix.All)
)

addCommandAlias(
  "fireworksBuild",
  List(
    "fireworks/fastOptJS",
    "fireworks/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "lightingBuild",
  List(
    "lighting/fastOptJS",
    "lighting/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "distortionBuild",
  List(
    "distortion/fastOptJS",
    "distortion/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "effectsBuild",
  List(
    "effects/fastOptJS",
    "effects/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "assetLoadingBuild",
  List(
    "assetLoading/fastOptJS",
    "assetLoading/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "tiledBuild",
  List(
    "tiled/fastOptJS",
    "tiled/indigoBuildJS"
  ).mkString(";", ";", "")
)
