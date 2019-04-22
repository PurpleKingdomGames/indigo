
// Rebuild ScalaDocs and open in Firefox
addCommandAlias(
  "readdocs",
  List(
    "shared/doc",
    "indigo/doc",
    "indigoExts/doc",
    "openshareddocs",
    "openindigodocs",
    "openindigoextsdocs"
  ).mkString(";", ";", "")
)


addCommandAlias(
  "buildIndigo",
  List(
    "shared/compile",
    "circe9/compile",
    "indigoPlatforms/compile",
    "indigo/compile",
    "indigoExts/compile"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "buildDev",
  List(
    "sandbox/compile",
    "perf/compile",
    "framework/compile",
    "server/compile"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "buildExamples",
  List(
    "basicSetup/compile",
    "subSystems/compile",
    "scenesSetup/compile",
    "fullSetup/compile",
    "button/compile",
    "http/compile",
    "text/compile",
    "graphic/compile",
    "sprite/compile",
    "websocket/compile",
    "inputfield/compile",
    "audio/compile",
    "group/compile",
    "automata/compile"
  ).mkString(";", ";", "")
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
  "testIndigo",
  List(
    "shared/test",
    "indigo/test",
    "indigoExts/test"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testDev",
  List(
    "sandbox/test",
    "perf/test",
    "framework/test",
    "server/test"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testExamples",
  List(
    "basicSetup/test",
    "subSystems/test",
    "scenesSetup/test",
    "fullSetup/test",
    "button/test",
    "http/test",
    "text/test",
    "graphic/test",
    "sprite/test",
    "websocket/test",
    "inputfield/test",
    "audio/test",
    "group/test",
    "automata/test"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testAllNoClean",
  List(
    "testIndigo",
    "testDev",
    "testExamples"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testAll",
  List(
    "clean",
    "testAllNoClean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testCompileIndigo",
  List(
    "shared/test:compile",
    "indigo/test:compile",
    "indigoExts/test:compile"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testCompileDev",
  List(
    "sandbox/test:compile",
    "perf/test:compile",
    "framework/test:compile",
    "server/test:compile"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "testCompileExamples",
  List(
    "basicSetup/test:compile",
    "subSystems/test:compile",
    "scenesSetup/test:compile",
    "fullSetup/test:compile",
    "button/test:compile",
    "http/test:compile",
    "text/test:compile",
    "graphic/test:compile",
    "sprite/test:compile",
    "websocket/test:compile",
    "inputfield/test:compile",
    "audio/test:compile",
    "group/test:compile",
    "automata/test:compile"
  ).mkString(";", ";", "")
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
    "buildIndigo",
    "shared/publishLocal",
    "circe9/publishLocal",
    "indigoPlatforms/publishLocal",
    "indigo/publishLocal",
    "indigoExts/publishLocal"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuildJS",
  List(
    "buildIndigo",
    "sandbox/test",
    "sandbox/fastOptJS",
    "sandbox/indigoBuild"
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
  "perfBuild",
  List(
    "buildIndigo",
    "perf/test",
    "perf/fastOptJS",
    "perf/indigoBuild"
  ).mkString(";", ";", "")
)