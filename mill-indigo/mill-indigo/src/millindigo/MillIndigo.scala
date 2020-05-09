package millindigo

import mill._
import mill.scalalib._
import os._

trait MillIndigo extends mill.Module {

  val title: String
  val showCursor: Boolean
  val gameAssetsDirectory: Path

  // TODO: Convert from command to task to allow caching... how to depend on fastOpt's output?
  def indigoBuildJS() =
    T.command {
      val scriptPathBase: Path = T.dest / os.up / os.up / "fastOpt" / "dest"

      IndigoBuildJS.build(
        T.dest,
        TemplateOptions(
          title,
          showCursor,
          scriptPathBase,
          gameAssetsDirectory
        )
      )
    }

  def indigoBuildFullJS() =
    T.command {
      val scriptPathBase: Path = T.dest / os.up / os.up / "fullOpt" / "dest"

      IndigoBuildJS.build(
        T.dest,
        TemplateOptions(
          title,
          showCursor,
          scriptPathBase,
          gameAssetsDirectory
        )
      )
    }

}
