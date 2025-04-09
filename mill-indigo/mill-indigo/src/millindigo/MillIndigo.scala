package indigoplugin

import mill._
import mill.scalalib._
import mill.scalajslib._
import os.Path
import mill.define.Command
import java.io.File
import mill.define.Persistent
import indigoplugin.core.IndigoBuildMill
import indigoplugin.core.IndigoRun
import indigoplugin.core.IndigoCordova
import indigoplugin.IndigoGenerators
import indigoplugin.IndigoOptions

trait MillIndigo extends ScalaJSModule {

  /** Configuration options for your Indigo game. */
  def indigoOptions: IndigoOptions

  /** Indigo source code generators */
  def indigoGenerators: IndigoGenerators

  override def generatedSources: T[Seq[PathRef]] = T {
    indigoGenerators.toSourcePaths(T.dest).map(mill.PathRef(_)) ++ super.generatedSources()
  }

  /** Build a static site for your game using Scala.js's fast linking. */
  def indigoBuild(): Command[Path] =
    T.command {

      val scriptPathBase: Path =
        fastLinkJS().dest.path

      IndigoBuildMill.build(
        scriptPathBase,
        T.dest,
        indigoOptions
      )

      T.dest
    }

  /** Build a static site for your game using Scala.js's full linking. */
  def indigoBuildFull(): Command[Path] =
    T.command {
      val outputDir: Path = T.dest
      val scriptPathBase: Path =
        fullLinkJS().dest.path

      IndigoBuildMill.build(
        scriptPathBase,
        outputDir,
        indigoOptions
      )

      outputDir
    }

  /** Run your game using Electron and Scala.js's fast linking. */
  def indigoRun(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuild()()

      IndigoRun.run(
        outputDir,
        buildDir,
        indigoOptions
      )
    }

  /** Run your game using Electron and Scala.js's full linking. */
  def indigoRunFull(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuildFull()()

      IndigoRun.run(
        outputDir,
        buildDir,
        indigoOptions
      )
    }

  /** Build a Cordova app for your game using Scala.js's fast linking. */
  def indigoCordovaBuild(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuild()()

      IndigoCordova.run(
        outputDir,
        buildDir,
        indigoOptions.metadata
      )
    }

  /** Build a Cordova app for your game using Scala.js's full linking. */
  def indigoCordovaBuildFull(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuildFull()()

      IndigoCordova.run(
        outputDir,
        buildDir,
        indigoOptions.metadata
      )
    }

}
