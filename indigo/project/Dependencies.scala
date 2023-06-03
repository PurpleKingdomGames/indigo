import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.jsdependencies.sbtplugin.JSDependenciesPlugin.autoImport._
import sbt.{Def, _}

object Dependencies {

  object Versions {
    val circe            = "0.14.1"
    val scalaCheck       = "1.16.0"
    val scalajsDom       = "2.3.0"
    val scalajsMacroTask = "1.0.0"
    val scalajsBenchmark = "0.10.0"
    val chartjs          = "1.0.2"
    val munit            = "0.7.29"
    val ultraviolet      = "0.1.2"
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
