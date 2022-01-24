package millindigo

import mill._
import mill.scalalib._
import os.Path
import mill.define.Command
import java.io.File
import mill.define.Persistent
import indigoplugin.{IndigoRun, IndigoBuildMill, TemplateOptions}
import indigoplugin.IndigoCordova

trait MillIndigo extends mill.Module {

  val title: String
  val showCursor: Boolean
  val gameAssetsDirectory: Path
  val windowStartWidth: Int
  val windowStartHeight: Int

  def indigoBuild(): Command[Path] =
    T.command {
      val scriptPathBase: Path =  T.dest / os.up / "fastOpt.dest"

      IndigoBuildMill.build(
        T.dest,
        TemplateOptions(
          title,
          showCursor,
          scriptPathBase,
          gameAssetsDirectory
        )
      )

      T.dest
    }

  def indigoBuildFull(): Command[Path] =
    T.command {
      val outputDir: Path      = T.dest
      val scriptPathBase: Path = T.dest / os.up / "fullOpt.dest"

      IndigoBuildMill.build(
        outputDir,
        TemplateOptions(
          title,
          showCursor,
          scriptPathBase,
          gameAssetsDirectory
        )
      )

      outputDir
    }

  def indigoRun(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuild()()

      IndigoRun.run(outputDir, buildDir, title, windowStartWidth, windowStartHeight)
    }

  def indigoRunFull(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuildFull()()

      IndigoRun.run(outputDir, buildDir, title, windowStartWidth, windowStartHeight)
    }

  def indigoCordovaBuild(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuild()()

      IndigoCordova.run(outputDir, buildDir, title, windowStartWidth, windowStartHeight)
    }

  def indigoCordovaBuildFull(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuildFull()()

      IndigoCordova.run(outputDir, buildDir, title, windowStartWidth, windowStartHeight)
    }

}
