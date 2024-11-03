lazy val releaseProjects: List[String] =
  List(
    "indigo",
    "indigoJsonCirce",
    "indigoExtras",
    "tyrianIndigoBridge"
  )

lazy val coreProjects: List[String] =
  releaseProjects ++ List(
    "sandbox",
    "perf",
    "shader",
  )

val allProjects = List("indigoProject") // the aggregate

def applyCommand(projects: List[String], command: String): String =
  projects.map(p => p + "/" + command).mkString(";", ";", "")

def applyCrossCommand(projects: List[String], command: String): String =
  projects.map(p => "+" + p + "/" + command).mkString(";", ";", "")

def applyToAll(command: String): String =
  List(
    applyCommand(allProjects, command)
  ).mkString

def applyCrossToAll(command: String): String =
  List(
    applyCrossCommand(allProjects, command)
  ).mkString

def applyToAllReleaseable(command: String): String =
  List(
    applyCommand(releaseProjects, command)
  ).mkString

def applyCrossToAllReleaseable(command: String): String =
  List(
    applyCrossCommand(releaseProjects, command)
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
  applyToAll("publishLocal")
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
  "sandboxBuildFull",
  List(
    "buildAllNoClean",
    "sandbox/fullOptJS",
    "sandbox/indigoBuildFull"
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
  "sandboxRunFull",
  List(
    "buildAllNoClean",
    "sandbox/fullOptJS",
    "sandbox/indigoRunFull"
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
  "perfRunFull",
  List(
    "buildAllNoClean",
    "perf/fullOptJS",
    "perf/indigoRunFull"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "shaderBuild",
  List(
    "buildAllNoClean",
    "shader/fastOptJS",
    "shader/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "shaderRun",
  List(
    "buildAllNoClean",
    "shader/fastOptJS",
    "shader/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "shaderRunFull",
  List(
    "buildAllNoClean",
    "shader/fullOptJS",
    "shader/indigoRunFull"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "physicsRun",
  List(
    "buildAllNoClean",
    "physics/fastOptJS",
    "physics/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "physicsRunFull",
  List(
    "buildAllNoClean",
    "physics/fullOptJS",
    "physics/indigoRunFull"
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

// -- cross building --

addCommandAlias(
  "crossBuildIndigo",
  applyCrossCommand(allProjects, "compile")
)
addCommandAlias(
  "crossLocalPublishIndigo",
  applyCrossCommand(releaseProjects, "publishLocal")
)
addCommandAlias(
  "crossLocalPublishNoClean",
  List("crossLocalPublishIndigo").mkString(";", ";", "")
)

addCommandAlias(
  "crossTestIndigo",
  applyCrossCommand(allProjects, "test")
)
addCommandAlias(
  "crossTestAllNoClean",
  List(
    "crossTestIndigo"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "crossCleanAll",
  applyCrossToAll("clean")
)

addCommandAlias(
  "crossIndigoRelease",
  List(
    "crossCleanAll",
    "crossBuildIndigo",
    "crossIndigoPublishAllSigned",
    "sonatypeBundleRelease"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "crossIndigoPublishAllSigned",
  applyCrossToAllReleaseable("publishSigned")
)
