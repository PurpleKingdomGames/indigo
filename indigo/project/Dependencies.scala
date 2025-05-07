import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._
import sbt.{Def, _}

object Dependencies {

  object Versions {
    val circe            = "0.14.13"
    val scalaCheck       = "1.18.1"
    val scalajsDom       = "2.8.0"
    val scalajsMacroTask = "1.1.1"
    val scalajsBenchmark = "0.10.0"
    val chartjs          = "1.0.2"
    val munit            = "1.1.1"
    val ultraviolet      = "0.6.0"
    val tyrianVersion    = "0.14.0"
  }

  object Shared {
    val munit      = Def.setting(Seq("org.scalameta" %%% "munit" % Versions.munit % Test))
    val scalaCheck = Def.setting(Seq("org.scalacheck" %%% "scalacheck" % Versions.scalaCheck % Test))
    val scalajsDom = Def.setting(Seq("org.scala-js" %%% "scalajs-dom" % Versions.scalajsDom))
  }

  val commonSettings = Shared.munit

  val indigoExtras = Shared.scalaCheck

  val jsDocs = Shared.scalajsDom

  val benchmark: Def.Initialize[Seq[sbt.ModuleID]] = Def.setting(
    Seq(
      "com.github.japgolly.scalajs-benchmark" %%% "benchmark" % Versions.scalajsBenchmark
    )
  )

  val benchmarkJs = Def.setting(
    Seq(
      "org.webjars" % "chartjs" % Versions.chartjs / "Chart.js" minified "Chart.min.js"
    )
  )

  val indigo = Def.setting(
    Seq(
      "org.scala-js"    %%% "scala-js-macrotask-executor" % Versions.scalajsMacroTask,
      "io.indigoengine" %%% "ultraviolet"                 % Versions.ultraviolet
    ) ++
      Shared.scalaCheck.value ++
      Shared.scalajsDom.value
  )

  val indigoJsonCirce = Def.setting(
    Seq(
      "io.circe" %%% "circe-core"   % Versions.circe,
      "io.circe" %%% "circe-parser" % Versions.circe
    )
  )
}
