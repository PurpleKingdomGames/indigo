package millindigo

import mill._
import mill.scalalib._
import os._
import mill.define.Command
import java.io.File
import mill.define.Persistent
import indigoplugin.ElectronRequirements

trait MillIndigo extends mill.Module {

  val title: String
  val showCursor: Boolean
  val gameAssetsDirectory: Path
  val windowStartWidth: Int
  val windowStartHeight: Int

  def indigoBuild(): Command[Path] =
    T.command {
      val scriptPathBase: Path = T.dest / os.up / os.up / "fastOpt" / "dest"

      IndigoBuild.build(
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
      val scriptPathBase: Path = T.dest / os.up / os.up / "fullOpt" / "dest"

      IndigoBuild.build(
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

  private def run(outputDir: Path, buildDir: Path): Unit = {
    ElectronRequirements.filesToWrite(windowStartWidth, windowStartHeight).foreach { f =>
      os.write.over(outputDir / f.name, f.contents)
    }

    os.list(buildDir).foreach { file =>
      os.copy.into(file, outputDir, true, true, true, true)
    }

    println(s"Starting '$title'")

    os.proc("npm", "start")
      .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)

    ()
  }

  def indigoRun(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir        = indigoBuild()()

      run(outputDir, buildDir)
    }

  def indigoRunFull(): Command[Unit] =
    T.command {
      val outputDir: Path = T.dest
      val buildDir        = indigoBuildFull()()

      run(outputDir, buildDir)
    }

}
