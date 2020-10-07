lazy val releaseProjects: List[String] =
  List(
    "shared",
    "indigoJsonCirce",
    "indigoPlatforms",
    "indigoCore",
    "indigoExtras",
    "indigo",
    "facades"
  )

lazy val coreProjects: List[String] =
  releaseProjects ++ List(
    // "sandbox",
    // "perf"
  )

def applyCommand(projects: List[String], command: String): String =
  projects.map(p => p + "/" + command).mkString(";", ";", "")

def applyToAll(command: String): String =
  List(
    applyCommand(coreProjects, command)
  ).mkString

def applyToAllReleaseable(command: String): String =
  List(
    applyCommand(releaseProjects, command)
  ).mkString

// Rebuild ScalaDocs and open in Firefox
addCommandAlias(
  "readdocs",
  applyCommand(coreProjects, "doc") +
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
  "testIndigo",
  applyCommand(coreProjects, "test")
)
addCommandAlias(
  "testAllNoClean",
  List(
    "testIndigo"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testAll",
  List(
    "cleanAll",
    "testAllNoClean"
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
  applyCommand(coreProjects, "compile")
)
addCommandAlias(
  "localPublishIndigo",
  applyCommand(
    coreProjects.filterNot(name => name == "indigoExtsExp" || name == "sandbox" || name == "perf"),
    "publishLocal"
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
    "buildAllNoClean",
    "sandbox/fastOptJS",
    "sandbox/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxRun",
  List(
    "buildAllNoClean",
    "sandbox/fastOptJS",
    "sandbox/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfBuild",
  List(
    "buildAllNoClean",
    "perf/fastOptJS",
    "perf/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfRun",
  List(
    "buildAllNoClean",
    "perf/fastOptJS",
    "perf/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "indigoPublishAllSigned",
  applyToAllReleaseable("publishSigned")
)

addCommandAlias(
  "indigoRelease",
  List(
    "cleanAll",
    "buildAllNoClean",
    "testAllNoClean",
    "indigoPublishAllSigned",
    "sonatypeBundleRelease"
  ).mkString(";", ";", "")
)
