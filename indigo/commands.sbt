lazy val releaseProjects: List[String] =
  List(
    "shared",
    "indigoJsonCirce",
    "indigoJsonUPickle",
    "indigoPlatforms",
    "indigoCore",
    "indigoExtras",
    "indigo",
    "facades"
  )

lazy val coreProjects: List[String] =
  releaseProjects ++ List(
    "sandbox",
    "perf"
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
  applyCommand(
    coreProjects.filterNot(name => name == "indigoExtsExp" || name == "sandbox" || name == "perf"),
    "publishLocal",
    PlatformSuffix.All
  )
)

addCommandAlias(
  "localPublish",
  List(
    "cleanAll",
    "buildIndigo",
    "localPublishIndigo"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "localPublishNoClean",
  List(
    "buildIndigo",
    "localPublishIndigo"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuild",
  List(
    "sandboxJS/fastOptJS",
    "sandboxJS/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuildJS",
  List(
    "buildAllNoClean",
    "sandboxJS/fastOptJS",
    "sandboxJS/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuildJVM",
  List(
    "buildAllNoClean",
    "sandboxJVM/compile",
    "sandboxJVM/assembly",
    "sandboxJVM/indigoBuildJVM"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfBuildJS",
  List(
    "buildAllNoClean",
    "perfJS/fastOptJS",
    "perfJS/indigoBuildJS"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfBuildJVM",
  List(
    "buildAllNoClean",
    "perfJVM/compile",
    "perfJVM/assembly",
    "perfJVM/indigoBuildJVM"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "indigoRelease",
  List(
    "cleanAll",
    "buildAllNoClean",
    "testAllNoCleanJVM",
    "testAllNoCleanJS",
    applyCommand(releaseProjects, "publishSigned", PlatformSuffix.All)
  ).mkString(";", ";", "")
)
