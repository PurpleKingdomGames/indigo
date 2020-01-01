package indigojs.delegates

import indigo.shared.scenegraph.PlaybackPattern

import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
sealed trait PlaybackPatternDelegate {
  
  @JSExport
  val playbackType: String

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def toInternal: PlaybackPattern =
    this.playbackType match {
      case "silent" =>
        PlaybackPattern.Silent

      case "single" =>
        PlaybackPattern.SingleTrackLoop(this.asInstanceOf[SingleTrackLoopDelegate].track.toInternal)
    }
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("PlaybackPatternHelper")
object PlaybackPatternDelegate {

  @JSExport
  val Silent: SilentDelegate =
    new SilentDelegate
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Silent")
final class SilentDelegate extends PlaybackPatternDelegate {
  @JSExport
  val playbackType: String = "silent"
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SingleTrackLoop")
final class SingleTrackLoopDelegate(_track: TrackDelegate) extends PlaybackPatternDelegate {

  @JSExport
  val track = _track

  @JSExport
  val playbackType: String = "single"
}
