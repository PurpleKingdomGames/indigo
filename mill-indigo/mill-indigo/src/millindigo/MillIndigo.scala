package millindigo

import mill._
import mill.scalalib._
import os.Path
import mill.define.Command
import java.io.File
import mill.define.Persistent
import indigoplugin.core.IndigoBuildMill
import indigoplugin.core.IndigoRun
import indigoplugin.core.IndigoCordova
import indigoplugin.IndigoOptions

trait MillIndigo extends mill.Module {

  /** Configuration options for your Indigo game. */
  def indigoOptions: IndigoOptions

  def indigoBuild(): Command[Path] =
    T.command {
      val scriptPathBase: Path = {
        val paths =
          List(
            T.dest / os.up / "fastLinkJS.dest",
            T.dest / os.up / "fastOpt.dest",
            T.dest / os.up / "fastOpt" / "dest"
          )

        paths.find(os.exists) match {
          case Some(p) => p
          case None =>
            throw new Exception(
              "Could not find fastOpt / fastLinkJS dir, did you compile to JS? Tried: " +
                paths.map(_.toString).mkString("[", ", ", "]")
            )
        }
      }

      IndigoBuildMill.build(
        scriptPathBase,
        T.dest,
        indigoOptions
      )

      T.dest
    }

  def indigoBuildFull(): Command[Path] =
    T.command {
      val outputDir: Path = T.dest
      val scriptPathBase: Path = {
        val paths =
          List(
            T.dest / os.up / "fullLinkJS.dest",
            T.dest / os.up / "fullOpt.dest",
            T.dest / os.up / "fullOpt" / "dest"
          )

        paths.find(os.exists) match {
          case Some(p) => p
          case None =>
            throw new Exception(
              "Could not find fullOpt / fullLinkJS dir, did you compile to JS? Tried: " +
                paths.map(_.toString).mkString("[", ", ", "]")
            )
        }
      }

      IndigoBuildMill.build(
        scriptPathBase,
        outputDir,
        indigoOptions
      )

      outputDir
    }

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
