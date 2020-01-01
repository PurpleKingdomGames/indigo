package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.SceneAudio

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneAudio")
final class SceneAudioDelegate(_sourceA: SceneAudioSourceDelegate, _sourceB: SceneAudioSourceDelegate, _sourceC: SceneAudioSourceDelegate) {

  @JSExport
  val sourceA = _sourceA
  @JSExport
  val sourceB = _sourceB
  @JSExport
  val sourceC = _sourceC

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
