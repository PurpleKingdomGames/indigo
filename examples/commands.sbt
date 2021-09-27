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
    "tiled",
    "radio",
    "jobs",
    "inputmapper",
    "errors",
    "confetti"
  )

def applyCommand(projects: List[String], command: String): String =
  projects.map(p => p + "/" + command).mkString(";", ";", "")

def applyToAll(command: String): String =
  List(
    applyCommand(exampleProjects, command)
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
  "fastOptAll",
  applyToAll("fastOptJS")
)
addCommandAlias(
  "indigoBuildAll",
  applyToAll("indigoBuild")
)
addCommandAlias(
  "buildExamples",
  List(
    "buildAllNoClean",
    "fastOptAll",
    "indigoBuildAll"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testExamples",
  applyCommand(exampleProjects, "test")
)
addCommandAlias(
  "testAllNoClean",
  List(
    "testExamples"
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
  "buildExamples",
  applyCommand(exampleProjects, "compile")
)

addCommandAlias(
  "fireworksBuild",
  List(
    "fireworks/fastOptJS",
    "fireworks/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "fireworksRun",
  List(
    "fireworks/fastOptJS",
    "fireworks/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "lightingBuild",
  List(
    "lighting/fastOptJS",
    "lighting/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "lightingRun",
  List(
    "lighting/fastOptJS",
    "lighting/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "distortionBuild",
  List(
    "distortion/fastOptJS",
    "distortion/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "distortionRun",
  List(
    "distortion/fastOptJS",
    "distortion/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "effectsBuild",
  List(
    "effects/fastOptJS",
    "effects/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "effectsRun",
  List(
    "effects/fastOptJS",
    "effects/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "assetLoadingBuild",
  List(
    "assetLoading/fastOptJS",
    "assetLoading/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "assetLoadingRun",
  List(
    "assetLoading/fastOptJS",
    "assetLoading/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "tiledBuild",
  List(
    "tiled/fastOptJS",
    "tiled/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "radioBuild",
  List(
    "radio/fastOptJS",
    "radio/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "inputmapperBuild",
  List(
    "inputmapper/fastOptJS",
    "inputmapper/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "errorsBuild",
  List(
    "errors/fastOptJS",
    "errors/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "confettiRun",
  List(
    "confetti/fastOptJS",
    "confetti/indigoRun"
  ).mkString(";", ";", "")
)
