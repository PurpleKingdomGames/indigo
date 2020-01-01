lazy val coreProjects: List[String] =
  List(
    "shared",
    "circe12",
    "indigoPlatforms",
    "indigo",
    "indigoExts"
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
    applyCommand(coreProjects, command, PlatformSuffix.All)
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
  "testAllNoCleanJS",
  List(
    "testIndigoJS"
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
    "testIndigoJVM"
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
  "buildIndigo",
  applyCommand(coreProjects, "compile", PlatformSuffix.All)
)
addCommandAlias(
  "localPublishIndigo",
  applyCommand(coreProjects, "publishLocal", PlatformSuffix.All)
)

addCommandAlias(
  "localPublish",
  List(
    "cleanAll",
    "buildIndigo",
    "localPublishIndigo"
  ).mkString(";", ";", "")
)
