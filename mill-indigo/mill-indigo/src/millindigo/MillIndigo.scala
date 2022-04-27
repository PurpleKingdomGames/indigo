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

  /** Title of your game.
    */
  val title: String

  /** Show the cursor?
    */
  val showCursor: Boolean

  /** Project relative path to a directory that contains all of the assets the game needs to load.
    */
  val gameAssetsDirectory: Path

  /** Initial window width.
    */
  val windowStartWidth: Int

  /** Initial window height.
    */
  val windowStartHeight: Int

  /** If possible, disables the runtime's frame rate limit, recommended to be `false`.
    */
  val disableFrameRateLimit: Boolean

  def indigoBuild(): Command[Path] =
    T.command {
      val scriptPathBase: Path = {
        val paths =
          List(
            T.dest / os.up / "fastOpt.dest",
            T.dest / os.up / "fastOpt" / "dest"
          )

        paths.find(os.exists) match {
          case Some(p) => p
          case None =>
            throw new Exception(
              "Could not find fastOpt dir, did you compile to JS? Tried: " +
                paths.map(_.toString).mkString("[", ", ", "]")
            )
        }
      }

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
      val outputDir: Path = T.dest
      val scriptPathBase: Path = {
        val paths =
          List(
            T.dest / os.up / "fullOpt.dest",
            T.dest / os.up / "fullOpt" / "dest"
          )

        paths.find(os.exists) match {
          case Some(p) => p
          case None =>
            throw new Exception(
              "Could not find fullOpt dir, did you compile to JS? Tried: " +
                paths.map(_.toString).mkString("[", ", ", "]")
            )
        }
      }

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

      IndigoRun.run(outputDir, buildDir, title, windowStartWidth, windowStartHeight, disableFrameRateLimit)
    }

  def indigoRunFull(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir: Path  = indigoBuildFull()()

      IndigoRun.run(outputDir, buildDir, title, windowStartWidth, windowStartHeight, disableFrameRateLimit)
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
