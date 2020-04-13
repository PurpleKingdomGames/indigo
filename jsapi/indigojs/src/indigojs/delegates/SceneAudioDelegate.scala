package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.scenegraph.SceneAudio

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneAudio")
final class SceneAudioDelegate(_sourceA: js.UndefOr[SceneAudioSourceDelegate], _sourceB: js.UndefOr[SceneAudioSourceDelegate], _sourceC: js.UndefOr[SceneAudioSourceDelegate]) {

  @JSExport
  val sourceA = _sourceA.toOption match {
      case Some(a) => a
      case None => SceneAudioSourceDelegate.None
  }

  @JSExport
  val sourceB = _sourceB.toOption match {
      case Some(b) => b
      case None => SceneAudioSourceDelegate.None
  }

  @JSExport
  val sourceC = _sourceC.toOption match {
      case Some(c) => c
      case None => SceneAudioSourceDelegate.None
  }

  @JSExport
  def concat(other: SceneAudioDelegate): SceneAudioDelegate =
    new SceneAudioDelegate(sourceA.concat(other.sourceA), sourceB.concat(other.sourceB), sourceC.concat(other.sourceC))


  def toInternal: SceneAudio =
    new SceneAudio(
      sourceA.toInternal,
      sourceB.toInternal,
      sourceC.toInternal
    )
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneAudioHelper")
@JSExportAll
object SceneAudioDelegate {

  val None: SceneAudioDelegate =
    new SceneAudioDelegate(SceneAudioSourceDelegate.None, SceneAudioSourceDelegate.None, SceneAudioSourceDelegate.None)

}
