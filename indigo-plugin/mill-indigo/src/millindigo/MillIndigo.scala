package indigoplugin

import mill.*
import mill.scalajslib.ScalaJSModule
import os.Path
import indigoplugin.core.IndigoBuildMill
import indigoplugin.core.IndigoRun
import indigoplugin.core.IndigoCordova
import indigoplugin.IndigoOptions
import scala.annotation.nowarn

trait MillIndigo extends ScalaJSModule:

  /** Location of your Indigo game's assets folder. */
  def indigoAssets: Task[PathRef]

  /** Configuration options for your Indigo game. */
  def indigoOptions(assetsDirectory: os.Path): IndigoOptions

  /** Indigo source code generators */
  def indigoGenerators(assetsDirectory: os.Path): IndigoGenerators

  @nowarn("msg=unused")
  private def _indigoGeneratedSources = Task {
    val assetFolder = indigoAssets()

    indigoGenerators(assetFolder.path)
      .toSourcePaths(indigoOptions(assetFolder.path), assetFolder.path, Task.dest)

    Seq(PathRef(Task.dest))
  }

  @nowarn("msg=unused")
  override def generatedSources =
    Task {
      val custom: Seq[PathRef] =
        _indigoGeneratedSources()
          .map: p =>
            val from: os.Path = p.path
            val to            = Task.dest / from.last

            os.copy(from, to)
            PathRef(to)

      super.generatedSources() ++ custom
    }

  /** Build a static site for your game using Scala.js's fast linking. */
  def indigoBuild(): Task.Command[Path] =
    Task.Command {

      val scriptPathBase  = fastLinkJS().dest.path
      val assetsDirectory = indigoAssets().path

      IndigoBuildMill.build(
        scriptPathBase,
        indigoOptions(assetsDirectory),
        assetsDirectory,
        Task.dest
      )

      Task.dest
    }

  /** Build a static site for your game using Scala.js's full linking. */
  def indigoBuildFull(): Task.Command[Path] =
    Task.Command {
      val outputDir: Path = Task.dest
      val scriptPathBase  = fullLinkJS().dest.path
      val assetsDirectory = indigoAssets().path

      IndigoBuildMill.build(
        scriptPathBase,
        indigoOptions(assetsDirectory),
        assetsDirectory,
        outputDir
      )

      outputDir
    }

  /** Run your game using Electron and Scala.js's fast linking. */
  def indigoRun(): Task.Command[Unit] =
    Task.Command {
      val outputDir: Path = Task.dest
      val buildDir: Path  = indigoBuild()()
      val assetsDirectory = indigoAssets().path

      IndigoRun.run(
        outputDir,
        buildDir,
        indigoOptions(assetsDirectory)
      )
    }

  /** Run your game using Electron and Scala.js's full linking. */
  def indigoRunFull(): Task.Command[Unit] =
    Task.Command {
      val outputDir: Path = Task.dest
      val buildDir: Path  = indigoBuildFull()()
      val assetsDirectory = indigoAssets().path

      IndigoRun.run(
        outputDir,
        buildDir,
        indigoOptions(assetsDirectory)
      )
    }

  /** Build a Cordova app for your game using Scala.js's fast linking. */
  def indigoCordovaBuild(): Command[Unit] =
    Task.Command {
      val outputDir: Path = Task.dest
      val buildDir: Path  = indigoBuild()()
      val assetsDirectory = indigoAssets().path

      IndigoCordova.run(
        outputDir,
        buildDir,
        indigoOptions(assetsDirectory).metadata
      )
    }

  /** Build a Cordova app for your game using Scala.js's full linking. */
  def indigoCordovaBuildFull(): Task.Command[Unit] =
    Task.Command {
      val outputDir: Path = Task.dest
      val buildDir: Path  = indigoBuildFull()()
      val assetsDirectory = indigoAssets().path

      IndigoCordova.run(
        outputDir,
        buildDir,
        indigoOptions(assetsDirectory).metadata
      )
    }

object MillIndigo:

  object Utils {
    def findWorkspace: os.Path =
      sys.env
        .get("MILL_WORKSPACE_ROOT")
        .map(os.Path(_))
        .getOrElse(os.pwd)
  }
